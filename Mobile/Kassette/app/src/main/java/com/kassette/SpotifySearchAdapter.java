package com.kassette;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by saurabhsharma on 3/3/15.
 */
public class SpotifySearchAdapter extends ArrayAdapter<SpotifySong> {


    public SpotifySearchAdapter(Context context, int resource) {
        super(context, resource);
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view from the XML layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_song, null);

        // Get the title and artist string for this row
        String title = this.getItem(position).getTitle();
        ((TextView) v.findViewById(R.id.textViewSongTitle)).setText(title);
        ((TextView) v.findViewById(R.id.textViewSongDescription)).setText(this.getItem(position).getArtist());
        ((ImageView) v.findViewById(R.id.albumImageView)).setImageBitmap(getItem(position).getAlbum());

        return v;
    }


}
