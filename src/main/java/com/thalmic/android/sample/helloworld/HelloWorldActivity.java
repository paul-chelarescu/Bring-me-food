/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;


//import com.loopj.android.http.AsyncHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class HelloWorldActivity extends Activity {

    private TextView mLockStateView;
    private TextView mTextView;

    //Global remember time stamp to not spam server
    private long globalTimeStamp;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
            globalTimeStamp = timestamp;
            //Make middle text invisible at first
            tableNumber.setVisibility(View.INVISIBLE);
            setTableNumber.setVisibility(View.INVISIBLE);
            String tableNumberString = "Table number " + tableNumberInt;
            tableNumberTitle.setText(tableNumberString);
            tableNumberTitle.setVisibility(View.VISIBLE);

        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.label_on_screen);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.label_on_screen));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.label_on_screen;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));

                    if(timestamp - globalTimeStamp  > 2500) {
                        globalTimeStamp = timestamp;
                        Toast msg = Toast.makeText(getBaseContext(), "Drink request for table "
                                        + tableNumberInt + " !",
                                Toast.LENGTH_SHORT);
                        msg.show();

                        new RequestTask().execute("http://ec2-52-17-109-1.eu-west-1.compute." +
                                "amazonaws.com:3000/api/add/" + tableNumberInt + "/drink");
                    }


























//                    HttpClient httpclient = new DefaultHttpClient();
//
//                    StatusLine statusLine = response.getStatusLine();
//                    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
//                        ByteArrayOutputStream out = new ByteArrayOutputStream();
//                        try {
//
//                            HttpResponse response = httpclient.execute(new HttpGet("http://ec2-52-17-109-1.eu-west-1.compute." +
//                                    "amazonaws.com:3000/api/add/111/drink"));
//                            response.getEntity().writeTo(out);
//                            out.close();
//                        }
//                        catch (Exception e) {
//                            System.out.println("We screwed up\n" + e);
//                        }
//                        String responseString = out.toString();
//                        //..more logic
//                    } else{
//                        //Closes the connection.
//                        response.getEntity().getContent().close();
//                        throw new IOException(statusLine.getReasonPhrase());
//                    }


//                    connectNode("http://ec2-52-17-109-1.eu-west-1.compute." +
//                            "amazonaws.com:3000/api/add/111/drink");






//                    new ConnectServer().execute("http://ec2-52-17-109-1.eu-west-1.compute." +
//                            "amazonaws.com:3000/api/add/111/drink");









//                    Thread thread = new Thread(new Runnable(){
//                        @Override
//                        public void run() {
//                            try {
//                                URL myURL = new URL("http://ec2-52-17-109-1.eu-west-1.compute." +
//                                        "amazonaws.com:3000/api/add/111/drink");
//                                URLConnection myURLConnection = myURL.openConnection();
//                                myURLConnection.connect();
//                            }
//                            catch (Exception e) {
//                                System.out.println("We screwed up\n" + e);
//                            }
//                        }
//                    });
//
//                    thread.start();







//                    try {
//                        URL url = new URL("http://ec2-52-17-109-1.eu-west-1.compute." +
//                            "amazonaws.com:3000/api/add/9003/drink");
//                        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
//                        String strTemp = "";
//                        while (null != (strTemp = br.readLine())) {
//                            System.out.println(strTemp);
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }

                    //Magic happens
//
////                    String url = "http://www.google.com/search?q=mkyong";
//                    String url = "http://ec2-52-17-109-1.eu-west-1.compute." +
//                            "amazonaws.com:3000/api/add/9003/drink";
//                    try {
//                        URL urlNode = new URL(url);
//                        HttpURLConnection connectionNode = (HttpURLConnection) urlNode.openConnection();
//
//                        // optional default is GET
//                        connectionNode.setRequestMethod("GET");
//                    }
//                    catch (Exception e) {
//                        Toast msg3 = Toast.makeText(getBaseContext(), "Connection didn't work",
//                                Toast.LENGTH_SHORT);
//                        msg3.show();
//                    }

                    //add request header
//                    connectionNode.setRequestProperty("User-Agent", USER_AGENT);

//                    int responseCode = connectionNode.getResponseCode();
//                    System.out.println("\nSending 'GET' request to URL : " + url);
//                    System.out.println("Response Code : " + responseCode);

//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(connectionNode.getInputStream()));
//                    String inputLine;
//                    StringBuffer response = new StringBuffer();
//
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();

//                    //print result
//                    System.out.println(response.toString());
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));

                    if(timestamp - globalTimeStamp > 2500) {
                        globalTimeStamp = timestamp;
                        Toast msg2 = Toast.makeText(getBaseContext(), "Food request for table "
                                        + tableNumberInt + " !",
                                Toast.LENGTH_SHORT);
                        msg2.show();
                        new RequestTask().execute("http://ec2-52-17-109-1.eu-west-1.compute." +
                                "amazonaws.com:3000/api/add/" + tableNumberInt + "/food");
                    }
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

//    private void connectNode(String urlStr) {
//        final String url = urlStr;
//
//        new Thread() {
//            public void run() {
//                try {
//                    openHttpConnection(url);
//                    bitmap = BitmapFactory.decodeStream(in);
//                    Bundle b = new Bundle();
//                    b.putParcelable("bitmap", bitmap);
//                    msg.setData(b);
//                    in.close();
//                }
//
//                catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//                messageHandler.sendMessage(msg);
//            }
//        }.start();
//    }


//    private InputStream openHttpConnection(String urlStr) {
//        InputStream in = null;
//        int resCode = -1;
//
//        try {
//            URL url = new URL(urlStr);
//            URLConnection urlConn = url.openConnection();
//
//            if (!(urlConn instanceof HttpURLConnection)) {
//                throw new IOException("URL is not an Http URL");
//            }
//            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
//            httpConn.setAllowUserInteraction(false);
//            httpConn.setInstanceFollowRedirects(true);
//            httpConn.setRequestMethod("GET");
//            httpConn.connect();
//            resCode = httpConn.getResponseCode();
//
//            if (resCode == HttpURLConnection.HTTP_OK) {
//                in = httpConn.getInputStream();
//            }
//        }
//
//        catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        return in;
//    }

//    // HTTP GET request
//    private void sendGet() throws Exception {
//
//        String url = "http://ec2-52-17-109-1.eu-west-1.compute." +
//                "amazonaws.com:3000/api/add/9003/drink";
//
//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//        // optional default is GET
//        con.setRequestMethod("GET");
//
//    }

    EditText tableNumber;
    Button setTableNumber;
    int tableNumberInt = 0;
    TextView tableNumberTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);

        tableNumber = (EditText) findViewById(R.id.tableNumber);
        setTableNumber = (Button) findViewById(R.id.button);
        tableNumberTitle = (TextView) findViewById(R.id.tableText);
        tableNumberTitle.setVisibility(View.INVISIBLE);

        setTableNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tableNumberString = "Table " + tableNumber.getText().toString() + " it is!";
                try {
                    tableNumberInt = Integer.parseInt(tableNumber.getText().toString());
                    Toast msg = Toast.makeText(getBaseContext(), tableNumberString, Toast.LENGTH_LONG);
                    msg.show();
                } catch (Exception e) {
                    Toast msg = Toast.makeText(getBaseContext(), "NUMBER ONLY!", Toast.LENGTH_LONG);
                    msg.show();
                }
            }
        });


        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //NO MORE Locking - Unlocking confusion
        Hub.getInstance().setLockingPolicy(Hub.LockingPolicy.NONE);

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
