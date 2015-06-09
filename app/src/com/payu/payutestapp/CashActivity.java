package com.payu.payutestapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.PaymentListener;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.adapters.CashCardAdapter;
import com.payu.sdk.adapters.NetBankingAdapter;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;
import com.payu.sdk.fragments.CardsFragment;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


public class CashActivity extends ActionBarActivity implements PaymentListener {

    Payment.Builder builder = new Payment().new Builder();
    String bankCode = "";

    Bundle defaultParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);

            defaultParams = getIntent().getExtras();


            List<NameValuePair> postParams = null;

            HashMap varList = new HashMap();

            if(defaultParams.getString(PayU.USER_CREDENTIALS) == null){// ok we dont have a user credentials.
                varList.put(Constants.VAR1, Constants.DEFAULT);
            }else{
                varList.put(Constants.VAR1, defaultParams.get(PayU.USER_CREDENTIALS)); // this will return the storedCards as well
            }

            try {
                postParams = PayU.getInstance(CashActivity.this).getParams(Constants.PAYMENT_RELATED_DETAILS, varList);
                GetResponseTask getResponse = new GetResponseTask(CashActivity.this);
                getResponse.execute(postParams);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        findViewById(com.payu.sdk.R.id.cashCardMakePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Payment.Builder builder = new Payment().new Builder();
                    Params requiredParams = new Params();

                    for(String key: defaultParams.keySet()){
                        builder.set(key, defaultParams.getString(key));
                        requiredParams.put(key, defaultParams.getString(key));
                    }

                    /*builder.set(PayU.PRODUCT_INFO, defaultParams.getString(PayU.PRODUCT_INFO));
                    builder.set(PayU.AMOUNT, defaultParams.getString(PayU.AMOUNT));
                    builder.set(PayU.TXNID, defaultParams.getString(PayU.TXNID));
                    builder.set(PayU.SURL, "https://dl.dropboxusercontent.com/s/dtnvwz5p4uymjvg/success.html");
                    builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.NB));*/

//                    requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
//                    requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
//                    requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
//                    requiredParams.put(PayU.SURL, builder.get(PayU.SURL));

                    builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.CASH));
                    requiredParams.put(PayU.BANKCODE, bankCode);
                    Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
                    requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                    Payment payment = builder.create();

                    String postData = PayU.getInstance(CashActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(CashActivity.this, ProcessPaymentActivity.class);
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
    }


    @Override
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    @Override
    public void onGetResponse(String responseMessage) {
        if(PayU.availableCashCards != null)
            setupAdapter();
        if(Constants.DEBUG)
            Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
    }


    private void setupAdapter() {

        CashCardAdapter adapter = new CashCardAdapter(this, PayU.availableCashCards);

        Spinner cashCardSpinner = (Spinner) findViewById(com.payu.sdk.R.id.cashCardSpinner);
        cashCardSpinner.setAdapter(adapter);

        cashCardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    bankCode = ((JSONObject) adapterView.getAdapter().getItem(i)).getString("code");

                    if (bankCode.contentEquals("default")) {
                        //disable the button
//                        getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button);
                       findViewById(R.id.cashCardMakePayment).setEnabled(false);
                    } else {
                        //enable the button
//                        getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button_enabled);
                        findViewById(R.id.cashCardMakePayment).setEnabled(true);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            setResult(resultCode, data);
            finish();
        }
    }

}
