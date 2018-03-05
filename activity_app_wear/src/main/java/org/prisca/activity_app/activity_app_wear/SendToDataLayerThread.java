package org.prisca.activity_app.activity_app_wear;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by kloudpix on 08/02/18.
 */

public class SendToDataLayerThread extends Thread {
    String path;
    String message;
    GoogleApiClient googleClient;


    // Constructor to send a message to the data layer
    SendToDataLayerThread(String p, String msg, GoogleApiClient gac) {
        path = p;
        message = msg;
        googleClient = gac;
    }

    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
            }
            else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send Message");
            }
        }
    }
}
