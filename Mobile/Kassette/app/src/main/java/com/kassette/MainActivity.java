package com.kassette;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.kassette.wifip2p.DeviceInfo;
import com.kassette.wifip2p.WiFiDirectBroadcastReceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements WifiP2pManager.ChannelListener {

    public static final String TAG = "MainActivity";

    public static SpotifyManager spotifyManager;

    // Used to set up p2p connection.
    private WifiP2pManager manager;
    private boolean retryChannel = false;

    // Used when trying to send data via p2p.
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    //
    private final ArrayList<DeviceInfo> deviceList = new ArrayList<>();

    // All fragments we will be using.
    private MainFragment mainFrag = new MainFragment();
    private JoinTripFragment joinTripFrag;
    private PlayListFragment playListFrag;
    private SearchFragment searchFragment;

    // Used to handle all of the action listeners.
    private TaggedActionListener tagActionLister;

    // Used to check if wifi is on or to reset wifi.
    private WifiManager wifiManager;

    public static boolean inGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.banner);

        spotifyManager = new SpotifyManager(getApplicationContext(),this);

        // Add necessary intent values to be matched in the IntentFilter.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Initialise manager, channel, and listView.
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        // Initialise MainFragment and add it to MainActivity.
        getSupportFragmentManager().beginTransaction().add(R.id.center_container_root, mainFrag).commit();

        // Set up wifi manger and notify user to turn it on if its off.
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Please enable wifi", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the BroadcastReceiver with the intent values to be matched
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        // If was in PlayListFragment and either pressed back button or home button, then go back to it.
        if (inGroup) {
            // If pressed back button, then playListFrag would be null.
            if (playListFrag == null) {
                playListFrag = new PlayListFragment();
            }
            // Press back button.
            if (mainFrag.isAdded()) {
                getSupportFragmentManager().beginTransaction().detach(mainFrag).add(R.id.full_container_root, playListFrag).commit();

            // Pressed home button.
            } else if (!playListFrag.isAdded()) {
                getSupportFragmentManager().beginTransaction().add(R.id.full_container_root, playListFrag).commit();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the BroadcastReceiver
        unregisterReceiver(receiver);
//        manager.clearLocalServices(channel,
//        manager.clearServiceRequests(channel,

    }


    /**
     * Class uses: JoinTripFragment
     *
     * Used then canceled search for nearby hosts.
     */
    public void stopDiscovery() {
        tagActionLister = new TaggedActionListener("clearServiceRequests", "clearServiceRequests success", "clearServiceRequests fail");
        manager.clearServiceRequests(channel, tagActionLister);

        // Reset wifi by turing it on and off.
//        wifiManager.setWifiEnabled(false);
//        wifiManager.setWifiEnabled(true);
    }

    /**
     * Implements: WifiP2pManager.ChannelListener
     *
     * Call back method for channel, where if it gets disconnect then handled it here.
     */
    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(MainActivity.this, "Channel lost. Trying again", Toast.LENGTH_SHORT).show();
//            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            String msg = "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Class Uses: MakeTripDialog
     *
     * Use info to make a trip via local service.
     */
    public void createGroup(String tripName, String tripUsername, String tripPassword) {
        //  Create a string map containing   information about your service.
        Map record = new HashMap();
        record.put("tripname", tripName);
        record.put("username", tripUsername);
        record.put("password", tripPassword);

        // Service information.  Pass it an instance name, service type _protocol._transportlayer ,
        // and the map containing information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_motesting", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel, and listener that will
        // be used to indicate success or failure of the request.
        tagActionLister = new TaggedActionListener("addLocalService");
//        tagActionLister = new TaggedActionListener("addLocalService", "addLocalService success", "addLocalService fail");
        manager.addLocalService(channel, serviceInfo, tagActionLister);

        tagActionLister = new TaggedActionListener("createGroup", "Group Created", "");
        manager.createGroup(channel, tagActionLister);

    }

    /**
     * State: WIFI_P2P_PEERS_CHANGED_ACTION
     *
     * Call back from MainFragment to make the phone discoverable and find peers.
     */
    public void discoverPeers() {
        // Get the fragment to notify it if trips are discovered and display the progressDialog.
        joinTripFrag.showProgressDialog();
        joinTripFrag.clear();

        // Clear any devices we found previously
        deviceList.clear();

        // Get all devices info from local services around the phone.
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {

            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());

                // Add each device one at a time to deviceList.
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.address = device.deviceAddress;
                deviceInfo.tripName = (String) record.get("tripname");
                deviceInfo.username = (String) record.get("username");
                deviceInfo.password = (String) record.get("password");
                deviceList.add(deviceInfo);
            }
        };

        // Called after finding all devices in local service.
        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {

                // Go through each device one at a time and see if  //TODO: need to remeber why i did this mo.
                for (int i = 0; i < deviceList.size(); i++) {
                    if (deviceList.get(i).address.equals(resourceType.deviceAddress)) {
                        DeviceInfo deviceInfo = deviceList.get(i);

                        // Add to the custom adapter defined specifically for showing wifi devices.
                        joinTripFrag.addDevice(deviceInfo);
                        Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
                    }
                }
            }
        };

        // Set listeners.
        manager.setDnsSdResponseListeners(channel, servListener, txtListener);
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        tagActionLister = new TaggedActionListener("addServiceRequest");
        manager.addServiceRequest(channel, serviceRequest, tagActionLister);

        tagActionLister = new TaggedActionListener("discoverServices");
        manager.discoverServices(channel, tagActionLister);
    }


    /**
     * State: WIFI_P2P_CONNECTION_CHANGED_ACTION
     *
     * Call back from JoinTripActivity to connect you device to a group.
     */
    public void connect(WifiP2pConfig config) {
        tagActionLister = new TaggedActionListener("connect", "Connected to a Trip.", "Connect failed. Retry.");
        manager.connect(channel, config, tagActionLister);
    }


    /**
     * Class uses: MainFragment
     *
     * Used to remove this device from the group.
     */
    public void disconnect() {
        tagActionLister = new TaggedActionListener("removeGroup", "Disconnected", "Disconnect failed.");
        manager.removeGroup(channel, tagActionLister);
    }


    public Fragment getFragment(String name) {
        if (name.equals(MainFragment.TAG)) {
            return mainFrag;

        } else if (name.equals(PlayListFragment.TAG)) {
            return playListFrag;
        }
        return null;
    }

    public void gotoFragment(String name) {

        if (name.equals(JoinTripFragment.TAG)) {
            if (joinTripFrag == null) {
                joinTripFrag = new JoinTripFragment();
                getSupportFragmentManager().beginTransaction().detach(mainFrag).add(R.id.full_container_root, joinTripFrag).commit();
            } else {
                getSupportFragmentManager().beginTransaction().detach(mainFrag).attach(joinTripFrag).commit();
            }


        } else if (name.equals(MainFragment.TAG)) {
//            getSupportFragmentManager().beginTransaction().attach(mainFrag).commit();

            getSupportFragmentManager().beginTransaction().detach(joinTripFrag).attach(mainFrag).commit();

        // Exit button from PlayListFragment
        }else if(name.equals(MainFragment.TAG + "1")){
            getSupportFragmentManager().beginTransaction().detach(playListFrag).attach(mainFrag).commit();

        } else if (name.equals(PlayListFragment.TAG)) {
            if (playListFrag == null) {
                playListFrag = new PlayListFragment();
                if (mainFrag.isAdded()) {
                    getSupportFragmentManager().beginTransaction().detach(mainFrag).add(R.id.full_container_root, playListFrag).commit();
                } else if (joinTripFrag.isAdded()) {
                    getSupportFragmentManager().beginTransaction().detach(joinTripFrag).add(R.id.full_container_root, playListFrag).commit();
                }
            }
            else {
                if(playListFrag.isHidden()){
                    getSupportFragmentManager().beginTransaction().detach(searchFragment).show(playListFrag).commit();
                }
                else if (mainFrag.isAdded()) {
                    getSupportFragmentManager().beginTransaction().detach(mainFrag).attach(playListFrag).commit();
                }
                else if(searchFragment.isAdded()){
                    getSupportFragmentManager().beginTransaction().detach(searchFragment).attach(playListFrag).commit();
                }
                else if (joinTripFrag.isAdded()) {
                    getSupportFragmentManager().beginTransaction().detach(joinTripFrag).attach(playListFrag).commit();
                }

            }
        } else if (name.equals(SearchFragment.TAG)) {
            if(searchFragment == null) {
                searchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction().hide(playListFrag).add(R.id.full_container_root,searchFragment).commit();
            }
            else {
                getSupportFragmentManager().beginTransaction().hide(playListFrag).attach(searchFragment).commit();
            }

        }
    }


    /**
     * Private class that is used to simply call backs from manager.
     */
    private class TaggedActionListener implements WifiP2pManager.ActionListener {

        private final String tag, success, fail;
        private boolean toToast = true;

        public TaggedActionListener(String tag) {
            this.tag = tag;
            this.success = "";
            this.fail = "";
            this.toToast = false;
        }

        public TaggedActionListener(String tag, String success, String fail) {
            this.tag = tag;
            this.success = success;
            this.fail = fail;
        }

        @Override
        public void onSuccess() {
            Log.d(TAG, tag + " " + success);
            if (toToast) {
                Toast.makeText(MainActivity.this, success, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, tag + " error " + reason);
            if (toToast) {
                Toast.makeText(MainActivity.this, fail, Toast.LENGTH_SHORT).show();
            }

            if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                Log.e(TAG, "Wifi P2P not supported");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        Log.d(TAG, "requestCode - " + requestCode + " resultCode - " + requestCode);
//        spotifyManager.spotifyResponse(requestCode, resultCode, intent);
        playListFrag.onActivityResult(requestCode, resultCode, intent);
    }

}
