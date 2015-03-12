package com.runze.yourheroes.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.runze.yourheroes.PersonListFragment;
import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.db.YourHeroesContract;
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
import java.util.Vector;

/**
 * Created by Eloi Jr on 20/02/2015.
 */
public class YourHeroesService extends IntentService {

    private final String LOG_TAG = YourHeroesService.class.getSimpleName();

    public static final String SEARCH_PARAM = "param";

    public YourHeroesService() {
        super("YourHeroes");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isSearchById = false;

        String param = intent.getStringExtra(SEARCH_PARAM);
        Log.d(LOG_TAG, "Param:" + param);
        isSearchById = TextUtils.isDigitsOnly(param);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String characterJsonStr = null;

        String ts = Long.toString(System.currentTimeMillis() / 1000);
        String apikey = Tools.PUBLIC_KEY;
        String hash = Tools.md5(ts + Tools.PRIVATE_KEY + Tools.PUBLIC_KEY);
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
                builtUri = Uri.parse(CHARACTER_BASE_URL + "/" + param).buildUpon()
                        .appendQueryParameter(TIMESTAMP, ts)
                        .appendQueryParameter(API_KEY, apikey)
                        .appendQueryParameter(HASH, hash)
                        .appendQueryParameter(ORDER, order)
                        .build();
            } else { // Seach by name
                builtUri = Uri.parse(CHARACTER_BASE_URL+"?").buildUpon()
                        .appendQueryParameter(QUERY_PARAM, param)
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
                return;
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
                return;
            }
            characterJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Character string: " + characterJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
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
            ArrayList<Person> persons = null;
            try {
                persons = getPersonDataFromJson(characterJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            Vector<ContentValues> vcPerson = new Vector<ContentValues>(persons.size());
            for (Person p : persons) {
                ContentValues personValues = new ContentValues();
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_MARVEL_ID, p.getId());
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_NAME, p.getName());
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_DESCRIPTION, p.getDescription());
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_URLDETAIL, p.getURLDetail());
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_LANDSCAPESMALL, p.getLandscapeSmallImageUrl());
                personValues.put(YourHeroesContract.PersonEntry.COLUMN_STANDARDXLARGE, p.getStandardXLargeImageUrl());
                vcPerson.add(personValues);
            }
            if (vcPerson.size() > 0) {
                ContentValues[] arPerson = new ContentValues[vcPerson.size()];
                vcPerson.toArray(arPerson);
                try {
                    // delete all records before super insert!
                    this.getContentResolver().delete(YourHeroesContract.PersonEntry.CONTENT_URI, null, null);

                    this.getContentResolver().bulkInsert(YourHeroesContract.PersonEntry.CONTENT_URI, arPerson);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        // Sending message that the task was finished
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(PersonListFragment.ServiceResponseReceive.ACTION_SERVICE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);

    }

    public ArrayList<Person> getPersonDataFromJson(String personJsonStr)
            throws JSONException {

        JSONObject wrapper = new JSONObject(personJsonStr);
        JSONObject data = wrapper.getJSONObject("data");
        JSONArray persons = data.getJSONArray("results");

        ArrayList<Person> chcs = new ArrayList<Person>();

        for(int i = 0; i < persons.length(); i++) {
            JSONObject jsonPerson = persons.getJSONObject(i);
            int id = jsonPerson.getInt("id");
            String name = jsonPerson.getString("name");
            String description = jsonPerson.getString("description");

            String URLDetail = "";
            String URLWiki = "";
            String URLComiclink = "";
            JSONArray urlsArray = jsonPerson.getJSONArray("urls"); //detail, wiki, comiclink
            for(int u = 0; u < urlsArray.length(); u++) {
                JSONObject jsonUrl = urlsArray.getJSONObject(u);
                String type = jsonUrl.getString("type");
                String url = jsonUrl.getString("url");
                if (type.equals("detail")) {
                    URLDetail = url;
                }
                if (type.equals("wiki")) {
                    URLWiki = url;
                }
                if (type.equals("comiclink")) {
                    URLComiclink = url;
                }
            }
            // I decide URLDetail is priority. I like the info there :)
            if (URLDetail.equals("")) {
                if (!URLWiki.equals("")) // if detail is null use the wiki url
                    URLDetail = URLWiki;
                else // otherwise, use the comiclink url
                    URLDetail = URLComiclink;
            }

            String landscapeSmallImageUrl;
            String standardXLargeImageUrl;
            if (!jsonPerson.isNull("thumbnail")) {
                JSONObject thumbnail = jsonPerson.getJSONObject("thumbnail");
                landscapeSmallImageUrl = thumbnail.getString("path") + "/" + ImageFormat.LANDSCAPE_SMALL + "." + thumbnail.getString("extension");
                standardXLargeImageUrl = thumbnail.getString("path") + "/" + ImageFormat.STANDARD_FANTASTIC + "." + thumbnail.getString("extension");
            } else {
                landscapeSmallImageUrl = "";
                standardXLargeImageUrl = "";
            }

            Person person = new Person();
            person.setId(id);
            person.setName(name);
            person.setDescription(description);
            person.setURLDetail(URLDetail);
            person.setLandscapeSmallImageUrl(landscapeSmallImageUrl);
            person.setStandardXLargeImageUrl(standardXLargeImageUrl);

            chcs.add(person);
        }
        return chcs;

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
