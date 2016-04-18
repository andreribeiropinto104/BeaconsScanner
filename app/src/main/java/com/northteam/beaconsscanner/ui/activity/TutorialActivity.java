package com.northteam.beaconsscanner.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.northteam.beaconsscanner.R;

public class TutorialActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(AppIntroFragment.newInstance("Beacons Scanner", this.getString(R.string.tutorialMain), R.drawable.ic_help_outline_white_48dp, Color.parseColor("#00b8ff")));
        addSlide(AppIntroFragment.newInstance(this.getString(R.string.tutorialScan), this.getString(R.string.tutorialApp), R.drawable.ic_bluetooth_searching_white_48dp, Color.parseColor("#0088cc")));
        addSlide(AppIntroFragment.newInstance(this.getString(R.string.tuturialSaveLogs), this.getString(R.string.tutorialLogs), R.drawable.ic_insert_drive_file_white_48dp, Color.parseColor("#00b8ff")));
        addSlide(AppIntroFragment.newInstance(this.getString(R.string.tutorialFolderApp), this.getString(R.string.tutorialFolder), R.drawable.ic_folder_open_white_48dp, Color.parseColor("#0088cc")));
        addSlide(AppIntroFragment.newInstance(this.getString(R.string.tutorialfileCsv), this.getString(R.string.tutorialSaveFile), R.drawable.csv_icon_white, Color.parseColor("#00b8ff")));

        // OPTIONAL METHODS

        // SHOW or HIDE the statusbar
        showStatusBar(false);

        // Edit the color of the nav bar on Lollipop+ devices
        //setNavBarColor(Color.parseColor("#3F51B5"))

        // Turn vibration on and set intensity
        // NOTE: you will need to ask VIBRATE permission in Manifest if you haven't already
        //setVibrate(true);
        //setVibrateIntensity(30);

        // Animations -- use only one of the below. Using both could cause errors.
        //setFadeAnimation(); // OR
        //setZoomAnimation(); // OR
        //setFlowAnimation(); // OR
        //setSlideOverAnimation(); // OR
        setDepthAnimation(); // OR
        //setCustomTransformer(yourCustomTransformer);

        // Permissions -- takes a permission and slide number
        //askForPermissions(new String[]{Manifest.permission.CAMERA}, 3);
    }


    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onSlideChanged() {
        // Do something when slide is changed
    }
}


