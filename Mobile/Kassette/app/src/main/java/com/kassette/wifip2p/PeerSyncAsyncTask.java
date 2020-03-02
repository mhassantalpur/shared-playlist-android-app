package com.kassette.wifip2p;

import android.os.AsyncTask;
import android.util.Log;
import com.kassette.PlayListFragment;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerSyncAsyncTask extends AsyncTask<Void, Void, String> {


    // Currently this class is not used, hopefully will be able to fix it in the feature. --MO
    private static final String TAG = "PeerSyncAsyncTask";
    private PlayListFragment frag;

    public PeerSyncAsyncTask(PlayListFragment frag) {
        this.frag = frag;
    }

    @Override
    protected void onPreExecute() {
        //TODO: Display syncing dialog.
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            Socket connectedSocket = serverSocket.accept();

            ObjectInputStream ois = new ObjectInputStream(connectedSocket.getInputStream());
            String result = (String) ois.readObject();


            ois.close();
//            connectedSocket.close();
            serverSocket.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "MO: IOException");
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d(TAG, "MO:" + result);
//            frag.messageDisplay(result);
        }
//        frag.resetPeerSync();
    }
}
