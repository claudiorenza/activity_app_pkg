package org.prisca.activity_app;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

import std_msgs.String;

/**
 * Created by kloudpix on 16/01/18.
 */

public class NodeMessage extends AbstractNodeMain {
    private java.lang.String topic_name;
    private Publisher<String> publisher;

    public NodeMessage()  {
        this.topic_name = "recogniser";
    }
    
    // To be seen by RosCore
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("activity_app/beacon_recognition");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher(this.topic_name, "std_msgs/String");
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            protected void setup() {

            }

            protected void loop() throws InterruptedException {

            }
        });
    }

    @Override
    public void onShutdown(Node node) {
        //
    }

    @Override
    public void onShutdownComplete(Node node) {
        //
    }

    public void msgEntryArea()  {
        std_msgs.String str = (std_msgs.String)publisher.newMessage();
        str.setData("1");
        publisher.publish(str);
    }

    public void msgExitArea()  {
        std_msgs.String str = (std_msgs.String)publisher.newMessage();
        str.setData("0");
        publisher.publish(str);
    }
}