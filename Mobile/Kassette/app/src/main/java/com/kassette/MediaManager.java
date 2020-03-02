package com.kassette;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;

public class MediaManager implements MediaPlayer.OnCompletionListener, View.OnClickListener, SongAdapter.Notify {

    private static final String TAG = "MediaManager";
    // UI components.
    private ImageView imageViewAlbumArt;
    private ImageButton buttonPausePlay;
    private TextView textViewSongTitle, textViewEndTime, textViewStartTime;

    // Used to play songs.
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler myHandler = new Handler();
    private SongAdapter adapter;

    // Used to determine if music is playing right now.
    private boolean isPlaying = false;
    private PlayListFragment frag;

    private boolean dontCompleteSpotifySong = false;

    public MediaManager(View view, SongAdapter adapter, PlayListFragment frag) {
        this.adapter = adapter;
        this.frag = frag;

        // Set onComplete listener.
        mediaPlayer.setOnCompletionListener(this);

        // Instantiate UI componets.
        buttonPausePlay = (ImageButton) view.findViewById(R.id.buttonPausePlay);
        imageViewAlbumArt = (ImageView) view.findViewById(R.id.imageViewAlbumArt);
        textViewSongTitle = (TextView) view.findViewById(R.id.textViewSongTitle);
        textViewStartTime = (TextView) view.findViewById(R.id.textViewStartTime);
        textViewEndTime = (TextView) view.findViewById(R.id.textViewEndTime);

        // Set listeners.
        buttonPausePlay.setOnClickListener(this);
        adapter.setNotifyListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonPausePlay:
                if(adapter.getCount() == 0){
                    return;
                }
                Song currentSong = adapter.getItem(0);
                // Pause song.
                if (isPlaying) {
                    isPlaying = false;
                    buttonPausePlay.setImageResource(R.drawable.play);
                    if(currentSong instanceof SimpleSong){
                        mediaPlayer.pause();
                    }
                    else if(currentSong instanceof SpotifySong){
                        try {
                            MainActivity.spotifyManager.pauseSong();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Play song
                else if (!isPlaying) {
                    isPlaying = true;
                    buttonPausePlay.setImageResource(R.drawable.pause);
                    if(currentSong instanceof SimpleSong){
                        mediaPlayer.start();
                        myHandler.postDelayed(updateSongTime, 100);
                    }
                    else if(currentSong instanceof SpotifySong){
                        try {
                            if(MainActivity.spotifyManager.isPaused){
                                MainActivity.spotifyManager.resumeSong();
                                MainActivity.spotifyManager.isPaused = false;
                            }else{
                                MainActivity.spotifyManager.PlaySong(((SpotifySong) adapter.getItem(0)).getUri());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //TODO: notify peers to start playing. //frag.syncWithPeer();
                break;
        }

    }

    private Runnable updateSongTime = new Runnable() {
        public void run() {
            long startTime = mediaPlayer.getCurrentPosition();
            textViewStartTime.setText(getFormattedTime("%d:%02d", startTime));
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void firstInQueue(Song song) {
        imageViewAlbumArt.setImageBitmap(song.getAlbum());
        textViewSongTitle.setText(song.getTitle());
        long endTime;
        try {
            if (song instanceof SimpleSong) {
                mediaPlayer.setDataSource(((SimpleSong) song).getPath());
                mediaPlayer.prepare();
                endTime = mediaPlayer.getDuration();
            } else {
                endTime = ((SpotifySong) song).getDuration();
            }

            textViewEndTime.setText(getFormattedTime(" / %d:%02d", endTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hidePausePlayButton() {
        buttonPausePlay.setVisibility(View.INVISIBLE);
    }

    public void skipSong() {
        // Don't skip if adapter is empty.
        if (adapter.getCount() == 0) {
            return;
        }
        mediaPlayer.pause();
        gotoNextSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        gotoNextSong();
        clear();
    }

    public void onCompleteSpotifySong(){
        if(!dontCompleteSpotifySong){
            gotoNextSong();
        }else{
            dontCompleteSpotifySong = false;
        }
    }

    public void clear(){
        textViewSongTitle.setText("Song");
        textViewStartTime.setText("0:00");
        textViewEndTime.setText(" / 0:00");
        imageViewAlbumArt.setImageResource(R.drawable.no_album_cover);
    }

    private void gotoNextSong() {
        mediaPlayer.reset();
        Song temp = adapter.getItem(0);
        if(temp instanceof SpotifySong){
            try {
                Log.d(TAG, "MO: skipping spotify song");
                dontCompleteSpotifySong = true;
                MainActivity.spotifyManager.pauseSong();
                MainActivity.spotifyManager.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adapter.removeCurrentSong();
        if (adapter.getCount() > 0) {
            Song song = adapter.getItem(0);
            imageViewAlbumArt.setImageBitmap(song.getAlbum());
            textViewSongTitle.setText(song.getTitle());

            if(isPlaying && song instanceof SimpleSong){
                mediaPlayer.start();
            }
            else if(isPlaying && song instanceof SpotifySong){
                try {

//                    MainActivity.spotifyManager.clear();
//                    dontCompleteSpotifySong = true;
                    MainActivity.spotifyManager.PlaySong(((SpotifySong) song).getUri());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            // Pause the current song/clear.
            if (isPlaying) {
                mediaPlayer.pause();
                try {
                    MainActivity.spotifyManager.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                buttonPausePlay.setImageResource(R.drawable.play);
                isPlaying = false;
            }
            myHandler.removeCallbacks(updateSongTime);
            clear();
        }
        frag.syncWithPeer();
    }

    /**
     * Helper method that will format milliseconds to minutes and seconds and arrange it based on
     * format passed.
     */
    private String getFormattedTime(String format, long startTime) {
        long min = TimeUnit.MILLISECONDS.toMinutes(startTime);
        long sec = TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(min);
        return String.format(format, min, sec);
    }


}
