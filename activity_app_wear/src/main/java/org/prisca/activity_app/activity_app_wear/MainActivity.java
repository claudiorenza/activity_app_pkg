package org.prisca.activity_app.activity_app_wear;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends WearableActivity implements BeaconConsumer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    GoogleApiClient googleClient;

    protected static final String TAG = "MonitoringActivity_wear";
    private BeaconManager beaconManager;
    private Button buttonEntry;
    private Button buttonApproach;

    private Boolean isEntered;
    private Boolean isApproached;

    private Map<String, Double> beaconsCompare = new HashMap<String, Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        buttonEntry = findViewById(R.id.buttonEntry);
        buttonEntry.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTON", "Entry");
                String message = "1";
                //Requires a new thread to avoid blocking the UI
                new SendToDataLayerThread("/message_path", message, googleClient).start();
            }
        });

        buttonApproach = findViewById(R.id.buttonApproach);
        buttonApproach.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTON", "Approach");
                String message = "2";
                //Requires a new thread to avoid blocking the UI
                new SendToDataLayerThread("/message_path", message, googleClient).start();
            }
        });

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Enables Always-on
        setAmbientEnabled();

        //Region region1 = new Region("myIdentifier1", "B4:99:4C:70:C3:C2");
        //Region region2 = new Region("myIdentifier2", "B4:99:4C:70:C3:D1");

        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers()
                .add(new BeaconParser()
                        .setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
/*
        try {
            beaconManager.startMonitoringBeaconsInRegion(region1);
            beaconManager.startMonitoringBeaconsInRegion(region2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
*/
        beaconManager.bind(this);
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                    recognitionArea(beacons.iterator().next());
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("com.beacon.lampOne", "B4:99:4C:70:C3:C2"));
            beaconManager.startRangingBeaconsInRegion(new Region("com.beacon.lampTwo", "B4:99:4C:70:C3:D1"));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

/*
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                //buttonEntry.setText("Beacon Area");
                Log.i(TAG, "I just saw an beacon for the first time!");
                String message = "Hello wearable\n Via the data layer";
                //Requires a new thread to avoid blocking the UI
                new SendToDataLayerThread("/message_path", message, googleClient).start();
            }

            @Override
            public void didExitRegion(Region region) {
                //buttonEntry.setText("SEND");
                Log.i(TAG, "I no longer see an beacon");
                String message = "Hello wearable\n Via the data layer";
                //Requires a new thread to avoid blocking the UI
                new SendToDataLayerThread("/message_path", message, googleClient).start();
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    }
        */
    }

    public void recognitionArea(Beacon beacon) {
        Log.i(TAG,  "ADDRESS: " + beacon.getBluetoothAddress() + " - Distance: " + beacon.getDistance());

        beaconsCompare.put(beacon.getBluetoothAddress() , beacon.getDistance());
        Log.i("SIZE", String.valueOf(beaconsCompare.size()));

        if (beaconsCompare.size() > 1) {
            if(!isEntered && !isApproached) {//se la luce principale non è accesa
                if(beaconsCompare.get("B4:99:4C:70:C3:D1") < 2.0) { //se mi trovo a meno di 2 metri di distanza
                    Log.i("MESSAGE", "1 - Accendo la luce Principale");
                    new SendToDataLayerThread("/message_path","1", googleClient).start();
                    isEntered = true;   //segnalo che sono entrato nella stanza
                }
            } else  {   //se la luce principale è accesa
                if(beaconsCompare.get("B4:99:4C:70:C3:C2") < 1.0 && !isApproached) {
                    Log.i("MESSAGE", "2 - Spengo la luce Principale e accendo la luce Scrivania");
                    new SendToDataLayerThread("/message_path", "2", googleClient).start();
                    isApproached = true; //segnalo che mi sono avvicinato
                } else if(beaconsCompare.get("B4:99:4C:70:C3:C2") > 1.0 && isApproached) {
                    Log.i("MESSAGE", "3 - Accendo la luce Principale e spengo la luce Scrivania");
                    new SendToDataLayerThread("/message_path", "3", googleClient).start();
                    isApproached = false; //segnalo che mi sono avvicinato
                } else if(beaconsCompare.get("B4:99:4C:70:C3:D1") > 2.0 && beaconsCompare.get("B4:99:4C:70:C3:C2") > 4.0 && !isApproached)  {
                    Log.i("MESSAGE", "0 - Spengo la luce Principale");
                    new SendToDataLayerThread("/message_path", "0", googleClient).start();
                    isEntered = false; //segnalo che mi sono uscito dalla stanza
                }
            }


            /*
            if(beaconsCompare.get("B4:99:4C:70:C3:D1") < 3.0) {
                if (beaconsCompare.get("B4:99:4C:70:C3:D1") < beaconsCompare.get("B4:99:4C:70:C3:C2")) {
                    Log.i("MESSAGE", "1");
                    String message = "1";
                    new SendToDataLayerThread("/message_path", message, googleClient).start();
                } else {
                    Log.i("MESSAGE", "2");
                    String message = "2";
                    new SendToDataLayerThread("/message_path", message, googleClient).start();
                }
            } else {
                Log.i("MESSAGE", "0");
                String message = "0";
                new SendToDataLayerThread("/message_path", message, googleClient).start();
            }
            */

            beaconsCompare.clear();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {
        //String message = "Hello wearable\n Via the data layer";
        //Requires a new thread to avoid blocking the UI
        //new SendToDataLayerThread("/message_path", message, googleClient).start();
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

}
