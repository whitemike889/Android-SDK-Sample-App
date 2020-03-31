package com.payu.payuui.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.payu.payuui.Fragment.TEZFragment;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piyush on 29/7/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mTitles;
    private HashMap<Integer, Fragment> mPageReference = new HashMap<Integer, Fragment>();

    public PagerAdapter(FragmentManager fragmentManager, ArrayList<String> titles) {
        super(fragmentManager);
        this.mTitles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if(SdkUIConstants.TEZ.equalsIgnoreCase(mTitles.get(i))){
            fragment = new TEZFragment();
            mPageReference.put(i, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        if(mTitles != null)
            return mTitles.size();
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }


}
