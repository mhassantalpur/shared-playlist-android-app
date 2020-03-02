package com.kassette;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kassette.wifip2p.DeviceInfo;


public class JoinTripFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "JoinTripActivity";
    private JoinTripDialog passwordDialog;
    private ProgressDialog progressDialog;
    private JoinTripAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_join_trip, null);

        passwordDialog = new JoinTripDialog(getActivity(), this);
        adapter = new JoinTripAdapter(getActivity(), R.layout.item_join_trip);

        ListView listview = (ListView) view.findViewById(R.id.listViewJoinTrip);
        listview.setOnItemClickListener(this);
        listview.setAdapter(adapter);

        ((MainActivity) getActivity()).discoverPeers();
        return view;
    }

    /**
     * Implements: AdapterView.OnItemClickListener
     *
     * When users click on one of the items in the ListView, then pass the device object associated
     * with it to JoinTripDialog so that it can check if the user inputed the correct password for
     * that device object.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Show password dialog and pass the device we are trying to connect.
        passwordDialog.show(adapter.getItem(position));
    }

    /**
     * Class Uses: MainActivity
     *
     * Allows MainActivity to display when looking for devices near it. Once it does then
     * addDevice() is called and it will handle closing the dialog.
     */
    public void showProgressDialog() {
        // Create progressDialog and allow it to be canceled via back button.
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Scanning. Please wait...", true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnKeyListener( new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK){
                        Log.d(TAG, "MO: in KEYCODE_BACK");
                        progressDialog.cancel();
                        ((MainActivity) getActivity()).stopDiscovery();
                        ((MainActivity) getActivity()).gotoFragment(MainFragment.TAG);
                    }
                return false;
            }
        });
    }

    /**
     * Class Uses: MainActivity
     *
     * Allows MainActivity to add devices to the JoinTripFragment's ListView. Typically MainActivty
     * will find devices around it one at time, so then add the devices to the ListView one at time.
     */
    public void addDevice(DeviceInfo device) {
        // Cancel progressDialog if its showing.
        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }
        adapter.add(device);
    }

    /**
     * Class Uses: MainActivity
     *
     * Allows MainActivity to clear the ListView when the user clicked the "Join A Trip" button to
     * make sure any other data already collected from earlier is cleared.
     */
    public void clear() {
        adapter.clear();
    }

    /**
     * Class Uses: JoinTripDialog
     *
     * Call back method from JoinTripDialog when the user inputs the correct password. Then set up
     * configuration and then pass it to MainActivity to finish the connection. Lastly close this
     * fragment and goto MainFragment.
     */
    public void correctPassword(DeviceInfo device) {

        Log.d(TAG, "MO: create config");
        // Create config file based on the device address.
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.address;
        config.wps.setup = WpsInfo.PBC;

        //TODO: Mo add progress bar
        //TODO: MO research to see if the order of the next 2 statments matter.
        // Pass config to MainActivty to connect a device. And goto PlayListFragment.
        ((MainActivity) getActivity()).gotoFragment(PlayListFragment.TAG);
        ((MainActivity) getActivity()).connect(config);
    }

    /**
     * Class Uses: JoinTripDialog
     *
     * Call back method from JoinTripDialog when the user inputs the incorrect password when trying
     * to connect a device. Notify user with Toast.
     */
    public void incorrectPassword() {
        Toast.makeText(getActivity(), "Incorrect password.", Toast.LENGTH_SHORT).show();
    }
}