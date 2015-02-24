package com.runze.yourheroes.utilities;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.runze.yourheroes.PersonListFragment;
import com.runze.yourheroes.R;
import com.runze.yourheroes.net.ImageLoader;

/**
 * Created by Eloi Jr on 22/02/2015.
 */
public class PersonCursorAdapter extends CursorAdapter {

    private ImageLoader imageLoader;

    public PersonCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        imageLoader = new ImageLoader(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_person, parent, false);

        ViewHolderPerson viewHolderPerson = new ViewHolderPerson(view);
        view.setTag(viewHolderPerson);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolderPerson viewHolderPerson = (ViewHolderPerson) view.getTag();

        String name = cursor.getString(PersonListFragment.COL_COLUMN_NAME);
        String landscapeImage = cursor.getString(PersonListFragment.COL_COLUMN_LANDSCAPESMALL);

        viewHolderPerson.name.setText(name);
        viewHolderPerson.image.setTag(landscapeImage);
        imageLoader.displayImage(landscapeImage, viewHolderPerson.image, viewHolderPerson.progress);

    }

    public static class ViewHolderPerson {
        public final TextView name;
        public final ImageView image;
        public final ProgressBar progress;

        public ViewHolderPerson(View view) {
            name = (TextView) view.findViewById(R.id.name);
            image = (ImageView) view.findViewById(R.id.image);
            progress = (ProgressBar) view.findViewById(R.id.progress);
        }
    }

}
