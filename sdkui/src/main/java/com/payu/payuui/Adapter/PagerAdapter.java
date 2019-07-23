package com.payu.payuui.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Fragment.GenericUpiIntentFragment;
import com.payu.payuui.Fragment.PhonePeFragment;
import com.payu.payuui.Fragment.StandAlonePhonePeFragment;
import com.payu.payuui.Fragment.TEZFragment;
import com.payu.payuui.Fragment.UPIFragment;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piyush on 29/7/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mTitles;
    private PayuResponse payuResponse;
    private PayuResponse valueAddedResponse;
    private HashMap<String, String> oneClickCardTokens;
    private HashMap<Integer, Fragment> mPageReference = new HashMap<Integer, Fragment>();

    public PagerAdapter(FragmentManager fragmentManager, ArrayList<String> titles, PayuResponse payuResponse, PayuResponse valueAddedResponse, HashMap<String, String> oneClickCardTokens) {
        super(fragmentManager);
        this.mTitles = titles;
        this.payuResponse = payuResponse;
        this.valueAddedResponse = valueAddedResponse;
        this.oneClickCardTokens = oneClickCardTokens;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (mTitles.get(i)){





            case SdkUIConstants.UPI:
                fragment = new UPIFragment();
                bundle.putParcelableArrayList(PayuConstants.NETBANKING, payuResponse.getNetBanks());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getNetBankingDownStatus());
                fragment.setArguments(bundle);
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.TEZ:
                fragment = new TEZFragment();
                mPageReference.put(i, fragment);
                return fragment;

            case SdkUIConstants.GENERICINTENT:
                fragment = new GenericUpiIntentFragment();
                mPageReference.put(i, fragment);
                return fragment;



            case SdkUIConstants.PHONEPE:
                fragment = new StandAlonePhonePeFragment();
                mPageReference.put(i,fragment);
                return fragment;

            case SdkUIConstants.CBPHONEPE:
                fragment = new PhonePeFragment();
                mPageReference.put(i,fragment);
                return fragment;



            default:
                return null;
        }
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

    public Fragment getFragment(int key){
        return mPageReference.get(key);
    }


}
