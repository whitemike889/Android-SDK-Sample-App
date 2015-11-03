package com.payu.payuui.Fragment;

/**
 * Created by ankur on 8/24/15.
 */

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;
import com.payu.payuui.Activity.PayuBaseActivity;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;


public final class SavedCardItemFragment extends Fragment {

    private StoredCard mStoredCard;
    private PayuUtils mPayuUtils;
    private EditText cvvEditText;
    private String issuingBankStatus;
    private TextView issuingBankDownText;

    public SavedCardItemFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mStoredCard = bundle.getParcelable(PayuConstants.STORED_CARD);
        issuingBankStatus = bundle.getString(SdkUIConstants.ISSUING_BANK_STATUS);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.layout_saved_card,null);

        issuingBankDownText = (TextView) view.findViewById(R.id.text_view_saved_card_bank_down_error);

        mPayuUtils = new PayuUtils();

        cvvEditText = (EditText) view.findViewById(R.id.edit_text_cvv);

        if(mStoredCard.getCardBrand().equals("AMEX")){
            Log.d("####HARMEET", mStoredCard.getMaskedCardNumber().length()+"");
            cvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(4)});
        }


        Log.d("####HARMEET", mStoredCard.getCardBrand());

        if(mStoredCard.getMaskedCardNumber().length() == 19 && mStoredCard.getCardBrand() == "SMAE" ){
            cvvEditText.setVisibility(View.GONE);
        }

        cvvEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String cvv = s.toString();
                if (mPayuUtils.validateCvv(mStoredCard.getCardBin(), cvv) && !cvv.equals("")) {
                    getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
                    //isCvvValid = true;
                    //valid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);

                } else{

                    getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
                    //isCvvValid = false;
//                    invalid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((TextView)view.findViewById(R.id.text_view_masked_card_number)).setText(mStoredCard.getMaskedCardNumber());
        ((TextView)view.findViewById(R.id.text_view_card_name)).setText(mStoredCard.getCardName());
        ((TextView)view.findViewById(R.id.text_view_card_mode)).setText(mStoredCard.getCardMode());
        ((ImageView)view.findViewById(R.id.card_type_image)).setImageResource(getIssuerImage(mStoredCard.getCardBrand()));
        ((ImageView)view.findViewById(R.id.bank_image)).setImageResource(getIssuingBankImage(mStoredCard.getIssuingBank()));
        Log.d("Abc", mStoredCard.getIssuingBank());
//        mStoredCard.getCardBin();
        if(issuingBankStatus.equals("") == false){
            issuingBankDownText.setVisibility(View.VISIBLE);
            issuingBankDownText.setText(issuingBankStatus);
//                            bankDownText.setCompoundDrawablesWithIntrinsicBounds(R.id, 0, 0, 0);
//                            bankDownText.setText(netBankingList.get(index).getBankName()+" is temporarily down");
        }
        else{
            issuingBankDownText.setVisibility(View.GONE);
        }




        return  view;
    }

    private int getIssuingBankImage(String issuingBank){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            switch (issuingBank) {
                case PayuConstants.HDFC:
                    return R.drawable.hdfc;
                case PayuConstants.ICICI:
                    return R.drawable.icici;
                case PayuConstants.CITI:
                    return R.drawable.citi;
                case PayuConstants.HSBC:
                    return R.drawable.hsbc;
                case PayuConstants.IDBI:
                    return R.drawable.idbi;
                case PayuConstants.INDUSIND:
                    return R.drawable.induslogo;
                case PayuConstants.ING:
                    return R.drawable.ing_logo;
                case PayuConstants.KOTAK:
                    return R.drawable.kotak;
                case PayuConstants.SBIDC:
                    return R.drawable.sbi;
                case PayuConstants.SC:
                    return R.drawable.scblogo;
                case PayuConstants.YES:
                    return R.drawable.yesbank_logo;

            }
            return 0;
        } else {

            switch (issuingBank) {
                case PayuConstants.HDFC:
                    return R.drawable.hdfc;
                case PayuConstants.ICICI:
                    return R.drawable.icici;
                case PayuConstants.CITI:
                    return R.drawable.citi;
                case PayuConstants.HSBC:
                    return R.drawable.hsbc;
                case PayuConstants.IDBI:
                    return R.drawable.idbi;
                case PayuConstants.INDUSIND:
                    return R.drawable.induslogo;
                case PayuConstants.ING:
                    return R.drawable.ing_logo;
                case PayuConstants.KOTAK:
                    return R.drawable.kotak;
                case PayuConstants.SBIDC:
                    return R.drawable.sbi;
                case PayuConstants.SC:
                    return R.drawable.scblogo;
                case PayuConstants.YES:
                    return R.drawable.yesbank_logo;
            }
            return 0;
        }

    }
    private int getIssuerImage(String issuer) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            switch (issuer) {
                case PayuConstants.VISA:
                    return R.drawable.logo_visa;
                case PayuConstants.LASER:
                    return R.drawable.laser;
                case PayuConstants.DISCOVER:
                    return R.drawable.discover;
                case PayuConstants.MAES:
                    return R.drawable.mas_icon;
                case PayuConstants.MASTERCARD:
                    return R.drawable.mc_icon;
                case PayuConstants.AMEX:
                    return R.drawable.amex;
                case PayuConstants.DINR:
                    return R.drawable.diner;
                case PayuConstants.JCB:
                    return R.drawable.jcb;
                case PayuConstants.SMAE:
                    return R.drawable.maestro;
                case PayuConstants.RUPAY:
                    return R.drawable.rupay;
//                TODO ask Franklin for rupay regex
            }
            return 0;
        } else {

            switch (issuer) {
                case PayuConstants.VISA:
                    return R.drawable.logo_visa;
                case PayuConstants.LASER:
                    return R.drawable.laser;
                case PayuConstants.DISCOVER:
                    return R.drawable.discover;
                case PayuConstants.MAES:
                    return R.drawable.mas_icon;
                case PayuConstants.MASTERCARD:
                    return R.drawable.mc_icon;
                case PayuConstants.AMEX:
                    return R.drawable.amex;
                case PayuConstants.DINR:
                    return R.drawable.diner;
                case PayuConstants.JCB:
                    return R.drawable.jcb;
                case PayuConstants.SMAE:
                    return R.drawable.maestro;
//                case PayuConstants.RUPAY:
//                    return getResources().getDrawable(R.drawable.rupay, null);
                //TODO ask Franklin
            }
            return 0;
        }
    }




}