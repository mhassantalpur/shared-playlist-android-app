package com.kassette;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kassette.wifip2p.DeviceInfo;

public class JoinTripDialog implements OnClickListener{

    private static final String TAG = "JoinTripDialog";
    private Context context;
    private DeviceInfo device;
    private JoinTripFragment joinTripFrag;
    private EditText editTextPassword;

    public JoinTripDialog(Context context, JoinTripFragment joinTripFrag){
        this.context = context;
        this.joinTripFrag = joinTripFrag;
    }

    /**
     * Inflate and display the dialog
     */
    public void show(DeviceInfo device){

        // Save device to be used to check if the password th user entered is correct.
        this.device = device;

        // Inflate the view, set title, and get instance of the EditText.
        View view = View.inflate(context, R.layout.dialog_join_trip, null);
        ((TextView)view.findViewById(R.id.textViewJoinTripName)).setText("Test dialog");
        editTextPassword = (EditText) view.findViewById(R.id.editTextJoinTripPassword);

        // Create dialog.
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        // Set view, button names, and onClick listen.
        alert.setView(view);
        alert.setPositiveButton("Submit", this);
        alert.setNegativeButton("Cancel", this);

        // Show dialog.
        alert.show();
    }

    /**
     * Implements: OnClickListener
     *
     * Handle when users click on a button by getting the portion of that button in the dialog.
     */
    @Override
    public void onClick(DialogInterface dialog, int pos) {

//        joinTripFrag.correctPassword(device);

        //Switch into the appropriate case statement.
        switch(pos){
            case Dialog.BUTTON_POSITIVE:
                String input = editTextPassword.getText().toString();
                if(input.equals(device.password)){
                    joinTripFrag.correctPassword(device);
                } else{
                    joinTripFrag.incorrectPassword();
                }
                break;

            case Dialog.BUTTON_NEGATIVE:
                break;
        }
    }

}
