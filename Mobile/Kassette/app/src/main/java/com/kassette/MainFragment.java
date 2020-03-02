package com.kassette;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "MainFragment";

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, null);

        // Set button onClick listener.
        view.findViewById(R.id.buttonMakeTrip).setOnClickListener(this);
        view.findViewById(R.id.buttonJoinTrip).setOnClickListener(this);
        view.findViewById(R.id.buttonDisconnect).setOnClickListener(this);

        return view;
    }


    /**
     * Handle when users click on a button by getting the Id of the view pressed. Id is an unique
     * integer representing the each individual view that is set in the xml.
     */
    @Override
    public void onClick(View v) {

        //Get the id of the view that was clicked and switch into the appropriate case statement.
        switch(v.getId()){
            case R.id.buttonMakeTrip:
                MakeTripDialog dialog = new MakeTripDialog(getActivity());
                dialog.show();
                break;

            case R.id.buttonJoinTrip:
                // Discover any peers near by, if there is then the call back will be in WiFiDirectBroadcastReceiver.
                ((MainActivity) getActivity()).gotoFragment(JoinTripFragment.TAG);
                break;

            case R.id.buttonDisconnect:
                ((MainActivity) getActivity()).disconnect();
                break;
        }
    }

}
