package com.runze.yourheroes.net;

import android.net.Uri;
import android.util.Log;

import com.runze.yourheroes.db.Person;
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
import java.util.List;

/**
 * Created by Eloi Jr on 12/01/2015.
 */
public class PersonClient {

    public static final String URL_MARVEL = "http://www.marvel.com";

    public static final String PUBLIC_KEY = "199a5380e83d0c5ab97677669503a6e8";
    public static final String PRIVATE_KEY = "429ace9a4246567523c98cc1f8d0d365f0444d5f";

    public static final String TIMESTAMP = "ts";
    public static final String API_KEY = "apikey";
    public static final String HASH = "hash";

    private final String LOG_TAG = PersonClient.class.getSimpleName();

    private final String BASE_URL = "http://gateway.marvel.com/v1/public/";
    private final String CHARACTER_BASE_URL = BASE_URL + "characters";

    public Person getPerson(int id) throws Exception {

        Uri builtUri = Uri.parse(CHARACTER_BASE_URL + "/" + String.valueOf(id)).buildUpon()
                    .build();

        String pss = fetchData(builtUri);
        ArrayList<Person> persons = new ArrayList<Person>();
        try {
            persons = convertJSON(pss);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        if (persons.size() > 0)
            return persons.get(0);
        else
            return null;
    }

    public ArrayList<Person> getPersons(String startName) {
        ArrayList<Person> persons = new ArrayList<Person>();

        String QUERY_PARAM = "nameStartsWith";
        String ORDER = "orderBy";

        String order = "name"; // name, modified, -name, -modified (- is descending order)

        Uri builtUri = Uri.parse(CHARACTER_BASE_URL+"?").buildUpon()
                .appendQueryParameter(QUERY_PARAM, startName)
                .appendQueryParameter(ORDER, order).build();

        String pss = fetchData(builtUri);
        try {
            persons = convertJSON(pss);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return persons;
    }

    private String fetchData(Uri builtUri) {
        String ts = Long.toString(System.currentTimeMillis() / 1000);
        String apikey = PUBLIC_KEY;
        String hash = Tools.md5(ts + PRIVATE_KEY + PUBLIC_KEY);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        InputStream inputStream;
        StringBuffer buffer = null;

        builtUri = builtUri.buildUpon().appendQueryParameter(TIMESTAMP, ts)
                .appendQueryParameter(API_KEY, apikey)
                .appendQueryParameter(HASH, hash).build();

        try {

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            Log.d(LOG_TAG, "Resposta: "+urlConnection.getResponseCode()+" - "+urlConnection.getResponseMessage());

            if (urlConnection.getResponseCode() != 200) {
                return null;
            }

            inputStream = urlConnection.getInputStream();
            buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
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
        return buffer.toString();
    }

    private ArrayList<Person> convertJSON(String json) throws JSONException {
        ArrayList<Person> persons = new ArrayList<Person>();

        JSONObject wrapper = new JSONObject(json);
        JSONObject data = wrapper.getJSONObject("data");
        JSONArray personArray = data.getJSONArray("results");

        ArrayList<Person> prs = new ArrayList<Person>();

        Log.d(LOG_TAG, json);
        for(int i = 0; i < personArray.length(); i++) {
            JSONObject jsonPerson = personArray.getJSONObject(i);
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

            prs.add(person);
        }
        return prs;
    }
}
