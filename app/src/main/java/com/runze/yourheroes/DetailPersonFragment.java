package com.runze.yourheroes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.net.ConnectTask;
import com.runze.yourheroes.net.ImageLoader;
import com.runze.yourheroes.net.PersonClient;
import com.runze.yourheroes.utilities.Action;
import com.runze.yourheroes.utilities.Tools;

/**
 * Created by Eloi Jr on 09/01/2015.
 */
public class DetailPersonFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = DetailPersonFragment.class.getSimpleName();

    private TextView mName;
    private ImageView mImagePerson;
    private ProgressBar mProgress;
    private TextView mDescription;
    private TextView mMoreDetails;

    private Person person;

    public DetailPersonFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            person = (Person) args.getSerializable("person");
        } else {
            person = null;
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_person, container, false);
        mName = (TextView) rootView.findViewById(R.id.fragment_detail_name);
        mImagePerson = (ImageView) rootView.findViewById(R.id.fragment_detail_image);
        mProgress = (ProgressBar) rootView.findViewById(R.id.fragment_detail_progress);
        mDescription = (TextView) rootView.findViewById(R.id.fragment_detail_description);
        mMoreDetails = (TextView) rootView.findViewById(R.id.seedetails);
        mMoreDetails.setOnClickListener(this);

        if (person != null) {
            mName.setText(person.getName());
            mImagePerson.setTag(person.getStandardXLargeImageUrl());
            mDescription.setText(person.getDescription());

            ImageLoader imageLoader = new ImageLoader(getActivity());
            imageLoader.displayImage(person.getStandardXLargeImageUrl(), mImagePerson, mProgress);
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.seedetails) {
            Intent intent;
            if ((person.getURLDetail() == null) || (person.getURLDetail().equals("")))
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PersonClient.URL_MARVEL));
            else
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(person.getURLDetail())); //+ Tools.genKeyUser()));
            startActivity(intent);
        }
    }

}
