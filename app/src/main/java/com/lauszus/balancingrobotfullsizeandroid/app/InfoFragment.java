/*************************************************************************************
 * Copyright (C) 2014 Kristian Sloth Lauszus. All rights reserved.
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
 * Kristian Sloth Lauszus
 * Web      :  http://www.lauszus.com
 * e-mail   :  lauszus@gmail.com
 *
 ************************************************************************************/

package com.lauszus.balancingrobotfullsizeandroid.app;

import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.*;

import com.cardiomood.android.controls.gauge.*;

import java.util.*;

//import com.cardiomood.android.speedometer.SpeedometerView;

public class InfoFragment extends Fragment {
    private SpeedometerGauge/*View*/ mSpeed;

    TextView mCurrentDraw, mTurning, mBatteryLevel, mRunTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info, container, false);

        if (v == null)
            throw new RuntimeException();

        // Customize SpeedometerView
        mSpeed = (SpeedometerGauge/*View*/) v.findViewById(R.id.speedometer);
        mCurrentDraw = (TextView) v.findViewById(R.id.current);
        mTurning = (TextView) v.findViewById(R.id.turning);
        mBatteryLevel = (TextView) v.findViewById(R.id.battery);
        mRunTime = (TextView) v.findViewById(R.id.runTime);

        // Add label converter
        mSpeed.setLabelConverter((progress, maxProgress) -> String.valueOf((int) Math.round(progress)));

        // Configure value range and ticks
        mSpeed.setMaxSpeed(100);
        mSpeed.setMajorTickStep(10);
        mSpeed.setMinorTicks(1);

        // Configure value range colors
        mSpeed.addColoredRange(10, 50, Color.GREEN);
        mSpeed.addColoredRange(50, 80, Color.YELLOW);
        mSpeed.addColoredRange(80, 100, Color.RED);

        return v;
    }

    public void updateView(int speed, int current, int turning, int batteryLevel, long runTime) {
        // TODO: Implement this properly
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // Animation is only supported on API level >= 11
            mSpeed.setSpeed((float)speed / 100.0f + 0.001, 500, 50);
        else*/
            mSpeed.setSpeed((float)speed / 100.0f);

        if (mCurrentDraw != null) {
            String str = String.format(Locale.US, "%.2f", (float) current / 100.0f) + 'A';
            mCurrentDraw.setText(str);
        }

        if (mTurning != null)
            mTurning.setText(String.format(Locale.US, "%.2f", (float)turning / 100.0f));

        if (mBatteryLevel != null) {
            String str = String.format(Locale.US, "%.2f", (float)batteryLevel / 100.0f) + 'V';
            mBatteryLevel.setText(str);
        }

        if (mRunTime != null) { // The run time is is ms
            float runTimeFloat = (float)runTime / 60000.0f;
            String minutes = String.format(Locale.US, "%d", (int) Math.floor(runTimeFloat));
            String seconds = String.format(Locale.US, "%d", (int) (runTimeFloat % 1 / (1.0f / 60.0f)));
            String str = minutes + " min " + seconds + " sec";
            mRunTime.setText(str);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BalancingRobotFullSizeActivity activity = (BalancingRobotFullSizeActivity) getActivity();
        if (activity != null && activity.mChatService != null && activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && activity.checkTab(ViewPagerAdapter.INFO_FRAGMENT))
            activity.mChatService.mBluetoothProtocol.startInfo(); // Request data
    }
}
