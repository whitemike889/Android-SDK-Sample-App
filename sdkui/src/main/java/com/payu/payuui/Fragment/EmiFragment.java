package com.payu.payuui.Fragment;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Activity.PayuBaseActivity;
import com.payu.payuui.Interfaces.ClickListener;
import com.payu.payuui.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmiFragment extends Fragment implements ClickListener {

    public EmiFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emi, container, false);
    }


    @Override
    public void onClickFunction() {

    }


}
