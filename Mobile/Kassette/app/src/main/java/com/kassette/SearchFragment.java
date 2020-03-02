package com.kassette;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.kassette.wifip2p.InfoHolder;
import com.kassette.wifip2p.PeerSyncServer;
import com.kassette.wifip2p.PeerSyncService;

import org.json.JSONException;
import org.json.JSONObject;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;

import retrofit.client.Response;

/**
 * Created by saurabhsharma on 3/2/15.
 */
public class SearchFragment extends Fragment implements View.OnClickListener, Callback<TracksPager>, AdapterView.OnItemClickListener {

    public static final String TAG = "SearchFragment";
    private View myView;
    private SpotifySearchAdapter resultsAdapter;
    private TracksPager res;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_music, null);
        resultsAdapter = new SpotifySearchAdapter(getActivity(), R.layout.item_song);
        myView = view;
        ((ListView) view.findViewById(R.id.trackList)).setAdapter(resultsAdapter);
        view.findViewById(R.id.searchButton).setOnClickListener(this);
        ((ListView) view.findViewById(R.id.trackList)).setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                doSearch();
                break;
        }
    }

    public void doSearch() {

        EditText song = (EditText) myView.findViewById(R.id.songEditText);

        if (song.getText().length() > 0) {
            MainActivity.spotifyManager.spotify.searchTracks(String.valueOf(song.getText()), this);
        }
        dialog = ProgressDialog.show(getActivity(),"","Searching...",true);
        dialog.setCanceledOnTouchOutside(false);

    }

    @Override
    public void success(TracksPager tracksPager, Response response) {
        res = tracksPager;
        Message msg = Message.obtain();
        handler.sendMessage(msg);
        dialog.cancel();


    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            resultsAdapter.clear();
            for (Track track : res.tracks.items) {
                SpotifySong song = new SpotifySong(getActivity(),track, resultsAdapter);
                resultsAdapter.add(song);
            }
        }
    };


    @Override
    public void failure(RetrofitError retrofitError) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((MainActivity) getActivity()).gotoFragment(PlayListFragment.TAG);
        PlayListFragment.adapter.add(resultsAdapter.getItem(position));

        SpotifySong song = resultsAdapter.getItem(position);


        if(!PlayListFragment.info.isGroupOwner){
            JSONObject json = new JSONObject();
            try {
                json.put("DaTitle", song.getTitle());
                json.put("Artist", song.getArtist());
                json.put("uri", song.getUri());
                json.put("albumURL", song.getAlbumURL());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent serviceIntent = new Intent(getActivity(), PeerSyncService.class);
            serviceIntent.setAction(PeerSyncService.ACTION_SYNC_PEER);
            serviceIntent.putExtra(PeerSyncService.EXTRAS_PEER_ADDRESS, PlayListFragment.info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(PeerSyncService.EXTRAS_PEER_PORT, PeerSyncServer.RECIEVE_IP_PORT);
            serviceIntent.putExtra(PeerSyncService.EXTRAS_KEY, InfoHolder.KEY_SPOTIFY_SONG_ADDED);
            serviceIntent.putExtra(PeerSyncService.EXTRAS_VALUE, json.toString());
            getActivity().startService(serviceIntent);
        }

    }
}
