package com.runze.yourheroes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.db.YourHeroesContract;
import com.runze.yourheroes.service.YourHeroesService;
import com.runze.yourheroes.utilities.CallBackItemSelection;

import java.lang.*;

/**
 * Created by Eloi Jr on 21/12/2014.
 */
public class PersonListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener,
        TextView.OnEditorActionListener, LoaderCallbacks<Cursor> {

    private final String LOG_TAG = PersonListFragment.class.getSimpleName();

    private static final String PERSONS = "persons";

    private static final int PERSON_LOADER = 0;

    private PersonCursorAdapter personCursorAdapter;
    private TextView edSearch;
    private ImageView btSearch;
    private ListView listPerson;

    // Columns showed by the loader
    private static final String[] PERSON_COLUMNS = {
            YourHeroesContract.PersonEntry._ID,
            YourHeroesContract.PersonEntry.COLUMN_MARVEL_ID,
            YourHeroesContract.PersonEntry.COLUMN_NAME,
            YourHeroesContract.PersonEntry.COLUMN_DESCRIPTION,
            YourHeroesContract.PersonEntry.COLUMN_URLDETAIL,
            YourHeroesContract.PersonEntry.COLUMN_LANDSCAPESMALL,
            YourHeroesContract.PersonEntry.COLUMN_STANDARDXLARGE
    };
    public static final int COL_PERSON_ID = 0;
    public static final int COL_PERSON__MARVEL_ID = 1;
    public static final int COL_PERSON_NAME = 2;
    public static final int COL_PERSON_DESCRIPTION = 3;
    public static final int COL_PERSON_URLDETAIL = 4;
    public static final int COL_PERSON_LANDSCAPESMALL = 5;
    public static final int COL_PERSON_STANDARDXLARGE = 6;

    public PersonListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.personfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        personCursorAdapter = new PersonCursorAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        edSearch = (TextView) rootView.findViewById(R.id.edsearch);
        edSearch.setOnEditorActionListener(this);
        btSearch = (ImageView) rootView.findViewById(R.id.btsearch);
        btSearch.setOnClickListener(this);
        listPerson = (ListView) rootView.findViewById(R.id.listperson);
        listPerson.setAdapter(personCursorAdapter);
        listPerson.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PERSON_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btsearch:
                seachPerson(edSearch.getText().toString());
                break;
        }
    }

    public void seachPerson(String search) {
        Intent intent = new Intent(getActivity(), YourHeroesService.class);
        intent.putExtra(YourHeroesService.SEARCH_PARAM, search);
        getActivity().startService(intent);
        getLoaderManager().restartLoader(PERSON_LOADER, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = personCursorAdapter.getCursor();
        if (cursor != null) {

            Person person = new Person();
            person.setId(cursor.getInt(COL_PERSON_ID));
            person.setMarvelId(cursor.getInt(COL_PERSON__MARVEL_ID));
            person.setName(cursor.getString(COL_PERSON_NAME));
            person.setDescription(cursor.getString(COL_PERSON_DESCRIPTION));
            person.setURLDetail(cursor.getString(COL_PERSON_URLDETAIL));
            person.setLandscapeSmallImageUrl(cursor.getString(COL_PERSON_LANDSCAPESMALL));
            person.setStandardXLargeImageUrl(cursor.getString(COL_PERSON_STANDARDXLARGE));

            ((CallBackItemSelection) getActivity()).onItemSelected(person);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (v == edSearch) {
            seachPerson(edSearch.getText().toString());

            // Hiding the soft keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edSearch.getWindowToken(), 0);

            handled = true;
        }
        return handled;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!edSearch.getText().toString().equals("")) {
            getLoaderManager().restartLoader(PERSON_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uriSearch;
        String pStartName = edSearch.getText().toString();

        if (pStartName.equals(""))
            pStartName = "A";

        uriSearch = YourHeroesContract.PersonEntry.buildPersonStartName(pStartName);
        Log.d(LOG_TAG, "uriSearch: "+uriSearch.toString());

        return new CursorLoader(
                getActivity(),
                uriSearch,
                PERSON_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        personCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        personCursorAdapter.swapCursor(null);
    }
}
