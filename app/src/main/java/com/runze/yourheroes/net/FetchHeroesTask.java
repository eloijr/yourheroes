package com.runze.yourheroes.net;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.runze.yourheroes.AdapterPerson;
import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.utilities.Action;
import com.runze.yourheroes.utilities.ImageFormat;
import com.runze.yourheroes.utilities.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Eloi Jr on 12/01/2015.
 */
public class FetchHeroesTask extends AsyncTask<String, Void, ArrayList<Person>> {

    private final String LOG_TAG = FetchHeroesTask.class.getSimpleName();

    private final String PUBLIC_KEY = "199a5380e83d0c5ab97677669503a6e8";
    private final String PRIVATE_KEY = "429ace9a4246567523c98cc1f8d0d365f0444d5f";

    private Context context;
    private Action action;
    private int codMsg;

    private Dialog dialog;
    private Throwable exceptionError;

    public FetchHeroesTask(Context context, Action action, int codMsg) {
        this.context = context;
        this.action = action;
        this.codMsg = codMsg;
        if (codMsg == 0)
            dialog = null;
        else
            dialog = null; //Tools.dialogSpinner(context, context.getString(this.codMsg));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (dialog != null)
            dialog.show();
    }

    public ArrayList<Person> getPersonDataFromJson(String personJsonStr)
            throws JSONException {

        JSONObject wrapper = new JSONObject(personJsonStr);
        JSONObject data = wrapper.getJSONObject("data");
        JSONArray persons = data.getJSONArray("results");

        ArrayList<Person> chcs = new ArrayList<Person>();

        for(int i = 0; i < persons.length(); i++) {
            JSONObject jsonName = persons.getJSONObject(i);
            int id = jsonName.getInt("id");
            String name = jsonName.getString("name");
            String description = jsonName.getString("description");
            JSONObject thumbnail = jsonName.getJSONObject("thumbnail");
            String landscapeSmallImageUrl = thumbnail.getString("path") + "/" + ImageFormat.LANDSCAPE_SMALL+"."+thumbnail.getString("extension");
            String standardXLargeImageUrl = thumbnail.getString("path") + "/" + ImageFormat.STANDARD_FANTASTIC+"."+thumbnail.getString("extension");

            Person person = new Person();
            person.setId(id);
            person.setName(name);
            person.setDescription(description);
            person.setLandscapeSmallImageUrl(landscapeSmallImageUrl);
            person.setStandardXLargeImageUrl(standardXLargeImageUrl);

            chcs.add(person);
        }
        return chcs;

    }

    @Override
    protected ArrayList<Person> doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }
        boolean isSearchById = TextUtils.isDigitsOnly(params[0]);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String characterJsonStr = null;

        String ts = Long.toString(System.currentTimeMillis() / 1000);
        String apikey = PUBLIC_KEY;
        String hash = Tools.md5(ts + PRIVATE_KEY + PUBLIC_KEY);
        String order = "name"; // name, modified, -name, -modified (- is descending order)

        try {
            final String CHARACTER_BASE_URL =
                    "http://gateway.marvel.com/v1/public/characters";
            final String QUERY_PARAM = "nameStartsWith";
            final String TIMESTAMP = "ts";
            final String API_KEY = "apikey";
            final String HASH = "hash";
            final String ORDER = "orderBy";

            Uri builtUri;
            if (isSearchById) { // Seach by Id
                builtUri = Uri.parse(CHARACTER_BASE_URL + "/" + params[0]).buildUpon()
                        .appendQueryParameter(TIMESTAMP, ts)
                        .appendQueryParameter(API_KEY, apikey)
                        .appendQueryParameter(HASH, hash)
                        .appendQueryParameter(ORDER, order)
                        .build();
            } else { // Seach by name
                builtUri = Uri.parse(CHARACTER_BASE_URL+"?").buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(TIMESTAMP, ts)
                        .appendQueryParameter(API_KEY, apikey)
                        .appendQueryParameter(HASH, hash)
                        .appendQueryParameter(ORDER, order)
                        .build();
            }

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.d(LOG_TAG, "Resposta: "+urlConnection.getResponseCode()+" - "+urlConnection.getResponseMessage());

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            characterJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Character string: " + characterJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            this.exceptionError = e;
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        if (characterJsonStr != null) {
            try {
                return getPersonDataFromJson(characterJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                this.exceptionError = e;
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Person> persons) {
        if (persons != null) {
            action.updateView();
        } else {
            // alert dialog showing the error
        }
    }

    static class MarvelErrors {
        private final static int MISSING_API_KEY = 409; // Occurs when the apikey parameter is not included with a request.
        private final static int MISSING_HASH = 409; // Occurs when an apikey parameter is included with a request, a ts parameter is present, but no hash parameter is sent. Occurs on server-side applications only.
        private final static int MISSING_TIMESTAMP = 409; // Occurs when an apikey parameter is included with a request, a hash parameter is present, but no ts parameter is sent. Occurs on server-side applications only.
        private final static int INVALID_REFERER = 401; // Occurs when a referrer which is not valid for the passed apikey parameter is sent.
        private final static int INVALID_HASH = 401; // Occurs when a ts, hash and apikey parameter are sent but the hash is not valid per the above hash generation rule.
        private final static int METHOD_NOT_ALLOWED = 405; // Occurs when an API endpoint is accessed using an HTTP verb which is not allowed for that endpoint.
        private final static int FORBIDDEN = 403; // Occurs when a user with an otherwise authenticated request attempts to access an endpoint to which they do not have access.
    }

}
