package com.payu.payutestapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.payu.sdk.adapters.EmiBankListAdapter;
import com.payu.sdk.adapters.EmiTimeIntervalAdapter;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EmiActivity extends ActionBarActivity implements PaymentListener{

    Spinner bankListSpinner;

    Spinner emiTimeIntervalSpinner;
    String bankCode;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    int mYear;
    int mMonth;
    int mDay;
    Boolean isNameOnCardValid = false;
    Boolean isCardNumberValid = false;
    Boolean isExpired = true;
    Boolean isCvvValid = false;
    Drawable nameOnCardDrawable;
    Drawable cardNumberDrawable;
    Drawable calenderDrawable;
    Drawable cvvDrawable;
    private int expiryMonth;
    private int expiryYear;
    private String cardNumber = "";
    private String cvv = "";
    private String nameOnCard = "";
    private String issuer;

    ProgressDialog mProgressDialog;

    String userCredentials;

    Bundle defaultParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emi);

        Cards.initializeIssuers(getResources());

        defaultParams = getIntent().getExtras();

        mProgressDialog = new ProgressDialog(this);

        nameOnCardDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.user);
        cardNumberDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.card);
        calenderDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.calendar);
        cvvDrawable = getResources().getDrawable(com.payu.sdk.R.drawable.lock);

        nameOnCardDrawable.setAlpha(100);
        cardNumberDrawable.setAlpha(100);
        calenderDrawable.setAlpha(100);
        cvvDrawable.setAlpha(100);

        if(savedInstanceState != null){
            expiryMonth = savedInstanceState.getInt("ccexpmon");
            expiryYear = savedInstanceState.getInt("ccexpyr");
            bankCode = savedInstanceState.getString("bankcode");

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

        mProgressDialog.setMessage(getString(com.payu.sdk.R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        List<NameValuePair> postParams = null;

        HashMap varList = new HashMap();

        if(defaultParams.getString("user_credentials") == null){// ok we dont have a user credentials.
            varList.put(Constants.VAR1, Constants.DEFAULT);
        }else{
            varList.put(Constants.VAR1, defaultParams.getString("user_credentials"));
        }

        try {
            postParams = PayU.getInstance(this).getParams(Constants.PAYMENT_RELATED_DETAILS, varList);
            GetResponseTask getResponse = new GetResponseTask(this);
            getResponse.execute(postParams);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        /*if(Constants.ENABLE_VAS && PayU.issuingBankDownBin == null){ // vas has not been called, lets fetch bank down time.
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
        }*/

        /*mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH);
        mDay = Calendar.getInstance().get(Calendar.DATE);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                ((TextView) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).setText("" + (i2 + 1) + " / " + i);
                expiryMonth = i2 + 1;
                expiryYear = i;

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
            }
        };

        findViewById(com.payu.sdk.R.id.expiryDatePickerEditText).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    Cards.customDatePicker(EmiActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
                return false;
            }
        });*/

        findViewById(R.id.expiryDatePickerEditText).setFocusable(false);



        findViewById(R.id.expiryDatePickerEditText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(EmiActivity.this, com.payu.sdk.R.style.ProgressDialog);
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
                        ((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).setText("" + expiryMonth + " / " + expiryYear);
                        if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
                            isExpired = false;
                            valid(((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
                            isExpired = false;
                            valid(((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        } else {
                            isExpired = true;
                            invalid(((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)), calenderDrawable);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        findViewById(com.payu.sdk.R.id.makePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    Params requiredParams = new Params();
                    Payment.Builder builder = new Payment().new Builder();

                    for(String key: defaultParams.keySet()){
                        builder.set(key, defaultParams.getString(key));
                        requiredParams.put(key, defaultParams.getString(key));
                    }


                    /*builder.set(PayU.PRODUCT_INFO, "product");
                    builder.set(PayU.AMOUNT, "5.0");
                    builder.set(PayU.TXNID, "MyProduct" + System.currentTimeMillis());
                    builder.set(PayU.SURL, "https://dl.dropboxusercontent.com/s/dtnvwz5p4uymjvg/success.html");*/
                    builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.EMI));

                    String cardNumber = ((TextView) findViewById(com.payu.sdk.R.id.cardNumberEditText)).getText().toString();

                    requiredParams.put(PayU.CARD_NUMBER, cardNumber);
                    requiredParams.put(PayU.EXPIRY_MONTH, String.valueOf(expiryMonth));
                    requiredParams.put(PayU.EXPIRY_YEAR, String.valueOf(expiryYear));
                    requiredParams.put(PayU.CARDHOLDER_NAME, nameOnCard);
                    requiredParams.put(PayU.CVV, cvv);


//                    requiredParams.put(PayU.BANKCODE, bankCode);


                    /*requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
                    requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
                    requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
                    requiredParams.put(PayU.SURL, builder.get(PayU.SURL));*/
                    requiredParams.put(PayU.BANKCODE, bankCode);
                    Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
                    requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                    Payment payment = builder.create();

                    String postData = PayU.getInstance(EmiActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(EmiActivity.this, ProcessPaymentActivity.class);
                    intent.putExtra(Constants.POST_DATA, postData);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivityForResult(intent, PayU.RESULT);

                }catch (MissingParameterException e) {
                    e.printStackTrace();
                } catch (HashException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        ((EditText) findViewById(com.payu.sdk.R.id.nameOnCardEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, nameOnCardDrawable, null);
        ((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNumberDrawable, null);
        ((EditText) findViewById(com.payu.sdk.R.id.expiryDatePickerEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, calenderDrawable, null);
        ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cvvDrawable, null);

        ((EditText) findViewById(com.payu.sdk.R.id.nameOnCardEditText)).addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                nameOnCard = charSequence.toString();
                if (nameOnCard.length() > 1) {
                    isNameOnCardValid = true;
                    valid(((EditText)findViewById(com.payu.sdk.R.id.nameOnCardEditText)), nameOnCardDrawable);
                } else {
                    isNameOnCardValid = false;
                    invalid(((EditText) findViewById(com.payu.sdk.R.id.nameOnCardEditText)), nameOnCardDrawable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((EditText)findViewById(com.payu.sdk.R.id.cardNumberEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardNumber = charSequence.toString();

                issuer = Cards.getIssuer(cardNumber);

                if (issuer.contentEquals("AMEX"))
                    ((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                else
                    ((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});


                if (Cards.validateCardNumber(cardNumber)) {
                    // valid name on card
                    isCardNumberValid = true;
                    valid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), Cards.ISSUER_DRAWABLE.get(issuer));
                } else {
                    isCardNumberValid = false;
                    invalid(((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)), cardNumberDrawable);
                    cardNumberDrawable.setAlpha(100);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((EditText) findViewById(com.payu.sdk.R.id.cvvEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cvv = charSequence.toString();
                if(Cards.validateCvv(cardNumber, cvv)){
                    isCvvValid = true;
                    valid(((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }else{
                    isCvvValid = false;
                    invalid(((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)), cvvDrawable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

    @Override
    public void onGetResponse(String responseMessage) {
        if(!isFinishing()) {

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (Constants.DEBUG) {
                Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
            }
            if (responseMessage != null && responseMessage.startsWith("Error:")) { // oops something went wrong.
                Intent intent = new Intent();
                intent.putExtra("result", responseMessage);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
            if (PayU.availableEmi != null) {
                setupAdapter();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null  && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }


    public void setupAdapter(){
        JSONArray bankList = new JSONArray();

        Set<String> bankNames = new HashSet<String>();

        try {
            for (int i = 0; i < PayU.availableEmi.length(); i++) {
                JSONObject jsonObject = new JSONObject();
                String bankName = PayU.availableEmi.getJSONObject(i).getString("bankName");
                if (!bankNames.contains(bankName)) {
                    bankNames.add(bankName);
                    jsonObject.put("bank", bankName);
                    bankList.put(jsonObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EmiBankListAdapter adapter = new EmiBankListAdapter(this, bankList);
        bankListSpinner = (Spinner) findViewById(com.payu.sdk.R.id.bankListSpinner);
        bankListSpinner.setAdapter(adapter);

        EmiTimeIntervalAdapter emiTimeIntervalAdapter = new EmiTimeIntervalAdapter(this, new JSONArray());
        emiTimeIntervalSpinner = (Spinner) findViewById(com.payu.sdk.R.id.emiTimeIntervalSpinner);
        emiTimeIntervalSpinner.setAdapter(emiTimeIntervalAdapter);


        bankListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                JSONArray emiArray = new JSONArray();
                try {
                    String selectedBank = ((JSONObject) adapterView.getAdapter().getItem(position)).getString("bank");

                    for (int i = 0; i < PayU.availableEmi.length(); i++) {
                        String bank = PayU.availableEmi.getJSONObject(i).getString("bankName");
                        if (bank.contentEquals(selectedBank)) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("emiInterval", PayU.availableEmi.getJSONObject(i).getString("emiInterval"));
                            jsonObject.put("emiCode", PayU.availableEmi.getJSONObject(i).getString("emiCode"));
                            emiArray.put(jsonObject);
                        }

                    }

                    EmiTimeIntervalAdapter adapter = new EmiTimeIntervalAdapter(EmiActivity.this, jsonArraySort(emiArray, "emiInterval"));
                    emiTimeIntervalSpinner.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (emiTimeIntervalSpinner != null)
            emiTimeIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        bankCode = ((JSONObject) adapterView.getAdapter().getItem(i)).getString("emiCode");
                        if(!bankCode.contentEquals("default")){
                            // call validation
                            valid(null, null);
                        }else{
                            invalid(null, null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
    }

    private void valid(EditText editText, Drawable drawable) {
        if(drawable != null) // for bank code
            drawable.setAlpha(255);
        if(editText != null) // for bank code
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if (isCardNumberValid && !isExpired && isCvvValid && isNameOnCardValid && !bankCode.contentEquals("default") ) {
            findViewById(com.payu.sdk.R.id.makePayment).setEnabled(true);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button_enabled);
        } else {
           findViewById(com.payu.sdk.R.id.makePayment).setEnabled(false);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button);
        }
    }

    private void invalid(EditText editText, Drawable drawable) {
        if(drawable != null) // for bank code
            drawable.setAlpha(100);
        if(editText != null) // for bank code
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        findViewById(com.payu.sdk.R.id.makePayment).setEnabled(false);
        findViewById(com.payu.sdk.R.id.makePayment).setBackgroundResource(com.payu.sdk.R.drawable.button);
    }

    private void makeInvalid() {

        if (!isCardNumberValid && cardNumber.length() > 0 && !findViewById(com.payu.sdk.R.id.cardNumberEditText).isFocused())
            ((EditText) findViewById(com.payu.sdk.R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.error_icon), null);
        if (!isNameOnCardValid && nameOnCard.length() > 0 && !findViewById(com.payu.sdk.R.id.nameOnCardEditText).isFocused())
            ((EditText) findViewById(com.payu.sdk.R.id.nameOnCardEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.error_icon), null);
        if (!isCvvValid && cvv.length() > 0 && !findViewById(com.payu.sdk.R.id.cvvEditText).isFocused())
            ((EditText)findViewById(com.payu.sdk.R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.error_icon), null);
    }

    private JSONArray jsonArraySort(JSONArray jsonArray, final String key) throws JSONException {

        // sort the available banks
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i));
        }

        Collections.sort(list, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID

            @Override
            public int compare(JSONObject a, JSONObject b) {
                try {
                    if (Integer.parseInt(a.getString(key).replaceAll("\\D+", "")) > Integer.parseInt(b.getString(key).replaceAll("\\D+", ""))) {
                        return -1;
                    } else
                        return 1;
                } catch (JSONException e) {

                }
                return 0;
            }
        });

        for (int i = 0; i < jsonArray.length(); i++) {
            sortedJsonArray.put(list.get(i));
        }
        return sortedJsonArray;
    }

    @Override
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // lets put the expiry month and expiry year here.
        outState.putInt("ccexpmon", expiryMonth);
        outState.putInt("ccexpyr", expiryYear);
        outState.putString("bankcode", bankCode);
    }

}
