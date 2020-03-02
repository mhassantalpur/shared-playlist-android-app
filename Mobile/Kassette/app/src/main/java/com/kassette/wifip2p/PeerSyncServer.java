package com.kassette.wifip2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.kassette.PlayListFragment;
import com.kassette.SongAdapter;

public class PeerSyncServer {

    private static final String TAG = "MyServer";
    public static final int RECIEVE_IP_PORT = 8950;
    public static final int SEND_INFO_PORT = 8960;
    public static boolean isOpen = false;
    private DataDisplay listener;
    private int port;

    public interface DataDisplay { void display(InfoHolder message);}

    public void setEventListener(DataDisplay dataDisplay) {
        listener = dataDisplay;
    }

    public PeerSyncServer(int port){ this.port = port;}

    public void startListening() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    isOpen = true;
                    ServerSocket server = new ServerSocket(port);
                    Socket connectedSocket = server.accept();

                    Log.d(TAG, "MO: opening server socket");
                    Message clientmessage = Message.obtain();
                    ObjectInputStream ois = new ObjectInputStream(connectedSocket.getInputStream());

                    InfoHolder strMessage = (InfoHolder) ois.readObject();
                    clientmessage.obj = strMessage;
                    mHandler.sendMessage(clientmessage);

                    // Close everything.
                    ois.close();
                    server.close();
                    isOpen = false;
                    Log.d(TAG, "MO: closing server socket");

                    Message blah = Message.obtain();
                    blah.obj = "Reset";
                    mHandler.sendMessage(blah);

                } catch (IOException e) {
                    Log.d(TAG, "MO IOException: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "MO ClassNotFoundException: " +  e.getMessage());
                }
            }
        });

        thread.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message status) {
            if(status.obj instanceof String && status.obj.toString().equals("Reset")){
                ((PlayListFragment) listener).resetPeerSync(port);
            }else {
                listener.display((InfoHolder)status.obj);
            }
        }
    };

}