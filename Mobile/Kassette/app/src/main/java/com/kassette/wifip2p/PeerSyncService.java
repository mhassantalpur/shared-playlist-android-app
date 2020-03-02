package com.kassette.wifip2p;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.kassette.PlayListFragment;

import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PeerSyncService extends IntentService {

    public static final String TAG = "PeerSyncService";
    public static final String ACTION_SYNC_PEER = "com.example.android.wifidirect.SYNC_PEER";
    public static final String EXTRAS_PEER_ADDRESS = "go_host";
    public static final String EXTRAS_PEER_PORT = "go_port";
    public static final String EXTRAS_KEY = "go_key";
    public static final String EXTRAS_VALUE = "go_value";

    public PeerSyncService(String name) {
        super(name);
    }

    public PeerSyncService() {
        super("FileTransferService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(ACTION_SYNC_PEER)) {
            // Get host ip address and port number.
            int port = intent.getExtras().getInt(EXTRAS_PEER_PORT);
            String host = intent.getExtras().getString(EXTRAS_PEER_ADDRESS);
            int key = intent.getExtras().getInt(EXTRAS_KEY);
            String value = intent.getExtras().getString(EXTRAS_VALUE);
            try {
                Log.d(TAG, "Opening peer socket - ");
                Socket clientSocket = new Socket(host, port);
                InfoHolder hold = new InfoHolder(key, value);
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());

                Log.d(TAG, "Sending - " + value);
                oos.writeObject(hold);

                oos.close();
                clientSocket.close();
                Log.d(TAG, "Closing peer socket - ");

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

}
