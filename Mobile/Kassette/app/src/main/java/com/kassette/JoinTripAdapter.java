package com.kassette;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.kassette.wifip2p.DeviceInfo;

public class JoinTripAdapter extends ArrayAdapter<DeviceInfo> {


    public JoinTripAdapter(Context context, int resource) {
        super(context, resource);
    }


    // Declare ViewHolder to hold reference to TextView label
    static class ViewHolder {
        TextView label;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        // Check to see if we've already inflated the view
        if (v == null) {

            // Inflate the view from the XML layout
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_join_trip, null);

            // ViewHolder Pattern: create the static holder to keep a reference to the label TextView
            holder = new ViewHolder();
            holder.label = (TextView) v.findViewById(R.id.textViewTitle);

            v.setTag(holder);

        } else {
            holder = (ViewHolder) v.getTag();
        }

        // Get the title string for this row
        String title = this.getItem(position).tripName;

        // Assign the title to the TextView
        holder.label.setText(title);

        return v;
    }

}