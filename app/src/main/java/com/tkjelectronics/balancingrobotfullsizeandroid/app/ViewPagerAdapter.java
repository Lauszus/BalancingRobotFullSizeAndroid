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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import java.util.Locale;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    /*public static final int IMU_FRAGMENT = 0;
    public static final int JOYSTICK_FRAGMENT = 1;
    public static final int GRAPH_FRAGMENT = 2;
    public static final int PID_FRAGMENT = 3;
    public static final int INFO_FRAGMENT = 4;*/

    public static final int PID_FRAGMENT = 1;

    Context context;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public SherlockFragment getItem(int position) {
        /*
        switch (position) {
            case 0:
                return new ImuFragment();
            case 1:
                return new JoystickFragment();
            case 2:
                return new GraphFragment();
            case 3:
                return new PIDFragment();
            case 4:
                return new InfoFragment();
            default:
                return null;
        }
        */
        switch (position) {
            case 1:
                return new PIDFragment();
            default:
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
        }
    }

    @Override
    public int getCount() {
        // Return number of tabs
        //return 5;
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return "PID";
            case 2:
                return context.getString(R.string.title_section3).toUpperCase(l);
        }
        /*
        switch (position) {
            case 0:
                return "IMU";
            case 1:
                return "Joystick";
            case 2:
                return "Graph";
            case 3:
                return "PID";
            case 4:
                return "Info";
        }
        */
        return null;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends SherlockFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_balancing_robot_full_size, container, false);
        }
    }
}