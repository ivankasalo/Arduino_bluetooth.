package com.example.ivan.arduino_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InterfaceActivity extends Activity implements OnClickListener, View.OnTouchListener {
    private TextView logview;
    private TextView logview2;
    private Button connect, deconnect, linetracking;
    private ImageView forwardArrow, backArrow, rightArrow, leftArrow, stop;
    private BluetoothAdapter mBluetoothAdapter = null;

    private String[] logArray = null, logArray2 = null;

    private BtInterface bt = null;

    static final String TAG = "Chihuahua";
    static final int REQUEST_ENABLE_BT = 3;

    //This handler listens to messages from the bluetooth interface and adds them to the log
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            // addToLog2(data);
        }
    };

    //this handler is dedicated to the status of the bluetooth connection
    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int status = msg.arg1;
            if (status == BtInterface.CONNECTED) {
                addToLog("Connected");
            } else if (status == BtInterface.DISCONNECTED) {
                addToLog("Disconnected");
            }
        }
    };

    //handles the log view modification
    //only the most recent messages are shown
    private void addToLog(String message) {
        for (int i = 1; i < logArray.length; i++) {
            logArray[i - 1] = logArray[i];
        }
        logArray[logArray.length - 1] = message;

        logview.setText("");
        for (int i = 0; i < logArray.length; i++) {
            if (logArray[i] != null) {
                logview.append(logArray[i] + "\n");
            }
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        setUI();

    }

    //it is better to handle bluetooth connection in onResume (ie able to reset when changing screens)
    @Override
    public void onResume() {
        super.onResume();
        //first of all, we check if there is bluetooth on the phone
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.v(TAG, "Device does not support Bluetooth");
        } else {
            //Device supports BT
            if (!mBluetoothAdapter.isEnabled()) {
                //if Bluetooth not activated, then request it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BtInterface(handlerStatus, handler);
            }
        }
    }

    public void setUI() {
        logview = (TextView) findViewById(R.id.logview);
        logview2 = (TextView) findViewById(R.id.logview2);
        //I chose to display only the last 3 messages
        logArray = new String[3];
        logArray2 = new String[3];

        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(this);

        deconnect = (Button) findViewById(R.id.deconnect);
        deconnect.setOnClickListener(this);

        linetracking = (Button) findViewById(R.id.line);
        linetracking.setOnClickListener(this);

        forwardArrow = (ImageView) findViewById(R.id.forward_arrow);
        forwardArrow.setOnTouchListener(this);
        backArrow = (ImageView) findViewById(R.id.back_arrow);
        backArrow.setOnTouchListener(this);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        rightArrow.setOnTouchListener(this);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        leftArrow.setOnTouchListener(this);

    }

    //called only if the BT is not already activated, in order to activate it
    protected void onActivityResult(int requestCode, int resultCode, Intent moreData) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //BT activated, then initiate the BtInterface object to handle all BT communication
                bt = new BtInterface(handlerStatus, handler);
            } else if (resultCode == Activity.RESULT_CANCELED)
                Log.v(TAG, "BT not activated");
            else
                Log.v(TAG, "result code not known");
        } else {
            Log.v(TAG, "request code not known");
        }
    }

    //handles the clicks on various parts of the screen
    //all buttons launch a function from the BtInterface object
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect:
                addToLog("Trying to connect");
                bt.connect();
                break;
            case R.id.deconnect:
                addToLog("closing connection");
                bt.close();
                break;
            case R.id.line:
                bt.sendData('6');
                addToLog("Line tracking");
        }
    }

    @Override
    public boolean onTouch (View v, MotionEvent event){
        switch (v.getId()) {
            case R.id.forward_arrow:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addToLog("Move forward");
                    bt.sendData('1');
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    addToLog("Stopping");
                    bt.sendData('5'); //finger was lifted
                }
                break;
            case R.id.back_arrow:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addToLog("Move back");
                    bt.sendData('3');
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    addToLog("Stopping");
                    bt.sendData('5'); //finger was lifted
                }
                break;
            case R.id.right_arrow:
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    addToLog("Turn right");
                    bt.sendData('2');
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    addToLog("Stopping");
                    bt.sendData('5'); //finger was lifted
                }
                break;
            case R.id.left_arrow:
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    bt.sendData('4');
                addToLog("Turn left");
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    addToLog("Stopping");
                    bt.sendData('5'); //finger was lifted
                }
                break;
        }
        return true;
    }
}

