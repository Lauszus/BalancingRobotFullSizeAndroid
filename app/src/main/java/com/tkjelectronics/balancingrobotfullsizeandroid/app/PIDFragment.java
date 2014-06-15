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


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PIDFragment extends SherlockFragment {
    private static final String TAG = "PIDFragment";
    private static final boolean D = BalancingRobotFullSizeActivity.D;

    Button mButton;
    TextView mKpView, mKiView, mKdView, mTargetAngleView, mTargetAngleText, mTargetAngleSeekBarText;
    SeekBar mKpSeekBar, mKiSeekBar, mKdSeekBar, mTargetAngleSeekBar;
    TextView mKpSeekBarValue, mKiSeekBarValue, mKdSeekBarValue, mTargetAngleSeekBarValue;

    CharSequence oldKpValue, oldKiValue, oldKdValue, oldTargetAngleValue;

    Handler mHandler = new Handler();
    int counter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pid, container, false);

        mKpView = (TextView) v.findViewById(R.id.textView1);
        mKiView = (TextView) v.findViewById(R.id.textView2);
        mKdView = (TextView) v.findViewById(R.id.textView3);
        mTargetAngleView = (TextView) v.findViewById(R.id.textView4);
        mTargetAngleText = (TextView) v.findViewById(R.id.targetAngleText);
        mTargetAngleSeekBarText = (TextView) v.findViewById(R.id.targetAngleSeekBarText);

        mKpSeekBar = (SeekBar) v.findViewById(R.id.KpSeekBar);
        mKpSeekBar.setMax(2000); // 0-20
        mKpSeekBar.setProgress(mKpSeekBar.getMax() / 2);
        mKpSeekBarValue = (TextView) v.findViewById(R.id.KpValue);
        mKpSeekBarValue.setText(String.format("%.2f", (float)mKpSeekBar.getMax() / 200.0f));

        mKpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKpSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mKiSeekBar = (SeekBar) v.findViewById(R.id.KiSeekBar);
        mKiSeekBar.setMax(2000); // 0-20
        mKiSeekBar.setProgress(mKiSeekBar.getMax() / 2);
        mKiSeekBarValue = (TextView) v.findViewById(R.id.KiValue);
        mKiSeekBarValue.setText(String.format("%.2f", (float)mKiSeekBar.getMax() / 200.0f));

        mKiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKiSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mKdSeekBar = (SeekBar) v.findViewById(R.id.KdSeekBar);
        mKdSeekBar.setMax(2000); // 0-20
        mKdSeekBar.setProgress(mKdSeekBar.getMax() / 2);
        mKdSeekBarValue = (TextView) v.findViewById(R.id.KdValue);
        mKdSeekBarValue.setText(String.format("%.2f", (float)mKdSeekBar.getMax() / 200.0f));

        mKdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mKdSeekBarValue.setText(String.format("%.2f", (float)progress / 100.0f)); // SeekBar can only handle integers, so format it to a float with two decimal places
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mTargetAngleSeekBar = (SeekBar) v.findViewById(R.id.TargetAngleSeekBar);
        mTargetAngleSeekBar.setMax(6000); // -30 to 30
        mTargetAngleSeekBar.setProgress(mTargetAngleSeekBar.getMax() / 2);
        mTargetAngleSeekBarValue = (TextView) v.findViewById(R.id.TargetAngleValue);
        mTargetAngleSeekBarValue.setText(String.format("%.2f", (float)mTargetAngleSeekBar.getMax() / 200.0f - 30.0f));

        mTargetAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                mTargetAngleSeekBarValue.setText(String.format("%.2f", ((float)progress - 30.0f * 100.0f) / 100.0f)); // It's not possible to set the minimum value either, so we will add a offset
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mButton = (Button) v.findViewById(R.id.button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((BalancingRobotFullSizeActivity) getActivity()).mChatService == null) {
                    if (D)
                        Log.e(TAG, "mChatService == null");
                    return;
                }
                if (((BalancingRobotFullSizeActivity) getActivity()).mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    if (mKpSeekBarValue.getText() != null && mKiSeekBarValue.getText() != null && mKdSeekBarValue.getText() != null && (!mKpSeekBarValue.getText().equals(oldKpValue) || !mKiSeekBarValue.getText().equals(oldKiValue) || !mKdSeekBarValue.getText().equals(oldKdValue))) {
                        oldKpValue = mKpSeekBarValue.getText();
                        oldKiValue = mKiSeekBarValue.getText();
                        oldKdValue = mKdSeekBarValue.getText();
                        mHandler.post(new Runnable() {
                            public void run() {
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.setPID((int) (Float.parseFloat(mKpSeekBarValue.getText().toString()) * 100.0f), (int) (Float.parseFloat(mKiSeekBarValue.getText().toString()) * 100.0f), (int) (Float.parseFloat(mKdSeekBarValue.getText().toString()) * 100.0f));
                            }
                        }); // Wait before sending the message
                        counter += 25;
                        mHandler.post(new Runnable() {
                            public void run() {
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.getPID();
                            }
                        }); // Wait before sending the message
                        counter += 25;
                    }

                    if (mTargetAngleSeekBarValue.getText() != null && !mTargetAngleSeekBarValue.getText().equals(oldKpValue)) {
                        oldTargetAngleValue = mTargetAngleSeekBarValue.getText();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.setTarget((int) (Float.parseFloat(mTargetAngleSeekBarValue.getText().toString()) * 100.0f) ); // The SeekBar can't handle negative numbers, do this to convert it
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.getTarget();
                            }
                        }, counter); // Wait before sending the message
                        counter += 25;
                    }
                    /*
                    if (counter != 0) {
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.getTurning();
                                ((BalancingRobotFullSizeActivity) getActivity()).mChatService.mBluetoothProtocol.getKalman();
                            }
                        }, counter); // Wait before sending the message
                        if (D)
                            Log.i(TAG, mKdSeekBar.getProgress() + " " + mKiSeekBar.getProgress() + " " + mKdSeekBar.getProgress() + " " + mTargetAngleSeekBar.getProgress());
                    }
                    */
                    counter = 0; // Reset counter
                }
            }
        });
        updateButton();
        return v;
    }

    public void updatePID(String KpValue, String KiValue, String KdValue) {
        if (mKpView != null && mKpSeekBar != null && mKpSeekBarValue != null) {
            String Kp = "";
            if (!KpValue.isEmpty())
                Kp = KpValue;
            mKpView.setText(Kp);
            if (!Kp.equals("")) {
                float value = Float.parseFloat(Kp);
                mKpSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKpSeekBar.setProgress((int) (value * 100.0f));
            }
        }
        if (mKiView != null && mKiSeekBar != null && mKiSeekBarValue != null) {
            String Ki = "";
            if (!KiValue.isEmpty())
                Ki = KiValue;
            mKiView.setText(Ki);
            if (!Ki.equals("")) {
                float value = Float.parseFloat(Ki);
                mKiSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKiSeekBar.setProgress((int) (value * 100.0f));
            }
        }
        if (mKdView != null && mKdSeekBar != null && mKdSeekBarValue != null) {
            String Kd = "";
            if (!KdValue.isEmpty())
                Kd = KdValue;
            mKdView.setText(Kd);
            if (!Kd.equals("")) {
                float value = Float.parseFloat(Kd);
                mKdSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
                mKdSeekBar.setProgress((int) (value * 100.0f));
            }
        }
    }

    public void updateAngle(String targetAngleValue) {
        if (mTargetAngleView != null && mTargetAngleSeekBar != null && mTargetAngleSeekBarValue != null && !targetAngleValue.isEmpty()) {
            float value = Float.parseFloat(targetAngleValue);
            mTargetAngleView.setText(String.format("%.2f", value));
            mTargetAngleSeekBarValue.setText(String.format("%.2f", value)); // Two decimal places
            mTargetAngleSeekBar.setProgress((int) ((value + 30) * 100.0f));
        }
    }

    public void updateButton() {
        BalancingRobotFullSizeActivity activity = ((BalancingRobotFullSizeActivity) getActivity());
        if (activity != null && activity.mChatService != null && mButton != null) {
            if (activity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
                mButton.setText(R.string.updateValues);
            else
                mButton.setText(R.string.button);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the user resumes the view, then set the values again
        BalancingRobotFullSizeActivity activity = ((BalancingRobotFullSizeActivity) getActivity());
        if (activity != null && activity.mChatService != null)
            updateButton();
    }
}
