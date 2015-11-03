package com.payu.payuui.Fragment;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.GetOfferStatusApiListener;
import com.payu.india.Model.CardStatus;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.GetOfferStatusTask;
import com.payu.payuui.Activity.PayuBaseActivity;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.MonthYearPickerDialog;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreditDebitFragment extends Fragment implements GetOfferStatusApiListener {

    private PayuHashes mPayuHashes;
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private PayuUtils payuUtils;
    private PostData postData;
    private MerchantWebService merchantWebService;

    private boolean isCvvValid = false;
    private boolean isExpiryMonthValid = false;
    private boolean isExpiryYearValid = false;
    private boolean isCardNumberValid = false;

    private String nameOnCard;
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear="20";
    private String cardName;
    private Bundle fragmentBundle;
    private Bundle activityBundle;
    private String issuer;
    private HashMap<String, CardStatus> valueAddedHashMap;

    private int amexLength = 4;



    private EditText nameOnCardEditText;
    private EditText cardNumberEditText;
    private EditText cardCvvEditText;
    private EditText cardExpiryMonthEditText;
    private EditText cardExpiryYearEditText;
    private EditText cardNameEditText;
    private CheckBox saveCardCheckBox;
    private ImageView cardImage;
    private ImageView cvvImage;
    private ImageButton myTestImageButton;
    private DatePickerDialog.OnDateSetListener datePickerListener;
    private static final char space = ' ';
    private LinearLayout mLinearLayout;
    private TextView amountText;
    private TextView issuingBankDown;
    private ViewPager viewpager;
    private int fragmentPosition;

    public CreditDebitFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentBundle = getArguments();
        valueAddedHashMap = (HashMap<String, CardStatus>) fragmentBundle.getSerializable(SdkUIConstants.VALUE_ADDED);
        fragmentPosition = fragmentBundle.getInt(SdkUIConstants.POSITION);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(this.isVisible()) {
//            getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
//        }
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d("###HARMEET", "OnView_Credit_Debit");


        View view = inflater.inflate(R.layout.fragment_credit_debit, container, false);

        viewpager = (ViewPager) getActivity().findViewById(R.id.pager);

//        getActivity().findViewById(R.id.button_pay_now).setEnabled(false);

        nameOnCardEditText = (EditText) view.findViewById(R.id.edit_text_name_on_card);
        cardNumberEditText = (EditText) view.findViewById(R.id.edit_text_card_number);
        cardCvvEditText = (EditText) view.findViewById(R.id.edit_text_card_cvv);
        cardExpiryMonthEditText = (EditText) view.findViewById(R.id.edit_text_expiry_month);
        cardExpiryYearEditText = (EditText) view.findViewById(R.id.edit_text_expiry_year);
        cardNameEditText = (EditText) view.findViewById(R.id.edit_text_card_label);
        saveCardCheckBox = (CheckBox) view.findViewById(R.id.check_box_save_card);
        cardImage = (ImageView) view.findViewById(R.id.image_card_type);
        cvvImage = (ImageView) view.findViewById(R.id.image_cvv);
        mLinearLayout = (LinearLayout) view.findViewById(R.id.layout_expiry_date);
        issuingBankDown = (TextView) view.findViewById(R.id.text_view_issuing_bank_down_error);

        amountText = (TextView) getActivity().findViewById(R.id.textview_amount);


        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            cardExpiryMonthEditText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    MonthYearPickerDialog newFragment = new MonthYearPickerDialog();
                    newFragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
                    newFragment.setListener(datePickerListener);

                }
            });

            cardExpiryYearEditText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    MonthYearPickerDialog newFragment = new MonthYearPickerDialog();
                    newFragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
                    newFragment.setListener(datePickerListener);

                }
            });


            datePickerListener
                    = new DatePickerDialog.OnDateSetListener() {

                // when dialog box is closed, below method will be called.
                public void onDateSet(DatePicker view, int selectedDay,
                                      int selectedMonth, int selectedYear) {
                    cardExpiryYearEditText.setText("" + selectedYear);
                    cardExpiryMonthEditText.setText("" + selectedMonth);

                    isExpiryYearValid = true;
                    isExpiryMonthValid = true;

                    if(selectedYear == Calendar.YEAR && selectedMonth < Calendar.MONTH){
                        isExpiryMonthValid = false;
                    }

                    uiValidation();
                }
            };

        }
        else{

            cardExpiryYearEditText.setFocusableInTouchMode(true);
            cardExpiryMonthEditText.setFocusableInTouchMode(true);

//            cardExpiryMonthEditText.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                    charSequence.toString();
//                }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//
//                    if(editable.length() == 2 && Integer.parseInt(editable.toString()) >= Calendar.MONTH){
//
//                        isExpiryMonthValid = true;
//
//                    }
//                    else
//                        isExpiryMonthValid = false;
//
//
//
//                }
//
//            });

            cardExpiryYearEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    charSequence.toString();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if(editable.length() == 4 && Integer.parseInt(editable.toString()) >= Calendar.YEAR){

                        isExpiryYearValid = true;

                    }
                    else
                        isExpiryYearValid = false;

                }

            });

        }
        cardNameEditText.setVisibility(View.GONE);

        saveCardCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (compoundButton.isChecked()) {
                    cardNameEditText.setVisibility(View.VISIBLE);
                } else {
                    cardNameEditText.setVisibility(View.GONE);
                }

            }
        });


        activityBundle = ((PayuBaseActivity) getActivity()).bundle;
        mPaymentParams = activityBundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        mPayuHashes = activityBundle.getParcelable(PayuConstants.PAYU_HASHES);

        payuConfig = activityBundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();


        if (null == mPaymentParams.getUserCredentials())
            saveCardCheckBox.setVisibility(View.GONE);
        else
            saveCardCheckBox.setVisibility(View.VISIBLE);

        payuUtils = new PayuUtils();

//
        //
        //
        //
        cardNumberEditText.addTextChangedListener(new TextWatcher() {

            int image;
            int cardLength = 20;
            int setSpacesIndex = 4;
            private String ccNumber = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 6) { // to confirm rupay card we need min 6 digit.
                    if (null == issuer) issuer = payuUtils.getIssuer(charSequence.toString().replace(" ",""));
                    Log.d("HARMEET##",issuer);
                    if (issuer != null && issuer.length() > 1 ) {
                        image = getIssuerImage(issuer);
                        cardImage.setImageResource(image);
                        if(issuer == "AMEX")
                            cardCvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(4)});
                        else
                            cardCvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
                        if(issuer == "SMAE" || issuer == "MAES") {
                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(23)});
                            cardLength = 23;
                        }else if(issuer == "AMEX"){
                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
                            cardLength = 18;
                        }
                        else if(issuer == "DINR"){
                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
                            cardLength = 17;
                        }
                        else {
                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19)});
                            cardLength = 20;
                        }
                    }
                } else {
                    issuer = null;
                    cardImage.setImageResource(R.drawable.icon_card);
                    cardCvvEditText.getText().clear();;

                }

                if(charSequence.length() == 7){
                    if(valueAddedHashMap.get(charSequence.toString().replace(" ","")) != null) {
                        int statusCode = valueAddedHashMap.get(charSequence.toString().replace(" ", "")).getStatusCode();

                        if(statusCode == 0){
                            issuingBankDown.setVisibility(View.VISIBLE);
                            issuingBankDown.setText(valueAddedHashMap.get(charSequence.toString().replace(" ", "")).getBankName()+" is temporarily down");
//                            bankDownText.setCompoundDrawablesWithIntrinsicBounds(R.id, 0, 0, 0);
//                            bankDownText.setText(netBankingList.get(index).getBankName()+" is temporarily down");
                        }
                        else{
                            issuingBankDown.setVisibility(View.GONE);
                        }


//                        Toast.makeText(getActivity(), "Response status: " + statusCode, Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                String temp = s.toString();
                int flag = 0;

                if(ccNumber.length() < s.length()) {


                    switch (s.length()) {
                        case 5:
                            temp = s.toString().substring(0,4)+" "+s.toString().substring(4);
                            flag = 1;
                            break;
                        case 10:
                            flag = 1;
                            temp = s.toString().substring(0,9)+" "+s.toString().substring(9);
                            break;
                        case 15:
                            flag = 1;
                            temp = s.toString().substring(0,14)+" "+s.toString().substring(14);
                            break;
                        case 17:
                            if(issuer == "DINR"){
                                cardValidation();
                            }
                            break;
                        case 18:
                            if(issuer == "AMEX"){
                                cardValidation();
                            }
                            break;
                        case 20:
                            temp = s.toString().substring(0,19)+" "+s.toString().substring(19);
                            flag = 1;
                            break;
                        case 19:
                            cardValidation();
                            break;
                        case 23:
                            cardValidation();
                            break;
                    }
                }
                else if(ccNumber.length() >= s.length() ) {
                    if(viewpager.getCurrentItem() == fragmentPosition)
                    getActivity().findViewById(R.id.button_pay_now).setEnabled(false);

                    switch (s.length()) {
                        case 5:
                            temp = s.toString().substring(0,4);
                            flag = 1;
                            break;
                        case 10:
                            temp = s.toString().substring(0,9);
                            flag = 1;
                            break;
                        case 15:
                            temp = s.toString().substring(0,14);
                            flag = 1;
                            break;
                        case 20:
                            temp = s.toString().substring(0,19);
                            flag = 1;
                            break;
                    }
                }

                ccNumber = s.toString();

                if(flag == 1){
                    flag = 0;
                    cardNumberEditText.setText(temp);
                    cardNumberEditText.setSelection(cardNumberEditText.length());
                }
            }

        });


//        cardNumberEditText.addTextChangedListener(new TextWatcher() {
//            String issuer;
//            int image;
//            int cardLength = 20;
//            int setSpacesIndex = 4;
//            private String ccNumber = "";
//
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length() > 6) { // to confirm rupay card we need min 6 digit.
//                    if (null == issuer) issuer = payuUtils.getIssuer(charSequence.toString().replace(" ",""));
//                    if (issuer != null && issuer.length() > 1 ) {
//                        image = getIssuerImage(issuer);
//                        cardImage.setImageResource(image);
//                        if(issuer == "AMEX")
//                            cardCvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(4)});
//                        else
//                            cardCvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
//                        if(issuer == "SMAE" || issuer == "MAES") {
//                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(23)});
//                            cardLength = 23;
//                        }else if(issuer == "AMEX"){
//                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
//                            cardLength = 18;
//                        }
//                        else if(issuer == "DINR"){
//                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
//                            cardLength = 17;
//                        }
//                        else {
//                            cardNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
//                            cardLength = 20;
//                        }
//                    }
//                } else {
//                    issuer = null;
//                    cardImage.setImageResource(R.drawable.icon_card);
//                    cardCvvEditText.getText().clear();;
//
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//
//
//
//
//                   if(cardNumberEditText.getSelectionStart() == cardNumberEditText.getSelectionEnd() && (cardNumberEditText.getSelectionEnd() == 4 || cardNumberEditText.getSelectionEnd() == 9 || cardNumberEditText.getSelectionEnd() == 14 ||cardNumberEditText.getSelectionEnd() == 19)){
//
//                       int position = cardNumberEditText.getSelectionStart();
//                       if(ccNumber.length() < s.length()){
//
//                           if(s.length() == position){
//                               cardNumberEditText.setText(cardNumberEditText.getText().toString()+" ");
//
//                               cardNumberEditText.setSelection(position+1);
//                           }else{
////                               cardNumberEditText.getText().clear();
//
//                           }
//                       }else {
////                           cardNumberEditText.getText().clear();
//                       }
//
//
//                   }
//
//                ccNumber = s.toString();
//            }
//
//        });


        //
        //
        //
        cardNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {

                    cardValidation();
                }
            }
        });

        cardCvvEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cvv = charSequence.toString();
                if (payuUtils.validateCvv(cardNumberEditText.getText().toString().replace(" ",""), cvv)) {
                    //isCvvValid = true;
                    //valid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    cvvImage.setAlpha((float)1);
                    isCvvValid = true;
                    uiValidation();
                } else{
                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    cvvImage.setAlpha((float)0.5);
                    isCvvValid = false;
                    uiValidation();
                    //isCvvValid = false;
//                    invalid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });



        return view;
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
                case PayuConstants.MAST:
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
                case PayuConstants.MAST:
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
                //TODO ask Franklin
            }
            return 0;
        }
    }



    public void onClickFunction() {

    }

    public void cardValidation(){

        if (!(payuUtils.validateCardNumber(cardNumberEditText.getText().toString().replace(" ", ""))) && cardNumberEditText.length() > 0 ) {
            cardImage.setImageResource(R.drawable.error_icon);
            isCardNumberValid = false;
            amountText.setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
            uiValidation();
        }
        else{
            isCardNumberValid = true;
            if(mPaymentParams.getOfferKey() != null && null != mPaymentParams.getUserCredentials())
            getOfferStatus();
            uiValidation();
        }
    }

    private void getOfferStatus() {

        merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.CHECK_OFFER_STATUS);
        merchantWebService.setHash(mPayuHashes.getCheckOfferStatusHash());
        merchantWebService.setVar1(mPaymentParams.getOfferKey());
        merchantWebService.setVar2(mPaymentParams.getAmount());
        merchantWebService.setVar3("CC");
        merchantWebService.setVar4("CC");
        merchantWebService.setVar5(cardNumberEditText.getText().toString().replace(" ",""));
        merchantWebService.setVar6(cardNameEditText.getText().toString());
        merchantWebService.setVar7("abc");
        merchantWebService.setVar8("abc@gmail.com");

        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if(postData.getCode() == PayuErrors.NO_ERROR) {
            payuConfig.setData(postData.getResult());

            GetOfferStatusTask getOfferStatusTask = new GetOfferStatusTask(CreditDebitFragment.this);
            getOfferStatusTask.execute(payuConfig);

            // lets cancel the dialog.
        }else{
            Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
        }

    }

    public void uiValidation(){

        if(issuer == "SMAE"){

            isCvvValid = true;
            isExpiryMonthValid = true;
            isExpiryYearValid = true;

        }

        if(isCardNumberValid && isCvvValid && isExpiryYearValid && isExpiryMonthValid && fragmentPosition == viewpager.getCurrentItem()){
            getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
        }
        else {
            if(viewpager.getCurrentItem() == fragmentPosition)
            getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
        }

    }

    @Override
    public void onGetOfferStatusApiResponse(PayuResponse payuResponse) {

        if(getActivity()!= null && payuResponse.getPayuOffer().getDiscount() != null ) {
            Toast.makeText(getActivity(), "Response status: " + payuResponse.getResponseStatus().getResult() + ": Discount = " + payuResponse.getPayuOffer().getDiscount(), Toast.LENGTH_LONG).show();


            Double amount = Double.parseDouble(mPaymentParams.getAmount()) - Double.parseDouble(payuResponse.getPayuOffer().getDiscount());
            String discountedAmount = "" + amount;
            amountText.setText(SdkUIConstants.AMOUNT + ": " + discountedAmount);

        }

        else
            amountText.setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
//            Toast.makeText(getActivity(), "Response status: " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();

    }

//


}
