package com.payu.payuui.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.payu.india.Model.PayuResponse;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Fragment.CashCardFragment;
import com.payu.payuui.Fragment.CreditDebitFragment;
import com.payu.payuui.Fragment.EmiFragment;
import com.payu.payuui.Fragment.NetBankingFragment;
import com.payu.payuui.Fragment.PayuMoneyFragment;
import com.payu.payuui.Fragment.SavedCardsFragment;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;

import java.util.ArrayList;

/**
 * Created by piyush on 29/7/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> mTitles;
    private PayuResponse payuResponse;
    private PayuResponse valueAddedResponse;

    public PagerAdapter(FragmentManager fragmentManager, ArrayList<String> titles, PayuResponse payuResponse, PayuResponse valueAddedResponse) {
        super(fragmentManager);
        this.mTitles = titles;
        this.payuResponse = payuResponse;
        this.valueAddedResponse = valueAddedResponse;


    }



    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (mTitles.get(i)){
            case SdkUIConstants.SAVED_CARDS :
                fragment = new SavedCardsFragment();
                bundle.putParcelableArrayList(PayuConstants.STORED_CARD, payuResponse.getStoredCards());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getIssuingBankStatus());
                bundle.putInt(SdkUIConstants.POSITION, i);
                fragment.setArguments(bundle);
                return fragment;

            case SdkUIConstants.CREDIT_DEBIT_CARDS:
                fragment = new CreditDebitFragment();
                bundle.putParcelableArrayList(PayuConstants.CREDITCARD, payuResponse.getCreditCard());
                bundle.putParcelableArrayList(PayuConstants.DEBITCARD, payuResponse.getDebitCard());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getIssuingBankStatus());
                bundle.putInt(SdkUIConstants.POSITION, i);
                fragment.setArguments(bundle);
                return fragment;

            case SdkUIConstants.NET_BANKING:
                fragment = new NetBankingFragment();
                bundle.putParcelableArrayList(PayuConstants.NETBANKING, payuResponse.getNetBanks());
                bundle.putSerializable(SdkUIConstants.VALUE_ADDED, valueAddedResponse.getNetBankingDownStatus());
                fragment.setArguments(bundle);
                return fragment;

            case SdkUIConstants.PAYU_MONEY:
                fragment = new PayuMoneyFragment();
                bundle.putParcelableArrayList(PayuConstants.PAYU_MONEY, payuResponse.getPaisaWallet());
                return fragment;

//            case SdkUIConstants.EMI:
//                fragment = new EmiFragment();
//                bundle.putParcelableArrayList(PayuConstants.EMI, payuResponse.getEmi());
//                fragment.setArguments(bundle);
//                return fragment;
//
//            case SdkUIConstants.CASH_CARDS:
//                fragment = new CashCardFragment();
//                bundle.putParcelableArrayList(PayuConstants.CASHCARD, payuResponse.getCashCard());
//                fragment.setArguments(bundle);
//                return  fragment;

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


}
