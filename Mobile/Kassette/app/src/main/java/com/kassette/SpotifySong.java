package com.kassette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by saurabhsharma on 3/3/15.
 */
public class SpotifySong extends Song {
    Bitmap image;
    public Track track;
    Context context;
    SpotifySearchAdapter adapter;
    @Expose
    private String songTitle;
    @Expose
    private String songArtist;
    @Expose
    private String uri;
    @Expose
    private String albumURI;
    private SongAdapter songAdapter;

    public SpotifySong(Context context, String songArtist, String songTitle, String uri, String albumURI){
        //TODO mo fliped it.
        this.songArtist = songTitle;
        this.songTitle = songArtist;
        this.image = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_album_cover);
        this.uri = uri;
        this.albumURI = albumURI;
//        this.songAdapter = adapter;
//        new ImageAsyncTask().execute();
    }

    public SpotifySong(Context _context, Track _track, SpotifySearchAdapter adapter) {
        context = _context;
        track = _track;
        songTitle = _track.name;
        String artistNames = track.artists.get(0).name;
        if (track.artists.size() > 1) {
            for (ArtistSimple artist : track.artists)
                artistNames += ", " + artist.name;
        }
        songArtist = artistNames;
        this.uri = track.uri;
        this.albumURI = track.album.images.get(0).url;
        image = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_album_cover);
        this.adapter = adapter;
        //new ImageAsyncTask().execute();
        //TODO thread problem, cant have more than 5 AsyncTask at any given time.
        //http://stackoverflow.com/questions/4068984/running-multiple-asynctasks-at-the-same-time-not-possible/4072832#4072832
    }

public String getAlbumURL(){
    return albumURI;
}
    public String getTitle() {
//        return track.name;
        return songTitle;
    }


    public String getArtist() {

        return songArtist;


    }

    public long getDuration() {
        return track.duration_ms;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public Bitmap getAlbum() {
      return  image;
    }


    public class ImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                Log.e("ImageAsyncTask", "Hit ImageAsyncTask do in bg");
                Bitmap x;
                HttpURLConnection connection = (HttpURLConnection) new URL(albumURI).openConnection();
                connection.connect();

                InputStream input = connection.getInputStream();
                x = BitmapFactory.decodeStream(input);

                return x;
            } catch (IOException e) {
                Log.e("ImageAsyncTask", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.e("ImageAsyncTask", "Hit ImageAsyncTask post execute");
            if(result != null){
                image = result;
                songAdapter.notifyDataSetChanged();
            }
        }

    }

}
