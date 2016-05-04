package com.northteam.beaconsscanner.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.northteam.beaconsscanner.R;

import java.io.File;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1;
    private static final String TAG = "MainActivity";
    private static final int FILE_SELECT_CODE = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** TUTORIAL **/

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, TutorialActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        /** ------------------- **/



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentScanActivity = new Intent(MainActivity.this, BeaconsScanActivity.class);
                //intentScanActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentScanActivity);
                //finish();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Alterar texto
                builder.setTitle(R.string.needsStoragePermissions);
                builder.setMessage(R.string.storagePermissions);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_STORAGE);
                    }
                });
                builder.show();
            }
        }

        File directory = new File(Environment.getExternalStorageDirectory(), "Beacons Scanner");
        System.out.println(directory.getAbsolutePath());
        if (!directory.exists()) {
            try {
                if (directory.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory is not created");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "read storage permission granted");
                    File directory = new File(Environment.getExternalStorageDirectory(), "Beacons Scanner");
                    System.out.println(directory.getAbsolutePath());
                    if (!directory.exists()) {
                        try {
                            if (directory.mkdir()) {
                                System.out.println("Directory created");
                            } else {
                                System.out.println("Directory is not created");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.limitedPermissions);
                    // Alterar texto
                    builder.setMessage(R.string.noStoragePermissions);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scanBeacons) {

            Intent intentScanActivity = new Intent(MainActivity.this, BeaconsScanActivity.class);
            startActivity(intentScanActivity);

        } else if (id == R.id.nav_tutorial) {

            Intent intentScanActivity = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intentScanActivity);

        } else if (id == R.id.nav_share) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.setType("text/plain");
            String shareText = this.getString(R.string.shareAppText) + " " + this.getString(R.string.appLink);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            startActivity(shareIntent);

        } else if (id == R.id.nav_market) {

            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }

        } else if (id == R.id.nav_send) {

            //Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("plain/text");
            //sendIntent.setData(Uri.parse("geral@northteamsoftware.com"));
            //sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"geral@northteamsoftware.com"});
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, " - App Beacons Scanner");
            //startActivity(sendIntent);
            startActivity(Intent.createChooser(sendIntent, "Send Email"));


        } else if (id == R.id.nav_about) {

            final Dialog dialog = new Dialog(this);

            dialog.setContentView(R.layout.content_about_us);
            dialog.setCanceledOnTouchOutside(true);

            dialog.show();


        } else if (id == R.id.nav_report) {

            showFileChooser();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Beacons Scanner/");

        File directory = new File(uri.toString());
        File[] contents = directory.listFiles();

        if (!(contents.length == 0)) {

            intent.setDataAndType(uri, "*/*");

            intent.addCategory(Intent.CATEGORY_OPENABLE);

            try {
                startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        FILE_SELECT_CODE);
                System.out.println("Select");
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(this, "Please install a File Manager.",
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            LayoutInflater inflater = getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_layout));
            ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
            imgCSV.setImageResource(R.drawable.ic_folder_open_black_48dp);
            TextView text = (TextView) layout.findViewById(R.id.textToShow);
            // Set the Text to show in TextView
            text.setText(R.string.folderEmpty);

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 400);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    System.out.println(uri);
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = FileUtils.getPath(this, uri);
                    Log.d(TAG, "File Path: " + path);
                    // Get the file instance
                    File file = new File(path);
                    // Initiate the upload

                    if (file.getName().substring(file.getName().lastIndexOf(".") + 1).compareTo("csv") == 0) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("plain/text");
                        //sendIntent.setData(Uri.parse("suporte@northteamsoftware.com"));
                        //sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"suporte@northteamsoftware.com"});
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Report log");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(sendIntent, "Send Email"));
                    } else {
                        LayoutInflater inflater = getLayoutInflater();
                        // Inflate the Layout
                        View layout = inflater.inflate(R.layout.custom_toast,
                                (ViewGroup) findViewById(R.id.custom_toast_layout));
                        ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
                        imgCSV.setImageResource(R.drawable.csv_icon);
                        TextView text = (TextView) layout.findViewById(R.id.textToShow);
                        // Set the Text to show in TextView
                        text.setText(R.string.fileSelectCsv);

                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 400);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();

                    }


                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
