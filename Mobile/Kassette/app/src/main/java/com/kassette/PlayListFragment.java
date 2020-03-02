package com.kassette;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kassette.wifip2p.FileServerAsyncTask;
import com.kassette.wifip2p.FileTransferService;
import com.kassette.wifip2p.InfoHolder;
import com.kassette.wifip2p.PeerSyncServer;
import com.kassette.wifip2p.PeerSyncService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// http://mrbool.com/how-to-extract-meta-data-from-media-file-in-android/28130
public class PlayListFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener,
        View.OnClickListener, AdapterView.OnItemClickListener, PeerSyncServer.DataDisplay,
        FileServerAsyncTask.ResetFileServer {

    public static final String TAG = "PlayListFragment";

    // Used to specifically get a song from anywhere.
    private static final int CHOOSE_FILE_RESULT_CODE = 20;

    public static Context context;

    // UI components.
    private TextView textViewStatus;

    // Used when trying to send a song.
    public static WifiP2pInfo info;

    // Used to list music in ListView.
    public static SongAdapter adapter;

    // Used to display loading screen
    private ProgressDialog progressDialog;

    // Store the ip address of all peers.
    private ArrayList<String> peerList = new ArrayList<>();

    // Keep track of the number of down votes.
    private int downVoteCount = 0;

    private MediaManager mediaManager;
    private boolean spotifyActive = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "MO: onCreateView ");
        View view = inflater.inflate(R.layout.fragment_play_list, null);

        context = getActivity();

        // Let MainActivity know that this fragment has an action bar menu.
        setHasOptionsMenu(true);

        // Instantiate UI componets.
        textViewStatus = (TextView) view.findViewById(R.id.textViewStatus);
        ListView list = (ListView) view.findViewById(R.id.listViewSong);

        // Set up Adapter and MediaManger.
        adapter = new SongAdapter(getActivity(), R.layout.item_song);
        mediaManager = new MediaManager(view, adapter, this);

        //Set MediaManager in SpotifyManger
        MainActivity.spotifyManager.setMediaManager(mediaManager);

        // Set adapter.
        list.setAdapter(adapter);

        // Set onClick listeners
        view.findViewById(R.id.buttonThumbDown).setOnClickListener(this);
        view.findViewById(R.id.buttonThumbUp).setOnClickListener(this);
        list.setOnItemClickListener(this);

        // Let MainActivity now we are in a group and display connecting dialog.
        MainActivity.inGroup = true;
        showProgressDialog("Connecting...");
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.menu_playlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
        //TODO hide spotify button.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_song:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                return true;

            case R.id.action_exit:
                ((MainActivity) getActivity()).disconnect();
                ((MainActivity) getActivity()).inGroup = false;
                ((MainActivity) getActivity()).gotoFragment(MainFragment.TAG + "1");
                return true;

            case R.id.action_spotify:
                if (info.isGroupOwner && !spotifyActive) {
                    MainActivity.spotifyManager.openLoginWindow();
                    spotifyActive = true;
                    //TODO notify peers;
                } else if (spotifyActive) {
                    ((MainActivity) getActivity()).gotoFragment(SearchFragment.TAG);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * implements: WifiP2pManager.ConnectionInfoListener
     * <p/>
     * Call back method for manager when calling on the requestConnectionInfo().
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // Save the info to be used when sending a song and stop progress dialog.
        this.info = info;
        progressDialog.cancel();
        Log.d(TAG, "MO: received the connection info");

        // After the group negotiation, we assign the group owner as the file server. The file
        // server is single threaded, single connection server socket.
        if (info.groupFormed && info.isGroupOwner) {

            // Display phone status.
            textViewStatus.setText("Group owner - " + MakeTripDialog.tripPassword);

            // Start server to songs from peers.
            new FileServerAsyncTask(getActivity(), this).execute();

            // Start server to receive ip addresses of peers, if not already doing so.
            if (!PeerSyncServer.isOpen) {
                Log.d(TAG, "MO: opening peer receiver server");
                PeerSyncServer server = new PeerSyncServer(PeerSyncServer.RECIEVE_IP_PORT);
                server.setEventListener(this);
                server.startListening();
            }
        } else if (info.groupFormed && !PeerSyncServer.isOpen) {

            //TODO remove spotifyActive
            spotifyActive = true;

            // Display phone status and hide the PausePlayButton.
            textViewStatus.setText("Group peer");
            mediaManager.hidePausePlayButton();

            // Get local ip address.
            WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wm.getConnectionInfo().getIpAddress();
            String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

            // Notify group owner of peer's ip address.
            peerSyncIntent(info.groupOwnerAddress.getHostAddress(), PeerSyncServer.RECIEVE_IP_PORT, InfoHolder.KEY_REGISTER_WITH_GROUP_OWNER, ip);

            // Start server to receive info from group owner, if not already doing so.
            Log.d(TAG, "MO: opening peer info server");
            PeerSyncServer server = new PeerSyncServer(PeerSyncServer.SEND_INFO_PORT);
            server.setEventListener(this);
            server.startListening();
        }
    }

    /**
     * Implements: FileServerAsyncTask.ResetFileServer
     * <p/>
     * Call back method to create a new FileServerAsyncTask only if you are the group owner.
     */
    @Override
    public void resetFileServer(String result) {
        progressDialog.cancel();
        if (info.groupFormed && info.isGroupOwner) {
            if (result != null) {
                adapter.add(result);
                Log.d(TAG, "MO: File copied path - " + "file://" + result);
                syncWithPeer();
            }
            Log.d(TAG, "MO: reset FileServerAsyncTask");
            new FileServerAsyncTask(getActivity(), this).execute();
        }
    }


    /**
     * Create a new PeerSyncServer.
     */
    public void resetPeerSync(int port) {
        Log.d(TAG, "MO: reset resetPeerSync");
        PeerSyncServer server = new PeerSyncServer(port);
        server.setEventListener(this);
        server.startListening();
    }


    /**
     * Implements: PeerSyncServer.DataDisplay,
     *
     * Call back from PeerSyncServer to perform some task depending on what is passed back.
     */
    @Override
    public void display(InfoHolder message) {
        // Get values to be used.
        String value = message.getValue();

        switch (message.getKey()) {

            case InfoHolder.KEY_SYNC_SONG_ADAPTER:
                showProgressDialog("Syncing...");
                adapter.setJsonList(value);
                progressDialog.cancel();
                break;

            case InfoHolder.KEY_SPOTIFY_SONG_ADDED:
                String title = "", artist = "", uri = "", albumURI = "";
                try {
                    JSONObject json = new JSONObject(value);
                    title = json.getString("DaTitle");
                    artist = json.getString("Artist");
                    uri = json.getString("uri");
                    albumURI = json.getString("albumURL");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SpotifySong song = new SpotifySong(getActivity(), title, artist, uri, albumURI);
                adapter.add(song);
                syncWithPeer();
                break;

            case InfoHolder.KEY_REGISTER_WITH_GROUP_OWNER:
                if (info.isGroupOwner) {
                    peerList.add(value);
                    syncWithPeer();
                    Log.d(TAG, "MO: Registering - " + value);
                    Toast.makeText(getActivity(), "" + value, Toast.LENGTH_SHORT).show();
                }
                break;

            case InfoHolder.KEY_DOWN_VOTE:
                downVoteCount++;
                if (info.isGroupOwner && downVoteCount >= (peerList.size() + 1 / 2)) {
                    mediaManager.skipSong();
                    syncWithPeer();
                    downVoteCount = 0;
                }
                break;

            //else if (message.contains("[")) {
            //mediaManager.clear();
        }
    }


    /**
     * This is the callback method after a user is done selecting a song. If the group owner picks a
     * song then it will add it to the ListView and sync all the peers. If not the group owner then
     * it will add it to the current ListView and then send the entire song to the group owner.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SpotifyManager.REQUEST_CODE) {
            MainActivity.spotifyManager.spotifyResponse(requestCode, resultCode, data);
            return;
        }

        // End the method here if user did not select a song.
        if (data == null) {
            Log.d(PlayListFragment.TAG, "MO: User did not select a song");
            return;
        }

        // End method here if info was never received in onConnectionInfoAvailable.
        if (info == null) {
            Log.d(PlayListFragment.TAG, "MO: WifiP2pInfo is null");
            return;
        }

        // User has picked an image. Transfer it to group owner i.e peer using FileTransferService.
        Uri uri = data.getData();
        Log.d(PlayListFragment.TAG, "Intent----------- " + uri.toString());

        // Helper method gotten from:
        // http://stackoverflow.com/questions/5657411/android-getting-a-file-uri-from-a-content-uri
        String filePath = null;
        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = uri.getPath();
        }

        Log.d(TAG, "MO: Chosen path = " + filePath);

        // If group owner, add song to ListView and then sync peers.
        if (info.isGroupOwner) {
            adapter.add(filePath);
            syncWithPeer();

            // If not group owner, add to ListView and then push song to group owner.
        } else {
            adapter.add(filePath);
            showProgressDialog("Sending song...");

            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        }
    }

    public void syncWithPeer() {

        if (peerList.isEmpty()) {
            Toast.makeText(getActivity(), "Peer list empty", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "MO: display - Peer list empty");
            return;
        }

        Log.d(TAG, "MO: Syncing...");
        String json = adapter.getJsonList();
        Log.d(TAG, "MO: " + json);

        for (int i = 0; i < peerList.size(); i++) {
            peerSyncIntent(peerList.get(i), PeerSyncServer.SEND_INFO_PORT, InfoHolder.KEY_SYNC_SONG_ADAPTER, json);
        }
    }

    private void peerSyncIntent(String ip, int port, int key, String value) {
        Intent serviceIntent = new Intent(getActivity(), PeerSyncService.class);
        serviceIntent.setAction(PeerSyncService.ACTION_SYNC_PEER);
        serviceIntent.putExtra(PeerSyncService.EXTRAS_PEER_ADDRESS, ip);
        serviceIntent.putExtra(PeerSyncService.EXTRAS_PEER_PORT, port);

        serviceIntent.putExtra(PeerSyncService.EXTRAS_KEY, key);
        serviceIntent.putExtra(PeerSyncService.EXTRAS_VALUE, value);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonThumbUp:

                break;

            case R.id.buttonThumbDown:
                if (info.isGroupOwner && !adapter.isEmpty()) {
                    downVoteCount++;
                } else if (!adapter.isEmpty()) {
                    // Send down vote count to group owner.
                    peerSyncIntent(info.groupOwnerAddress.getHostAddress(), PeerSyncServer.RECIEVE_IP_PORT, InfoHolder.KEY_DOWN_VOTE, "DownVote");
                }

                if (info.isGroupOwner && (downVoteCount >= (peerList.size() + 1 / 2))) {
                    mediaManager.skipSong();
                    syncWithPeer();
                    downVoteCount = 0;
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Show dialog", Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog(String message) {
        // Create progressDialog and allow it to be canceled via back button.
        progressDialog = ProgressDialog.show(getActivity(), "", message, true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    progressDialog.cancel();
                }
                return false;
            }
        });
    }


}