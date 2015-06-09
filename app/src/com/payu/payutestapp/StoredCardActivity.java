package com.payu.payutestapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.PaymentListener;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.adapters.StoredCardAdapter;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


public class StoredCardActivity extends ActionBarActivity implements PaymentListener {

    ProgressDialog mProgressDialog;
    String nameOnCard;
    String userCredentials;
    Payment payment;
    Bundle defaultParams;
    Payment.Builder builder = new Payment().new Builder();

    long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_card);
        mProgressDialog = new ProgressDialog(this);

        defaultParams = getIntent().getExtras();

        if(defaultParams.getString(PayU.USER_CREDENTIALS) != null){
            userCredentials = defaultParams.getString(PayU.USER_CREDENTIALS);
            fetchStoredCards();
        }else{
            Toast.makeText(this, "NO User credentials found!", Toast.LENGTH_LONG).show();
        }

    }

    private void makePayment(JSONObject selectedCard, String cvv) {
        Params requiredParams = new Params();
        try {

            builder.set(PayU.PRODUCT_INFO, defaultParams.getString(PayU.PRODUCT_INFO));
            builder.set(PayU.AMOUNT, defaultParams.getString(PayU.AMOUNT));
            builder.set(PayU.TXNID, defaultParams.getString(PayU.TXNID));
            builder.set(PayU.SURL, defaultParams.getString(PayU.SURL));
            builder.set(PayU.FURL, defaultParams.getString(PayU.FURL));
            builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.CC));

            requiredParams.put(PayU.CVV, cvv);
            requiredParams.put("store_card_token", selectedCard.getString("card_token"));

            requiredParams.put(PayU.CARDHOLDER_NAME, nameOnCard);
            requiredParams.put(PayU.CVV, cvv);

            requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
            requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
            requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
            requiredParams.put(PayU.SURL, builder.get(PayU.SURL));
            requiredParams.put(PayU.FURL, builder.get(PayU.FURL));
            requiredParams.put(PayU.BANKCODE, String.valueOf(PayU.PaymentMode.CC));
            requiredParams.put(PayU.USER_CREDENTIALS, userCredentials);

            // we gotta handle this
//            startPaymentProcessActivity(PayU.PaymentMode.valueOf(selectedCard.getString("card_mode")), requiredParams);

            try {
                Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;

                requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                payment = builder.create();

                String postData = PayU.getInstance(this).createPayment(payment, requiredParams);

                Intent intent = new Intent(this, ProcessPaymentActivity.class);
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


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void confirmDelete(final JSONObject selectedCard){
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete card")
                    .setCancelable(false)
                    .setMessage("Do you want to delete " + selectedCard.getString("card_no") + " ?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteCard(selectedCard);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private  void deleteCard(JSONObject selectedCard){

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setMessage(getString(com.payu.sdk.R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        try {
            List<NameValuePair> postParams = null;
            HashMap<String, String> varList = new HashMap<String, String>();
            // user credentials
            varList.put(Constants.VAR1, userCredentials);
            //card token
            varList.put(Constants.VAR2, selectedCard.getString("card_token"));
            postParams = PayU.getInstance(this).getParams(Constants.DELETE_USER_CARD, varList);
            GetResponseTask getStoredCards = new GetResponseTask(this);
            getStoredCards.execute(postParams);
            fetchStoredCards();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchStoredCards(){

        if(!isFinishing()) {
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(this);

            mProgressDialog.setMessage(getString(com.payu.sdk.R.string.please_wait));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
              mProgressDialog.show();
        }

        List<NameValuePair> postParams = null;

        HashMap<String, String> varList = new HashMap<String, String>();
        varList.put(Constants.VAR1, userCredentials);

        try {
            postParams = PayU.getInstance(this).getParams(Constants.GET_USER_CARDS, varList);
            GetResponseTask getStoredCards = new GetResponseTask(this);
            getStoredCards.execute(postParams);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void setupAdapter() {

        StoredCardAdapter adapter = new StoredCardAdapter(this, PayU.storedCards);
        if (!isFinishing()) {
            if (PayU.storedCards.length() < 1) {
                findViewById(com.payu.sdk.R.id.noCardFoundTextView).setVisibility(View.VISIBLE);
                findViewById(com.payu.sdk.R.id.savedCardTextView).setVisibility(View.GONE);
            }
        }
        if (!isFinishing()) {
            ListView listView = (ListView) findViewById(com.payu.sdk.R.id.storedCardListView);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {// make payment.
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) { // to prevent quick double click.
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    final JSONObject selectedCard = (JSONObject) adapterView.getAdapter().getItem(i);
                    final EditText input = new EditText(StoredCardActivity.this);
                    LinearLayout linearLayout = new LinearLayout(StoredCardActivity.this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(50, 0, 50, 0);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    int cvvLength;
                    try {
                        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(selectedCard.getString("card_no").matches("^3[47]+[0-9|X]*") ? 4 : 3)});
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    input.setBackgroundResource(com.payu.sdk.R.drawable.rectangle_box);
                    input.setLines(1);
                    input.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(com.payu.sdk.R.drawable.lock), null);
                    linearLayout.addView(input, layoutParams);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(StoredCardActivity.this);
                    builder.setView(linearLayout);
                    builder.setTitle(Constants.CVV_TITLE);
                    builder.setMessage(Constants.CVV_MESSAGE);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            makePayment(selectedCard, input.getText().toString());
                        }
                    });

                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    final AlertDialog dialog = builder.create();

                    if (!dialog.isShowing())
                        dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // initially ok button is disabled

                    input.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            try {
                                if ((selectedCard.getString("card_no").matches("^3[47]+[0-9|X]*") && input.getText().length() == 4) || (!selectedCard.getString("card_no").matches("^3[47]+[0-9|X]*")) && input.getText().length() == 3) { // ok allow the user to make payment
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                } else {// no dont allow the user to make payment
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {// delete card.
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) { // to prevent double click
                        return false;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    final JSONObject selectedCard = (JSONObject) parent.getAdapter().getItem(position);

                    confirmDelete(selectedCard);

                    return false;
                }
            });
        }
    }

    @Override
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null  && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onGetResponse(String responseMessage) {
        if(getBaseContext()!=null && !isFinishing()){
            if(Constants.DEBUG){
                Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
            }

            if(mProgressDialog != null  && mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }

            if(responseMessage.contains("deleted successfully")){
                fetchStoredCards();
            }

            if(PayU.storedCards != null){
                setupAdapter();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            setResult(resultCode, data);
            finish();
        }
    }
}
