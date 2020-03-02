package com.kassette;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

public class MakeTripDialog implements  View.OnClickListener {

    public static final String TAG = "MakeTripDialog";
    private static final int max = 9999, min = 1000;
    private Context context;
    private EditText editTextTripName, editTextTripUsername;
    private AlertDialog dialog;
    public static String tripPassword;
    private Random rand = new Random();

    public MakeTripDialog(Context context) {
        this.context = context;
    }

    /**
     * Inflate and display the dialog
     */
    public void show() {

        // Inflate the view, set title, and get instance of the EditText.
        View view = View.inflate(context, R.layout.dialog_make_trip, null);

        // Instantiate EditTexts.
        editTextTripName = (EditText) view.findViewById(R.id.editTextTripName);
        editTextTripUsername = (EditText) view.findViewById(R.id.editTextUsername);

        // Set listeners for buttons.
        view.findViewById(R.id.buttonLetsGo).setOnClickListener(this);
        view.findViewById(R.id.buttonCancel).setOnClickListener(this);

        // Get random number between 1000 - 9999 and append it to the TextViewPassword.
        tripPassword = String.valueOf(rand.nextInt(max - min + 1) + min);
        ((TextView)view.findViewById(R.id.textViewPassword)).append(tripPassword);

        // Create dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set view and create the dialog from the builder.
        builder.setView(view);
        dialog = builder.create();

        // Show dialog.
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        // Get the id of the view that was clicked and switch into the appropriate case statement.
        switch(v.getId()){
            case R.id.buttonLetsGo:
                // Get user inputs.
                String tripName = editTextTripName.getText().toString();
                String tripUsername = editTextTripUsername.getText().toString();

                // Make sure that the trip name is not empty and not more than 15 letters long.
                if(tripName.isEmpty()){
                    Toast.makeText(context, "Trip name cannot be blank", Toast.LENGTH_SHORT).show();
                    break;
                } else if(tripName.length() > 15){
                    Toast.makeText(context, "Trip name cannot be more than 15 letters", Toast.LENGTH_SHORT).show();
                    break;
                }

                // Make sure that the trip username is not empty and not more than 15 letters long.
                if(tripUsername.isEmpty()){
                    Toast.makeText(context, "Trip username cannot be blank", Toast.LENGTH_SHORT).show();
                    break;
                }else if(tripUsername.length() > 15){
                    Toast.makeText(context, "Trip username cannot be more than 15 letters", Toast.LENGTH_SHORT).show();
                    break;
                }

                // If there is user input then dismiss the dialog and pass info to MainActivity.
                dialog.dismiss();
                ((MainActivity) context).gotoFragment(PlayListFragment.TAG);
                ((MainActivity) context).createGroup(tripName, tripUsername, tripPassword);
                break;

            case R.id.buttonCancel:
                dialog.dismiss();
                break;
        }
    }
}
