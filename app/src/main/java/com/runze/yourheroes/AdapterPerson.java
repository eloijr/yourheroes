package com.runze.yourheroes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.runze.yourheroes.db.Person;
import com.runze.yourheroes.net.ImageLoader;

import java.util.ArrayList;

/**
 * Created by Eloi Jr on 02/01/2015.
 */
public class AdapterPerson extends BaseAdapter {

    private final String TAG = this.getClass().getName();

    private Context context;
    private ArrayList<Person> persons;
    private LayoutInflater mInflater;
    private ImageLoader imageLoader;

    public AdapterPerson(Context context, ArrayList<Person> persons) {
        this.context = context;
        this.persons = persons;

        mInflater = LayoutInflater.from(context);
        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
        return persons.size();
    }

    @Override
    public Person getItem(int pos) {
        return persons.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        Person person = persons.get(pos);
        ViewHolderPerson holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_person, null);

            holder = new ViewHolderPerson();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.image = (ImageView) view.findViewById(R.id.image);
            holder.progress = (ProgressBar) view.findViewById(R.id.progress);
            view.setTag(holder);
        } else {
            holder = (ViewHolderPerson) view.getTag();
        }
        if (person != null) {
            holder.name.setText(person.getName());
            holder.image.setTag(person.getLandscapeSmallImageUrl());
            imageLoader.displayImage(person.getLandscapeSmallImageUrl(), holder.image, holder.progress);
        }
        return view;
    }

    static class ViewHolderPerson {
        TextView id;
        TextView name;
        TextView description;
        ImageView image;
        ProgressBar progress;
    }

}
