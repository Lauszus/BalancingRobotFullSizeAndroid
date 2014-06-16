/*************************************************************************************
 * Copyright (C) 2012-2014 Kristian Lauszus, TKJ Electronics. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Lauszus, TKJ Electronics
 * Web      :  http://www.tkjelectronics.com
 * e-mail   :  kristianl@tkjelectronics.com
 *
 ************************************************************************************/

package com.tkjelectronics.balancingrobotfullsizeandroid.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.lang.ref.WeakReference;

public class BalancingRobotFullSizeActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "BalancingRobotFullSizeActivity";
    public static final boolean D = BuildConfig.DEBUG; // This is automatically set when building

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_DISCONNECTED = 4;
    public static final int MESSAGE_RETRY = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String KP_VALUE = "kp_value";
    public static final String KI_VALUE = "ki_value";
    public static final String KD_VALUE = "kd_value";
    public static final String TARGET_ANGLE = "target_angle";
    public static final String TURNING_SCALE = "turning_scale";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private final BluetoothHandler mBluetoothHandler = new BluetoothHandler(this);
    // Member object for the chat services
    public BluetoothChatService mChatService = null;

    BluetoothDevice btDevice; // The BluetoothDevice object
    boolean btSecure; // If it's a new device we will pair with the device
    public static boolean stopRetrying;

    private Toast mToast;

    /** The {@link UnderlinePageIndicator} that will host the section contents. */
    UnderlinePageIndicator mUnderlinePageIndicator;

    ViewPagerAdapter mViewPagerAdapter;
    CustomViewPager mViewPager;

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balancing_robot_full_size);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Get local Bluetooth adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        else
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            showToast("Bluetooth is not available", Toast.LENGTH_LONG);
            finish();
            return;
        }

        // Create the adapter that will return a fragment for each of the primary sections of the app.
        mViewPagerAdapter = new ViewPagerAdapter(getApplicationContext(), getSupportFragmentManager());

        // Set up the ViewPager with the adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);

        // Bind the underline indicator to the adapter
        mUnderlinePageIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        mUnderlinePageIndicator.setViewPager(mViewPager);
        mUnderlinePageIndicator.setFades(false);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mUnderlinePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (D)
                    Log.d(TAG, "ViewPager position: " + position);
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                            .setText(mViewPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    public void showToast(String message, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null)
            mToast.cancel(); // Close the toast if it's already open
        mToast = Toast.makeText(this, message, duration);
        mToast.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.d(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            if (D)
                Log.d(TAG, "Request enable BT");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else
            setupBTService(); // Otherwise, setup the chat session
    }

    @Override
    public void onBackPressed() {
        if (mChatService != null) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    mChatService.stop(); // Stop the Bluetooth chat services if the user exits the app
                }
            }, 1000); // Wait 1 second before closing the connection, this is needed as onPause() will send stop messages before closing
        }
        finish(); // Exits the app
    }

    private void setupBTService() {
        if (mChatService != null)
            return;

        if (D)
            Log.d(TAG, "setupBTService()");
        mChatService = new BluetoothChatService(mBluetoothHandler, mBluetoothAdapter); // Initialize the BluetoothChatService to perform Bluetooth connections
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (D)
            Log.d(TAG, "onPrepareOptionsMenu");
        MenuItem menuItem = menu.findItem(R.id.action_connect); // Find item
        if (mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
            menuItem.setIcon(R.drawable.device_access_bluetooth_connected);
        else
            menuItem.setIcon(R.drawable.device_access_bluetooth);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.balancing_robot_full_size, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_connect:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.action_settings:
                // TODO: Open up the settings dialog
                //return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mUnderlinePageIndicator.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect to
                if (resultCode == Activity.RESULT_OK)
                    connectDevice(data, false);
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                    setupBTService(); // Bluetooth is now enabled, so set up a chat session
                else {
                    // User did not enable Bluetooth or an error occurred
                    if (D)
                        Log.d(TAG, "BT not enabled");
                    showToast(getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_SHORT);
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean retry) {
        if (retry) {
            if (btDevice != null && !stopRetrying) {
                mChatService.start(); // This will stop all the running threads
                mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            }
        } else { // It's a new connection
            stopRetrying = false;
            mChatService.newConnection = true;
            mChatService.start(); // This will stop all the running threads
            if (data.getExtras() == null)
                return;
            String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device Bluetooth address
            btSecure = data.getExtras().getBoolean(DeviceListActivity.EXTRA_NEW_DEVICE); // If it's a new device we will pair with the device
            btDevice = mBluetoothAdapter.getRemoteDevice(address); // Get the BluetoothDevice object
            mChatService.nRetries = 0; // Reset retry counter
            mChatService.connect(btDevice, btSecure); // Attempt to connect to the device
            showToast(getString(R.string.connecting), Toast.LENGTH_SHORT);
        }
    }

    public SherlockFragment getFragment(int item) {
        return (SherlockFragment)mViewPagerAdapter.instantiateItem(mViewPager, item);
    }

    // The Handler class that gets information back from the BluetoothChatService
    private static class BluetoothHandler extends Handler {
        private final WeakReference<BalancingRobotFullSizeActivity> mActivity; // See: http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
        PIDFragment pidFragment;
        private String mConnectedDeviceName; // Name of the connected device

        BluetoothHandler(BalancingRobotFullSizeActivity activity) {
            mActivity  = new WeakReference<BalancingRobotFullSizeActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BalancingRobotFullSizeActivity mBalancingRobotFullSizeActivity = mActivity.get();
            if (mBalancingRobotFullSizeActivity == null)
                return;
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    mBalancingRobotFullSizeActivity.supportInvalidateOptionsMenu();
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            mBalancingRobotFullSizeActivity.showToast(mBalancingRobotFullSizeActivity.getString(R.string.connected_to) + " " + mConnectedDeviceName, Toast.LENGTH_SHORT);
                            if (mBalancingRobotFullSizeActivity.mChatService == null)
                                return;
                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    BalancingRobotFullSizeActivity mBalancingRobotFullSizeActivity = mActivity.get();
                                    if (mBalancingRobotFullSizeActivity != null) {
                                        mBalancingRobotFullSizeActivity.mChatService.mBluetoothProtocol.getPID();
                                        mBalancingRobotFullSizeActivity.mChatService.mBluetoothProtocol.getTarget();
                                        mBalancingRobotFullSizeActivity.mChatService.mBluetoothProtocol.getTurning();
                                        mBalancingRobotFullSizeActivity.mChatService.mBluetoothProtocol.getKalman();
                                    }
                                }
                            }, 1000); // Wait 1 second before sending the message

                            /*mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    mChatService.write(getPIDValues + getEncoderValues + getSettings + getInfo + getKalman);
                                }
                            }, 1000); // Wait 1 second before sending the message
                            if (GraphFragment.mToggleButton != null) {
                                if (GraphFragment.mToggleButton.isChecked() && checkTab(ViewPagerAdapter.GRAPH_FRAGMENT)) {
                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            mChatService.write(imuBegin); // Request data
                                        }
                                    }, 1000); // Wait 1 second before sending the message
                                } else {
                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            mChatService.write(imuStop); // Stop sending data
                                        }
                                    }, 1000); // Wait 1 second before sending the message
                                }
                            }
                            if (checkTab(ViewPagerAdapter.INFO_FRAGMENT)) {
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        mChatService.write(statusBegin); // Request data
                                    }
                                }, 1000); // Wait 1 second before sending the message
                            }*/
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                    }
                    pidFragment = (PIDFragment) mBalancingRobotFullSizeActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateButton();
                    break;

                case MESSAGE_READ:
                    Bundle data = msg.getData();
                    if (data != null) {
                        pidFragment = (PIDFragment) mBalancingRobotFullSizeActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                        if (pidFragment == null)
                            return;
                        if (data.containsKey(KP_VALUE) && data.containsKey(KI_VALUE) && data.containsKey(KD_VALUE))
                            pidFragment.updatePID(data.getString(KP_VALUE), data.getString(KI_VALUE), data.getString(KD_VALUE));
                        else if (data.containsKey(TARGET_ANGLE))
                            pidFragment.updateAngle(data.getString(TARGET_ANGLE));
                        else if (data.containsKey(TURNING_SCALE))
                            pidFragment.updateTurning(data.getInt(TURNING_SCALE));
                    }
                    /*
                    if (newInfo || newStatus) {
                        newInfo = false;
                        newStatus = false;
                        InfoFragment.updateView();
                    }
                    if (newIMUValues) {
                        newIMUValues = false;
                        GraphFragment.updateIMUValues();
                    }
                    if (newKalmanValues) {
                        newKalmanValues = false;
                        GraphFragment.updateKalmanValues();
                    }
                    if (pairingWithDevice) {
                        pairingWithDevice = false;
                        BalanduinoActivity.showToast("Now enable discovery of your device", Toast.LENGTH_LONG);
                    }
                    */
                    break;
                case MESSAGE_DEVICE_NAME:
                    // Save the connected device's name
                    if (msg.getData() != null)
                        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                case MESSAGE_DISCONNECTED:
                    mBalancingRobotFullSizeActivity.supportInvalidateOptionsMenu();
                    pidFragment = (PIDFragment) mBalancingRobotFullSizeActivity.getFragment(ViewPagerAdapter.PID_FRAGMENT);
                    if (pidFragment != null)
                        pidFragment.updateButton();
                    if (msg.getData() != null)
                        mBalancingRobotFullSizeActivity.showToast(msg.getData().getString(TOAST), Toast.LENGTH_SHORT);
                    break;
                case MESSAGE_RETRY:
                    if (D)
                        Log.d(TAG, "MESSAGE_RETRY");
                    mBalancingRobotFullSizeActivity.connectDevice(null, true);
                    break;
            }
        }
    }
}