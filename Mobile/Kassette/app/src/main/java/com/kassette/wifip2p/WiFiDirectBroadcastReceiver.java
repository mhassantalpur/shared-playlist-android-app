package com.kassette.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import com.kassette.MainActivity;
import com.kassette.PlayListFragment;


/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiDBroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }


    /**
     *
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

//        // Check to see if Wi-Fi is enabled and notify appropriate activity
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//
//            // UI update to indicate wifi p2p status.
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                // Wifi Direct mode is enabled
//                activity.setIsWifiP2pEnabled(true);
//            } else {
//                activity.setIsWifiP2pEnabled(false);
////                activity.resetData();
//
//            }
//
//            Log.d(TAG, "P2P state changed - " + state);

        // This if statement is hit when a p2p connection is established. Specifically via
        // the managers.connect().
         if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection info to find group owner IP.
                manager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) activity.getFragment(PlayListFragment.TAG));
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}

