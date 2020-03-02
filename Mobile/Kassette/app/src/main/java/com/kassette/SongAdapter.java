package com.kassette;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maybe change to BaseAdapter. --Mo
 * http://www.piwai.info/android-adapter-good-practices/
 */
public class SongAdapter extends ArrayAdapter<Song> {

    private static final String TAG = "SongAdapter";
    private Notify notify;

    public interface Notify { void firstInQueue(Song song);}

    public void setNotifyListener(Notify notify) {
        this.notify = notify;
    }

    public SongAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void add(String songPath) {
        super.add(new SimpleSong(songPath, getContext()));
        if(getCount() == 1){
            notify.firstInQueue(getItem(0));
        }
    }

    public void add(SpotifySong song){
        super.add(song);
        if(getCount() == 1){
            notify.firstInQueue(getItem(0));
        }
    }

    public void removeCurrentSong() {
        if (getCount() > 0) {
            remove(getItem(0));
        }
        if(getCount() > 0){
            notify.firstInQueue(getItem(0));
        }
    }

    public String getJsonList(){
        ArrayList<Song> list = new ArrayList<>();
        for(int i = 0; i < getCount(); i++){
            list.add(getItem(i));
        }

        // Set up builder.
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.excludeFieldsWithoutExposeAnnotation();

        // Create
        Gson gson = builder.create();
        String json = gson.toJson(list);

        return json.toString();
    }

    public void setJsonList(String json){
        clear();
        Gson gson = new Gson();
        List<SimpleSong> list = Arrays.asList(gson.fromJson(json, SimpleSong[].class));

        addAll(list);
        if(getCount() >= 1){
            notify.firstInQueue(getItem(0));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view from the XML layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_song, null);

        if (position == 0) {
            return new View(getContext());
        }

        // Get the title and artist string for this row
        String title = this.getItem(position).getTitle();
        ((TextView) v.findViewById(R.id.textViewSongTitle)).setText(title);
        ((TextView) v.findViewById(R.id.textViewSongDescription)).setText(getItem(position).getArtist());
        ((ImageView) v.findViewById(R.id.albumImageView)).setImageBitmap(getItem(position).getAlbum());

        return v;
    }
}
