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
import com.payu.sdk.adapters.NetBankingAdapter;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


public class NbActivity extends ActionBarActivity implements PaymentListener {

    Payment.Builder builder = new Payment().new Builder();
    String bankCode = "";
    Bundle defaultParams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nb);
        // lets make the first api call to fetch list of all netbanking options
//        String userCredentials = "smsplus:1b1b0";

            defaultParams= getIntent().getExtras();

            List<NameValuePair> postParams = null;

            HashMap varList = new HashMap();

            if(defaultParams.get(PayU.USER_CREDENTIALS) == null){// ok we dont have a user credentials.
                varList.put(Constants.VAR1, Constants.DEFAULT);
            }else{
                varList.put(Constants.VAR1, defaultParams.get(PayU.USER_CREDENTIALS)); // this will return the storedCards as well
            }

            try {
                postParams = PayU.getInstance(NbActivity.this).getParams(Constants.PAYMENT_RELATED_DETAILS, varList);
                GetResponseTask getResponse = new GetResponseTask(NbActivity.this);
                getResponse.execute(postParams);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        if(Constants.ENABLE_VAS && PayU.netBankingStatus == null){
            HashMap<String, String> netBankingVarList = new HashMap<String, String>();

            netBankingVarList.put("var1", "default");
            netBankingVarList.put("var2", "default");
            netBankingVarList.put("var3", "default");

            List<NameValuePair> netBankingPostParams = null;

            try {
                netBankingPostParams = PayU.getInstance(this).getParams(Constants.GET_VAS, netBankingVarList);
                GetResponseTask getResponse = new GetResponseTask(NbActivity.this);
                getResponse.execute(netBankingPostParams);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        findViewById(com.payu.sdk.R.id.nbPayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Payment.Builder builder = new Payment().new Builder();
                    Params requiredParams = new Params();

                    builder.set(PayU.PRODUCT_INFO, defaultParams.getString(PayU.PRODUCT_INFO));
                    builder.set(PayU.AMOUNT, defaultParams.getString(PayU.AMOUNT));
                    builder.set(PayU.TXNID, defaultParams.getString(PayU.TXNID));
                    builder.set(PayU.SURL, defaultParams.getString(PayU.SURL));
                    builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.NB));

                    requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
                    requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
                    requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
                    requiredParams.put(PayU.SURL, builder.get(PayU.SURL));
                    requiredParams.put(PayU.BANKCODE, bankCode);
                    Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
                    requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                    Payment payment = builder.create();

                    String postData = PayU.getInstance(NbActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(NbActivity.this, ProcessPaymentActivity.class);
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
        if(PayU.availableBanks != null)
            setupAdapter();
        if(Constants.DEBUG)
            Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
    }


    private void setupAdapter() {

        NetBankingAdapter adapter = new NetBankingAdapter(this, PayU.availableBanks);

        Spinner netBankingSpinner = (Spinner) findViewById(com.payu.sdk.R.id.netBankingSpinner);
        netBankingSpinner.setAdapter(adapter);

        netBankingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                try {
                    bankCode = ((JSONObject) adapterView.getAdapter().getItem(i)).getString("code");
                    if (bankCode.contentEquals("default")) {
                        findViewById(com.payu.sdk.R.id.nbPayButton).setEnabled(false);
                    } else {
                        if (PayU.netBankingStatus != null && PayU.netBankingStatus.get(bankCode) == 0) {
                            ((TextView) findViewById(com.payu.sdk.R.id.netBankingErrorText)).setText("Oops! " + ((JSONObject) adapterView.getAdapter().getItem(i)).getString("title") + " seems to be down. We recommend you pay using any other means of payment.");
                            findViewById(com.payu.sdk.R.id.netBankingErrorText).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(com.payu.sdk.R.id.netBankingErrorText).setVisibility(View.GONE);
                        }
                        findViewById(com.payu.sdk.R.id.nbPayButton).setEnabled(true);
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
