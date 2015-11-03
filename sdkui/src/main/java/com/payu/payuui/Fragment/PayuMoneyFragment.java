package com.payu.payuui.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Activity.PayuBaseActivity;
import com.payu.payuui.Interfaces.ClickListener;
import com.payu.payuui.R;


public class PayuMoneyFragment extends Fragment  {

    public PayuMoneyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payu_money, container, false);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(this.isVisible()) {
////            getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
//        }
//    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            getActivity().setResult(resultCode, data);
            getActivity().finish();
        }
    }

}
