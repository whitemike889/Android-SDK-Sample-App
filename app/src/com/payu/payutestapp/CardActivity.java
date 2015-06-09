package com.payu.payutestapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.Cards;
import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.PaymentListener;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class CardActivity extends ActionBarActivity implements PaymentListener{

    Boolean isCardNumberValid = false;
    Boolean isExpired = true;
    Boolean isCvvValid = false;
    Drawable nameOnCardDrawable;
    Drawable cardNumberDrawable;
    Drawable calenderDrawable;
    Drawable cvvDrawable;
    Drawable cardNameDrawable;
    Drawable issuerDrawable;
    private int expiryMonth;
    private int expiryYear;
    private String cardNumber = "";
    private String cvv = "";
    private String nameOnCard = "";
    private String cardName = "";

    private String issuer = "";

    private String userCredentials;

    Payment payment;
    Payment.Builder builder;

    Bundle defaultParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);

        builder = new Payment().new Builder();
        Cards.initializeIssuers(getResources());

        defaultParams = getIntent().getExtras();
        userCredentials = defaultParams.getString(PayU.USER_CREDENTIALS);

        if(Constants.ENABLE_VAS && PayU.issuingBankDownBin == null){ // vas has not been called, lets fetch bank down time.
            HashMap<String, String> varList = new HashMap<String, String>();

            varList.put("var1", "default");
            varList.put("var2", "default");
            varList.put("var3", "default");

            List<NameValuePair> postParams = null;


            try {
                postParams = PayU.getInstance(this).getParams(Constants.GET_VAS, varList);
                GetResponseTask getResponse = new GetResponseTask(this);
                getResponse.execute(postParams);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        ((EditText)findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).setFocusable(false);



        findViewById(com.payu.sdk.R.id.expiryDatePickerEditText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CardActivity.this, com.payu.sdk.R.style.ProgressDialog);
                View datePickerLayout = getLayoutInflater().inflate(com.payu.sdk.R.layout.date_picker, null);
                dialog.setContentView(datePickerLayout);
                dialog.setCancelable(false);
                Button okButton = (Button) datePickerLayout.findViewById(com.payu.sdk.R.id.datePickerOkButton);
                final DatePicker datePicker = (DatePicker) datePickerLayout.findViewById(com.payu.sdk.R.id.datePicker);

                try { // lets hide the day spinner on pre lollipop devices
                    Field datePickerFields[] = datePicker.getClass().getDeclaredFields();
                    for (Field datePickerField : datePickerFields) {
                        if ("mDayPicker".equals(datePickerField.getName()) || "mDaySpinner".equals(datePickerField.getName()) || "mDelegate".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            ((View) datePickerField.get(datePicker)).setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {

                }
                dialog.show();
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expiryMonth = datePicker.getMonth() + 1;
                        expiryYear = datePicker.getYear();
                        ((EditText)findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).setText("" + expiryMonth + " / " + expiryYear);
                        if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
                            isExpired = false;
                            valid(((EditText)findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
                            isExpired = false;
                            valid(((EditText)findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        } else {
                            isExpired = true;
                            invalid(((EditText)findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        /* store card */
        if (userCredentials != null) {
            findViewById(com.payu.sdk.R.id.storeCardCheckBox).setVisibility(View.VISIBLE);
        }
        // this comes form stored card fragment
//        if (userCredentials != null) {
//            findViewById(com.payu.sdk.R.id.storeCardCheckBox).setVisibility(View.VISIBLE);
//            ((CheckBox) findViewById(com.payu.sdk.R.id.storeCardCheckBox)).setChecked(true);
//            findViewById(com.payu.sdk.R.id.cardNameEditText).setVisibility(View.VISIBLE);
//        }

        findViewById(com.payu.sdk.R.id.storeCardCheckBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
//                    getArguments().putString(PayU.STORE_CARD, PayU.STORE_CARD);
                    findViewById(com.payu.sdk.R.id.cardNameEditText).setVisibility(View.VISIBLE);
                } else {
//                    getArguments().remove(PayU.STORE_CARD);
                    findViewById(com.payu.sdk.R.id.cardNameEditText).setVisibility(View.GONE);
                }
            }
        });


        findViewById(com.payu.sdk.R.id.makePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

/*                builder.set(PayU.PRODUCT_INFO, defaultParams.getString(PayU.PRODUCT_INFO));
                builder.set(PayU.AMOUNT, defaultParams.getString(PayU.AMOUNT));
                builder.set(PayU.TXNID, defaultParams.getString(PayU.TXNID));
                builder.set(PayU.SURL, defaultParams.getString(PayU.SURL));
                builder.set(PayU.FURL, defaultParams.getString(PayU.FURL));*/


                Params requiredParams = new Params();
                for(String key: defaultParams.keySet()){
                    builder.set(key, defaultParams.getString(key));
                    requiredParams.put(key, defaultParams.getString(key));
                }

                builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.CC));


                String nameOnCard = ((TextView) findViewById(com.payu.sdk.R.id.nameOnCardEditText)).getText().toString();
                if (nameOnCard.length() < 3) {
                    nameOnCard = "PayU " + nameOnCard;
                }


                requiredParams.put(PayU.CARD_NUMBER, cardNumber);
                requiredParams.put(PayU.EXPIRY_MONTH, String.valueOf(expiryMonth));
                requiredParams.put(PayU.EXPIRY_YEAR, String.valueOf(expiryYear));
                requiredParams.put(PayU.CARDHOLDER_NAME, nameOnCard);
                requiredParams.put(PayU.CVV, cvv);

                /*requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
                requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
                requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
                requiredParams.put(PayU.SURL, builder.get(PayU.SURL));
                requiredParams.put(PayU.FURL, builder.get(PayU.FURL));*/

                try {
                    Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;

                    requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                    payment = builder.create();

                    String postData = PayU.getInstance(CardActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(CardActivity.this, ProcessPaymentActivity.class);
                    intent.putExtra(Constants.POST_DATA, postData);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivityForResult(intent, PayU.RESULT);
                } catch (MissingParameterException e) {
                    e.printStackTrace();
                } catch (HashException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        nameOnCardDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.user);
        cardNumberDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.card);
        calenderDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.calendar);
        cvvDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.lock);
        cardNameDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.user);

        cardNumberDrawable.setAlpha(100);
        calenderDrawable.setAlpha(100);
        cvvDrawable.setAlpha(100);


        ((EditText) findViewById(R.id.nameOnCardEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, nameOnCardDrawable, null);
        ((EditText) findViewById(R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNumberDrawable, null);
        ((EditText) findViewById(R.id.expiryDatePickerEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, calenderDrawable, null);
        ((EditText) findViewById(R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cvvDrawable, null);
        ((EditText) findViewById(R.id.cardNameEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNameDrawable, null);

        if(savedInstanceState != null){
            expiryMonth = savedInstanceState.getInt("ccexpmon");
            expiryYear = savedInstanceState.getInt("ccexpyr");

            if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
                isExpired = false;
                valid(((EditText) findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
            } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
                isExpired = false;
                valid(((EditText) findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
            } else {
                isExpired = true;
                invalid(((EditText)findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
            }
        }

        ((EditText) findViewById(com.payu.sdk.R.id.cardNameEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardName = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        ((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                ((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)).getText().clear();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardNumber = charSequence.toString();

                issuer = Cards.getIssuer(cardNumber);

                if(issuer.contentEquals("AMEX")){
                    ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                }else{
                    ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                }
                if(issuer != null){
                    issuerDrawable = Cards.ISSUER_DRAWABLE.get(issuer);
                }

                if (issuer.contentEquals("SMAE")) {
                    // disable cvv and expiry
                    findViewById(com.payu.sdk.R.id.expiryCvvLinearLayout).setVisibility(View.GONE);
                    findViewById(com.payu.sdk.R.id.haveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                    findViewById(com.payu.sdk.R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    if (Cards.validateCardNumber(cardNumber)) {
                        isCardNumberValid = true;
                        if(PayU.issuingBankDownBin != null && PayU.issuingBankDownBin.has(cardNumber.substring(0, 6))){// oops bank is down.
                            try {
                                ((TextView)findViewById(com.payu.sdk.R.id.issuerDownTextView)).setText(PayU.issuingBankDownBin.getString(cardNumber.substring(0,6)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            findViewById(com.payu.sdk.R.id.issuerDownTextView).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(com.payu.sdk.R.id.issuerDownTextView).setVisibility(View.GONE);
                        }
                        if (getIntent().getExtras().getString(PayU.OFFER_KEY) != null)
                            checkOffer(cardNumber, getIntent().getExtras().getString(PayU.OFFER_KEY));
                        valid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), issuerDrawable);
                    } else {
                        isCardNumberValid = false;
                        invalid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        resetHeader();
                    }
                } else {
                    // enable cvv and expiry
                    findViewById(com.payu.sdk.R.id.expiryCvvLinearLayout).setVisibility(View.VISIBLE);
                    findViewById(com.payu.sdk.R.id.haveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    findViewById(com.payu.sdk.R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    if (Cards.validateCardNumber(cardNumber)) {

                        isCardNumberValid = true;

                        if(PayU.issuingBankDownBin != null && PayU.issuingBankDownBin.has(cardNumber.substring(0, 6))){// oops bank is down.
                            try {
                                ((TextView)findViewById(com.payu.sdk.R.id.issuerDownTextView)).setText("We are experiencing high failure rate for "+PayU.issuingBankDownBin.getString(cardNumber.substring(0,6)) + " cards at this time. We recommend you pay using any other means of payment.");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            findViewById(com.payu.sdk.R.id.issuerDownTextView).setVisibility(View.VISIBLE);
                        }else{
                            findViewById(com.payu.sdk.R.id.issuerDownTextView).setVisibility(View.GONE);
                        }

//                        if (getIntent().getExtras().getString(PayU.OFFER_KEY) != null)
//                            checkOffer(cardNumber, getIntent().getExtras().getString(PayU.OFFER_KEY));
                        valid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), issuerDrawable);
                    } else {
                        isCardNumberValid = false;
                        invalid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        resetHeader();
                    }
                }

                // lets set the issuer drawable.

                if(issuer != null && issuerDrawable != null){
                    ((EditText)findViewById(com.payu.sdk.R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, issuerDrawable, null);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cvv = charSequence.toString();
                if(Cards.validateCvv(cardNumber, cvv)){
                    isCvvValid = true;
                    valid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }else{
                    isCvvValid = false;
                    invalid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });
        findViewById(com.payu.sdk.R.id.haveClickHereTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(com.payu.sdk.R.id.expiryCvvLinearLayout).setVisibility(View.VISIBLE);
                findViewById(com.payu.sdk.R.id.haveCvvExpiryLinearLayout).setVisibility(View.GONE);
                findViewById(com.payu.sdk.R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                isCvvValid = false;
                isExpired = true;
                ((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).getText().clear();
                ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).getText().clear();
                invalid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);

            }
        });

        findViewById(com.payu.sdk.R.id.dontHaveClickHereTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(com.payu.sdk.R.id.expiryCvvLinearLayout).setVisibility(View.GONE);
                findViewById(com.payu.sdk.R.id.haveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                findViewById(com.payu.sdk.R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                valid(((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
            }
        });

        findViewById(com.payu.sdk.R.id.cardNumberEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
        findViewById(com.payu.sdk.R.id.nameOnCardEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

        findViewById(com.payu.sdk.R.id.cvvEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

        findViewById(com.payu.sdk.R.id.expiryDatePickerEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
    }

    private void valid(EditText editText, Drawable drawable) {
        drawable.setAlpha(255);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if (findViewById(com.payu.sdk.R.id.expiryCvvLinearLayout).getVisibility() == View.GONE) {
            isExpired = false;
            isCvvValid = true;
        }
        if (isCardNumberValid && !isExpired && isCvvValid ) {
            findViewById(com.payu.sdk.R.id.makePayment).setEnabled(true);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button_enabled);
        } else {
            findViewById(com.payu.sdk.R.id.makePayment).setEnabled(false);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button);
        }
    }

    private void invalid(EditText editText, Drawable drawable) {
        drawable.setAlpha(100);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        findViewById(com.payu.sdk.R.id.makePayment).setEnabled(false);
        findViewById(com.payu.sdk.R.id.makePayment).setBackgroundResource(com.payu.sdk.R.drawable.button);
    }

    private void makeInvalid() {
        if (!isCardNumberValid && cardNumber.length() > 0 && !findViewById(com.payu.sdk.R.id.cardNumberEditText).isFocused())
            ((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.error_icon), null);
        if (!isCvvValid && cvv.length() > 0 && !findViewById(com.payu.sdk.R.id.cvvEditText).isFocused())
            ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.error_icon), null);
    }

    private void checkOffer(String cardNumber, String offerKey) {
        List<NameValuePair> postParams = null;

        HashMap<String, String> varList = new HashMap<String, String>();

        // offer key
        varList.put(PayU.VAR1, offerKey);
        // amount
        varList.put(PayU.VAR2, "" + getIntent().getExtras().getDouble(PayU.AMOUNT));
        // category
        varList.put(PayU.VAR3, "CC");
        // bank code
        varList.put(PayU.VAR4, cardNumber.startsWith("4") ? "VISA" : "MAST");
        //  card number
        varList.put(PayU.VAR5, cardNumber);
        // name on card
        varList.put(PayU.VAR6, nameOnCard);
        // phone number
        varList.put(PayU.VAR7, "");
        // email id
        varList.put(PayU.VAR8, "");

        try {
            postParams = PayU.getInstance(this).getParams(Constants.OFFER_STATUS, varList);
            GetResponseTask getOfferStatus = new GetResponseTask(this);
            getOfferStatus.execute(postParams);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    @Override
    public void onGetResponse(String responseMessage) {
        Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            setResult(resultCode, data);
            finish();
        }
    }

    private void resetHeader() {
        if (findViewById(com.payu.sdk.R.id.offerMessageTextView) != null && findViewById(com.payu.sdk.R.id.offerMessageTextView).getVisibility() == View.VISIBLE) {
            findViewById(com.payu.sdk.R.id.offerAmountTextView).setVisibility(View.GONE);
            ((TextView) findViewById(com.payu.sdk.R.id.offerMessageTextView)).setText("");
            ((TextView) findViewById(com.payu.sdk.R.id.amountTextView)).setGravity(Gravity.CENTER);
            ((TextView) findViewById(com.payu.sdk.R.id.amountTextView)).setTextColor(Color.BLACK);
            ((TextView) findViewById(com.payu.sdk.R.id.amountTextView)).setPaintFlags(0);
            ((TextView) findViewById(com.payu.sdk.R.id.amountTextView)).setText(getString(com.payu.sdk.R.string.amount, getIntent().getExtras().getDouble(PayU.AMOUNT)));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // lets put the expiry month and expiry year here.
        outState.putInt("ccexpmon", expiryMonth);
        outState.putInt("ccexpyr", expiryYear);
    }
}
