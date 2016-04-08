package com.northteam.beaconsscanner.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.northteam.beaconsscanner.R;


public class GroupViewHolder {

    public TextView header;

    public GroupViewHolder(View view) {
        header = (TextView) view.findViewById(R.id.header);
    }


}
