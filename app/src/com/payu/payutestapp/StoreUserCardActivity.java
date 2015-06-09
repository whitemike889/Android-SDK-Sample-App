package com.payu.payutestapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.Cards;
import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.PayU;
import com.payu.sdk.PaymentListener;

import org.apache.http.NameValuePair;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class StoreUserCardActivity extends ActionBarActivity implements PaymentListener {

    DatePickerDialog.OnDateSetListener mDateSetListener;
    int mYear;
    int mMonth;
    int mDay;
    Boolean isNameOnCardValid = false;
    Boolean isCardNumberValid = false;
    Boolean isExpired = true;
    Drawable nameOnCardDrawable;
    Drawable cardNumberDrawable;
    Drawable calenderDrawable;
    Drawable cvvDrawable;
    Drawable cardNameDrawable;
    ProgressDialog mProgressDialog;
    private int expiryMonth;
    private int expiryYear;
    private String cardNumber = "";
    private String nameOnCard = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_user_card);

        mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH);
        mDay = Calendar.getInstance().get(Calendar.DATE);

        mProgressDialog = new ProgressDialog(this);

        findViewById(com.payu.sdk.R.id.cvvEditText).setVisibility(View.INVISIBLE);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                ((TextView) findViewById(R.id.expiryDatePickerEditText)).setText("" + (i2 + 1) + " / " + i);
                expiryMonth = i2 + 1;
                expiryYear = i;
                if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
                    isExpired = false;
                    valid(((EditText) findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
                    isExpired = false;
                    valid(((EditText) findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                } else {
                    isExpired = true;
                    invalid(((EditText) findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                }
            }
        };

        findViewById(R.id.expiryDatePickerEditText).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    Cards.customDatePicker(StoreUserCardActivity.this, mDateSetListener, mYear, mMonth, mDay).show();
                return false;
            }
        });


        findViewById(R.id.cardNameEditText).setVisibility(View.VISIBLE);


        findViewById(R.id.saveCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog.setMessage(getString(com.payu.sdk.R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                if (getIntent().getExtras().getString(PayU.USER_CREDENTIALS).length() < 1) {
                    Toast.makeText(StoreUserCardActivity.this, "User credentials not found.", Toast.LENGTH_LONG).show();
                    return;
                }

                RadioGroup radioGroup = (RadioGroup)findViewById(R.id.cardRadioGroup);
                String cardMode = ((RadioButton) findViewById(radioGroup.getCheckedRadioButtonId())).getText().toString().contentEquals("Credit card") ? "CC" : "DC";

                String cardNumber = ((TextView) findViewById(R.id.cardNumberEditText)).getText().toString();
                String cardType;
                if (cardMode.contentEquals("CC")) {
                    cardType = "CC";
                } else {
                    if (cardNumber.startsWith("4"))
                        cardType = "VISA";
                    else
                        cardType = "MAST";
                }

                List<NameValuePair> postParams = null;

                HashMap<String, String> varList = new HashMap<String, String>();
                // user credentials
                varList.put(Constants.VAR1, getIntent().getExtras().getString(PayU.USER_CREDENTIALS));
                //card name xyz
                varList.put(Constants.VAR2, ((EditText) findViewById(R.id.cardNameEditText)).getText().toString());
                //card mode cc
                varList.put(Constants.VAR3, cardMode);
                //card type
                varList.put(Constants.VAR4, cardType);
                // name on card
                varList.put(Constants.VAR5, ((EditText)findViewById(R.id.nameOnCardEditText)).getText().toString());
                // card number
                varList.put(Constants.VAR6, cardNumber);
                // card exp month
                varList.put(Constants.VAR7, "" + expiryMonth);
                // card exp year
                varList.put(Constants.VAR8, "" + expiryYear);

                try {
                    postParams = PayU.getInstance(StoreUserCardActivity.this).getParams(Constants.SAVE_USER_CARD, varList);
                    GetResponseTask getStoredCards = new GetResponseTask(StoreUserCardActivity.this);
                    getStoredCards.execute(postParams);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });


        nameOnCardDrawable = getResources().getDrawable(R.drawable.user);
        cardNumberDrawable = getResources().getDrawable(R.drawable.card);
        calenderDrawable = getResources().getDrawable(R.drawable.calendar);
        cvvDrawable = getResources().getDrawable(R.drawable.lock);
        cardNameDrawable = getResources().getDrawable(R.drawable.user);

//        cardNameDrawable.setAlpha(100);
        nameOnCardDrawable.setAlpha(100);
        cardNumberDrawable.setAlpha(100);
        calenderDrawable.setAlpha(100);
        cvvDrawable.setAlpha(100);


        ((EditText) findViewById(R.id.nameOnCardEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, nameOnCardDrawable, null);
        ((EditText) findViewById(R.id.cardNameEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNameDrawable, null);
        ((EditText) findViewById(R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNumberDrawable, null);
        ((EditText) findViewById(R.id.expiryDatePickerEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, calenderDrawable, null);
        ((EditText) findViewById(R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cvvDrawable, null);

        ((EditText) findViewById(R.id.nameOnCardEditText)).addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                nameOnCard = ((EditText) findViewById(R.id.nameOnCardEditText)).getText().toString();
                if (nameOnCard.length() > 1) {
                    isNameOnCardValid = true;
                    valid(((EditText) findViewById(R.id.nameOnCardEditText)), nameOnCardDrawable);
                } else {
                    isNameOnCardValid = false;
                    invalid(((EditText) findViewById(R.id.nameOnCardEditText)), nameOnCardDrawable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((EditText) findViewById(R.id.cardNumberEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardNumber = charSequence.toString();
                if (cardNumber.length() > 0 && Cards.validateCardNumber(cardNumber)) {
                    // valid name on card
                    isCardNumberValid = true;
//                    valid(((EditText) getActivity().findViewById(R.id.cardNumberEditText)), Cards.getCardDrawable(getResources(), cardNumber));
                } else {
                    isCardNumberValid = false;
                    invalid(((EditText)findViewById(com.payu.sdk.R.id.cardNumberEditText)), cardNumberDrawable);
                    cardNumberDrawable.setAlpha(100);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.cardNumberEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });
        findViewById(R.id.nameOnCardEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

        findViewById(R.id.expiryDatePickerEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_store_user_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void valid(EditText editText, Drawable drawable) {
        drawable.setAlpha(255);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if (isCardNumberValid && !isExpired && isNameOnCardValid) {
            findViewById(R.id.saveCard).setEnabled(true);
//            getActivity().findViewById(R.id.saveCard).setBackgroundResource(R.drawable.button_enabled);
        } else {
            findViewById(R.id.saveCard).setEnabled(false);
//            getActivity().findViewById(R.id.saveCard).setBackgroundResource(R.drawable.button);
        }
    }

    private void invalid(EditText editText, Drawable drawable) {
        drawable.setAlpha(100);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        findViewById(R.id.saveCard).setEnabled(false);
        findViewById(R.id.saveCard).setBackgroundResource(R.drawable.button);
    }

    private void makeInvalid() {
        if (!isCardNumberValid && cardNumber.length() > 0 && !findViewById(R.id.cardNumberEditText).isFocused())
            ((EditText) findViewById(R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.error_icon), null);
        if (!isNameOnCardValid && nameOnCard.length() > 0 && !findViewById(com.payu.sdk.R.id.nameOnCardEditText).isFocused())
            ((EditText) findViewById(R.id.nameOnCardEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.error_icon), null);
    }

    @Override
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    @Override
    public void onGetResponse(String responseMessage) {
        mProgressDialog.dismiss();
        String message;
        if(PayU.storedCards != null && PayU.storedCards.length() >1)
            message= responseMessage;
        else
            message = getString(com.payu.sdk.R.string.something_went_wrong);

        new AlertDialog.Builder(this)
                .setTitle(com.payu.sdk.R.string.status)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
//            Toast.makeText(getActivity(), response.get(0).toString(), Toast.LENGTH_LONG).show();
    }
}
