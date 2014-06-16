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
    public static final int INFO_FRAGMENT = 0;
    public static final int PID_FRAGMENT = 1;
    public static final int MAP_FRAGMENT = 2;
    public static final int GRAPH_FRAGMENT = 3;

    Context context;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public SherlockFragment getItem(int position) {
        switch (position) {
            case INFO_FRAGMENT:
                return new InfoFragment();
            case PID_FRAGMENT:
                return new PIDFragment();
            case MAP_FRAGMENT:
                //return new MapFragment();
            case GRAPH_FRAGMENT:
                //return new GraphFragment();

                return PlaceholderFragment.newInstance(position + 1);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4; // Return number of tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case INFO_FRAGMENT:
                return "Info";
            case PID_FRAGMENT:
                return "PID";
            case MAP_FRAGMENT:
                return "Map";
            case GRAPH_FRAGMENT:
                return "Graph";
        }
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