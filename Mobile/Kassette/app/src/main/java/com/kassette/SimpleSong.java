package com.kassette;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Base64;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.kassette.Song;

import java.io.ByteArrayOutputStream;

public class SimpleSong extends Song {
    private static final String TAG = "SimpleSong";
    private static final String NO_ALBUM_COVER = "no_album_cover";
    @Expose
    private String songTitle;
    @Expose
    private String songArtist;
    //    public String songAlbumEncoded;
    private Bitmap songAlbum;
    private String songPath;
    private Context context;

    // Used to retrieve song name and album cover.
    private static MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
    private static byte[] songImageData;

//    public SimpleSong(String songTitle, String songArtist, String songAlbumEncoded, Context context){
//        this.songTitle = songTitle;
//        this.songArtist = songArtist;
//        if(songAlbumEncoded.equals(NO_ALBUM_COVER)){
//            this.songAlbum = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_album_cover);
//        }else{
//            this.songAlbum = StringToBitMap(songAlbumEncoded, context);
//        }
//    }

    public SimpleSong(String songPath, Context context) {
        this.songPath = songPath;
        this.context = context;

        metaRetriver.setDataSource(songPath);
        try {
            songImageData = metaRetriver.getEmbeddedPicture();
            this.songAlbum = BitmapFactory.decodeByteArray(songImageData, 0, songImageData.length);
//            this.songAlbumEncoded = BitMapToString(this.songAlbum);
        } catch (Exception e) {
            this.songAlbum = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_album_cover);
//            this.songAlbumEncoded = NO_ALBUM_COVER;
        }

        try {
            this.songTitle = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (this.songTitle == null) {
                songTitle = "Unknown";
                Log.d(TAG, "MO: title is null");
            }

        } catch (Exception e) {
            this.songTitle = "Unknown";
            Log.d(TAG, "MO: " + e.getMessage());
        }
        try {
            this.songArtist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (songArtist == null) {
                this.songArtist = "Unknown";
                Log.d(TAG, "MO: artist is null");
            }

        } catch (Exception e) {
            this.songArtist = "Unknown";
            Log.d(TAG, "MO: " + e.getMessage());
        }


        // Clear data for next input.
        songImageData = null;
    }


//    /**
//     *
//     * http://androidtrainningcenter.blogspot.com/2012/03/how-to-convert-string-to-bitmap-and.html
//     */
//    private String BitMapToString(Bitmap bitmap){
//        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
//        byte [] b=baos.toByteArray();
//        String temp = Base64.encodeToString(b, Base64.DEFAULT);
//        return temp;
//    }
//
//    /**
//     *
//     * http://androidtrainningcenter.blogspot.com/2012/03/how-to-convert-string-to-bitmap-and.html
//     */
//    private Bitmap StringToBitMap(String encodedString, Context context) {
//        try {
//            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//            return bitmap;
//        } catch (Exception e) {
//            e.getMessage();
//            return BitmapFactory.decodeResource(context.getResources(), R.drawable.no_album_cover);
//        }
//    }

    public String getTitle() {
        return songTitle;
    }

    public String getArtist() {
        return songArtist;
    }

    public String getPath() {
        return songPath;
    }

    public Bitmap getAlbum() {
//        if(songAlbum == null){
//            if(songAlbumEncoded.equals(NO_ALBUM_COVER)){
//                this.songAlbum = BitmapFactory.decodeResource(PlayListFragment.context.getResources(), R.drawable.no_album_cover);
//            }else{
//                this.songAlbum = StringToBitMap(songAlbumEncoded, context);
//            }
//        }
        if (songAlbum == null) {
            this.songAlbum = BitmapFactory.decodeResource(PlayListFragment.context.getResources(), R.drawable.no_album_cover);
        }
        return songAlbum;
    }
}
