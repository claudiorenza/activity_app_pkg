package org.prisca.activity_app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class MainActivity extends RosActivity /*implements BeaconConsumer */{

    private NodeMessage node;
    private boolean isEntered = false;
    private boolean isApproached = false;

    protected static final String TAG = "MonitoringActivity";
    //private BeaconManager beaconManager;

    public MainActivity() {
        super("Activity App", "Activity App");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

/*
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
*/
        final Button buttonEntry = (Button) findViewById(R.id.buttonEntry);
        buttonEntry.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if (!isEntered) {
                node.msgArea("1");
                isEntered = true;
            }
            else {
                node.msgArea("0");
                isEntered = false;
            }
            }
        });

        final Button buttonApproach = (Button) findViewById(R.id.buttonApproach);
        buttonApproach.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isApproached) {
                    node.msgArea("2");
                    isApproached = true;
                }
                else {
                    node.msgArea("3");
                    isApproached = false;
                }
            }
        });
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            Log.d("Recevied Command", message);
            node.msgArea(message);

            if(message.equals("1") && !isEntered)   {
                Log.d("SEND", "ON Main Light");
                isEntered = true;
            } else if(message.equals("2") && isEntered && !isApproached) {
                Log.d("SEND", "Approach Second Light");
                isApproached = true;
            } else if(message.equals("1") && isEntered && isApproached)  {
                Log.d("SEND", "Retain Second Light");
                isApproached = false;
            } else if(message.equals("0") && isEntered) {
                Log.d("SEND", "OFF Main Light");
                isEntered = false;
            }
        }
    }


    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        node = new NodeMessage();

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().getHostAddress());
        nodeConfiguration.setMasterUri(getMasterUri());

        nodeMainExecutor.execute(node, nodeConfiguration);
    }

    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
                node.msgArea();
                isEntered = true;
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
                node.msgArea("0");
                isEntered = false;
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
    */
}
