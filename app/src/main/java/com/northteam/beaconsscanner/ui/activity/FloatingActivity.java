package com.northteam.beaconsscanner.ui.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.northteam.beaconsscanner.R;

public class FloatingActivity extends AppCompatActivity {
    private FloatingActionMenu menuBlue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating);

        /*
        menuBlue = (FloatingActionMenu) findViewById(R.id.menu_blue);

        final FloatingActionButton programFab1 = new FloatingActionButton(this);
        programFab1.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab1.setLabelText("texto");
        programFab1.setImageResource(R.drawable.ic_image_white_24dp);
        menuBlue.addMenuButton(programFab1);
        programFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("onClick 1");
            }
        });

        final FloatingActionButton programFab2 = new FloatingActionButton(this);
        programFab2.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab2.setLabelText("texto");
        programFab2.setImageResource(R.drawable.ic_save_white_24dp);
        programFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("onClick 2");
            }
        });
        menuBlue.addMenuButton(programFab2);
*/


    }
}
