package com.runze.yourheroes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.runze.yourheroes.net.ConnectTask;
import com.runze.yourheroes.net.PersonClient;
import com.runze.yourheroes.utilities.Action;
import com.runze.yourheroes.utilities.CallBackItemSelection;
import com.runze.yourheroes.utilities.Tools;

import java.lang.*;
import java.util.ArrayList;

/**
 * Created by Eloi Jr on 21/12/2014.
 */
public class PersonListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, Action,
        TextView.OnEditorActionListener {

    private final static String PERSONS = "persons";

    private AdapterPerson adapterPerson;
    private ArrayList<Person> persons = new ArrayList<Person>();

    private TextView edSearch;
    private ImageView btSearch;
    private ListView listPerson;

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

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        edSearch = (TextView) rootView.findViewById(R.id.edsearch);
        edSearch.setOnEditorActionListener(this);
        btSearch = (ImageView) rootView.findViewById(R.id.btsearch);
        btSearch.setOnClickListener(this);
        listPerson = (ListView) rootView.findViewById(R.id.listperson);
        listPerson.setOnItemClickListener(this);

        adapterPerson = new AdapterPerson(this.getActivity(), persons);
        listPerson.setAdapter(adapterPerson);
        adapterPerson.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*if (savedInstanceState != null) {
            persons = (ArrayList<Person>) savedInstanceState.getSerializable(PERSONS);
            if (persons != null) {
                updateView();
            } else {
                seachPerson();
            }
        }*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btsearch:
                seachPerson();
                break;
        }
    }

    public void seachPerson() {
        ConnectTask connectTask = new ConnectTask(getActivity(), this, R.string.searching);
        try {
            connectTask.execute();
        } catch (Exception e) {

        }
    }

    @Override
    public void execute() throws Exception {
        PersonClient personClient = new PersonClient();
        if (edSearch.getText().toString().equals(""))
            persons = personClient.getPersons("A");
        else
            persons = personClient.getPersons(edSearch.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Person person = (Person) parent.getAdapter().getItem(position);
        ((CallBackItemSelection) getActivity()).onItemSelected(person);
    }

    @Override
    public void updateView() {
        if (persons != null) {
            adapterPerson = new AdapterPerson(PersonListFragment.this.getActivity(), persons);
            listPerson.setAdapter(adapterPerson);
            adapterPerson.notifyDataSetChanged();
        } else {
            Tools.alertDialog(getActivity(), getResources().getString(R.string.erro_searching_person));
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (v == edSearch) {
            seachPerson();

            // Hiding the soft keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edSearch.getWindowToken(), 0);

            handled = true;
        }
        return handled;
    }
}
