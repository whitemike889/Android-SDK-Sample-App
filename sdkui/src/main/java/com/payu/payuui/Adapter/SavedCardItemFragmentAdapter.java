package com.payu.payuui.Adapter;

/**
 * Created by ankur on 8/24/15.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.payu.india.Model.CardStatus;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Fragment.SavedCardItemFragment;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class SavedCardItemFragmentAdapter extends FragmentStatePagerAdapter {

    ArrayList<StoredCard> mStoredCards;
    SavedCardItemFragment mSavedCardItemFragment;
    HashMap<String, CardStatus> mValueAddedHashMap;
    Bundle mBundle;
    String bankStatus = "";
    FragmentManager mFragmentManager;

    public SavedCardItemFragmentAdapter(FragmentManager fm , ArrayList<StoredCard> storedCards, HashMap<String, CardStatus> valueAddedHashMap) {
        super(fm);
        mFragmentManager = fm;
        mStoredCards = null;
        mStoredCards = storedCards;
        mValueAddedHashMap = valueAddedHashMap = (HashMap<String, CardStatus>) valueAddedHashMap;
    }

    @Override
    public Fragment getItem(int position) {
        mBundle = new Bundle();
        mBundle.putParcelable(PayuConstants.STORED_CARD, mStoredCards.get(position));
        if(mValueAddedHashMap.get(mStoredCards.get(position).getCardBin()) != null && mValueAddedHashMap.get(mStoredCards.get(position).getCardBin()).getStatusCode() == 0){
            bankStatus = mStoredCards.get(position).getIssuingBank()+" is temporarily down";
        }else {
            bankStatus = "";
        }
        mBundle.putString(SdkUIConstants.ISSUING_BANK_STATUS, bankStatus);
        mSavedCardItemFragment = new SavedCardItemFragment();
        mSavedCardItemFragment.setArguments(mBundle);
        return mSavedCardItemFragment;
    }

    @Override
    public int getCount() {
        if(null != mStoredCards) return mStoredCards.size();
        return 0;
    }

//    public void updateAdapter(ArrayList<StoredCard> storedCard){
//        mStoredCards = null;
//        mStoredCards = storedCard;
//
//    }



    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}