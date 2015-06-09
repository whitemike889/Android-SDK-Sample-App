package com.payu.payutestapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.payu.sdk.Constants;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;


public class BaseActivity extends ActionBarActivity {

    Bundle defaultParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        defaultParam = getIntent().getExtras();
    }

    public void creditCardPayment(View view){
        startPayment(new Intent(this, CardActivity.class));
    }

    public void nbMakePayment(View view){
        startPayment(new Intent(this, NbActivity.class));
    }

    public void cashCardMakePayment(View view){
        startPayment(new Intent(this, CashActivity.class));
    }

    public void storedCardPayment(View view){
        startPayment(new Intent(this, StoredCardActivity.class));
    }

    public void emiPayment(View view){
        Intent intent = new Intent(this, EmiActivity.class);
        startPayment(intent);
    }

    public void storeUserCard(View view){
        Intent intent = new Intent(this, StoreUserCardActivity.class);
        startPayment(intent);
    }

    public void startPayment(Intent intent){
        for(String key : defaultParam.keySet()){
            intent.putExtra(key, getIntent().getStringExtra(key));
        }
        startActivityForResult(intent, PayU.RESULT);
    }
    public void payumoneyMakePayment(View view) throws PackageManager.NameNotFoundException, MissingParameterException, HashException {
        // oops handle it here.

//        Payment payment;
//        Payment.Builder builder = new Payment().new Builder();
//        Params requiredParams = new Params();
//        requiredParams.put("service_provider", "payu_paisa");


//        payment = builder.create();

//        String postData = PayU.getInstance(this).createPayment(payment, requiredParams);
//        Log.d("postdata", postData);
//
//        Intent intent = new Intent(this, ProcessPaymentActivity.class);
//        intent.putExtra(Constants.POST_DATA, postData);
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //


        Payment.Builder builder = new Payment().new Builder();
        Params requiredParams = new Params();

        /*builder.set(PayU.PRODUCT_INFO, defaultParam.getString(PayU.PRODUCT_INFO));
        builder.set(PayU.AMOUNT, defaultParam.getString(PayU.AMOUNT));
        builder.set(PayU.TXNID, defaultParam.getString(PayU.TXNID));
        builder.set(PayU.SURL, defaultParam.getString(PayU.SURL));
        builder.set(PayU.FURL, defaultParam.getString(PayU.FURL));
        builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.PAYU_MONEY));

        requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
        requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
        requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
        requiredParams.put(PayU.SURL, builder.get(PayU.SURL));*/

        builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.PAYU_MONEY));
        for(String key : getIntent().getExtras().keySet()) {
            builder.set(key, String.valueOf(getIntent().getExtras().get(key)));
            requiredParams.put(key, builder.get(key));
        }

        Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
        requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
        Payment payment = builder.create();

        String postData = PayU.getInstance(BaseActivity.this).createPayment(payment, requiredParams);

        Intent intent = new Intent(this, ProcessPaymentActivity.class);
        intent.putExtra(Constants.POST_DATA, postData);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivityForResult(intent, PayU.RESULT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayU.RESULT) {

            setResult(resultCode, data);
            finish();
        }
    }
}
