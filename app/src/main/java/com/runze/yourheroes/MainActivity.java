package com.runze.yourheroes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.utilities.CallBackItemSelection;


public class MainActivity extends ActionBarActivity implements CallBackItemSelection {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private boolean mListWithDetail;

    private View footerMarvel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        footerMarvel = (View) findViewById(R.id.footer_marvel);

        if (findViewById(R.id.person_container) != null) {
            mListWithDetail = true;

            footerMarvel.setVisibility(View.INVISIBLE);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.person_container, new DetailPersonFragment())
                        .commit();
            }

        } else
            mListWithDetail = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Person person) {
        if (mListWithDetail) {

            Bundle args = new Bundle();
            args.putSerializable("person", person);

            DetailPersonFragment detailFragment = new DetailPersonFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.person_container, detailFragment)
                    .commit();

        } else {
            Intent i = new Intent(this, DetailPersonActivity.class);
            i.putExtra("person", person);
            startActivity(i);
        }
    }

}
