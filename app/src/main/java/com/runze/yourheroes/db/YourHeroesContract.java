package com.runze.yourheroes.db;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Eloi Jr on 31/01/2015.
 */
public class YourHeroesContract {

    public static final String CONTENT_AUTHORITY = "com.runze.yourheroes";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PERSON = "person";

    public static final String PATH_PERSON_MARVEL_ID = "marvelid";
    public static final String PATH_PERSON_STARTNAME = "startname";

    public static final class PersonEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PERSON).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PERSON;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PERSON;

        public static final String TABLE_NAME = "person";

        public static final String COLUMN_MARVEL_ID = "mid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_URLDETAIL = "URLDetail";
        public static final String COLUMN_LANDSCAPESMALL = "landscapeSmallImageUrl";
        public static final String COLUMN_STANDARDXLARGE = "standardXLargeImageUrl";

        public static Uri buildPersonUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPersonMarvelID(String marvelID) {
            return CONTENT_URI.buildUpon().appendPath(PATH_PERSON_MARVEL_ID).appendPath(marvelID).build();
        }

        public static Uri buildPersonStartName(String startName) {
            return CONTENT_URI.buildUpon().appendPath(PATH_PERSON_STARTNAME).appendPath(startName).build();
        }

        public static String getMarvelIDFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartNameFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }
}
