package com.payu.payuui.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;
import com.payu.phonepe.PhonePe;
import com.payu.phonepe.callbacks.PayUPhonePeCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PaymentsActivity extends FragmentActivity {
    Bundle bundle;
    String url;
    PayuConfig payuConfig;
    private String UTF = "UTF-8";
    String txnId = null;
    String merchantKey;
    boolean isPhonePeOnlyFlow = false;
    String userCredentials;


    @Override
    protected void onResume() {
        super.onResume();
        Log.v("PayU", "class name:" + getClass().getCanonicalName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("PayU", "oncreate class name:" + getClass().getCanonicalName());
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
            url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV ? PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.TEST_PAYMENT_URL;
            String[] list = payuConfig.getData().split("&");

            for (String item : list) {
                String[] items = item.split("=");
                if (items.length >= 2) {
                    String id = items[0];
                    switch (id) {
                        case "txnid":
                            txnId = items[1];
                            break;
                        case "key":
                            merchantKey = items[1];
                            break;
                        case "var1":
                            userCredentials = items[1];
                            break;
                    }
                }
            }

        }

        paymentThroughPhonepeStandalone();

    }

    private HashMap<String, String> getDataFromPostData(String postData) {
        HashMap<String, String> postParamsMap = new HashMap<>();
        if (null != postData) {
            StringTokenizer tokens = new StringTokenizer(postData, "&");
            while (tokens.hasMoreTokens()) {
                String[] keyValue = tokens.nextToken().split("=");
                if (null != keyValue && keyValue.length > 0 && null != keyValue[0]) {
                    postParamsMap.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
                }
            }
        }
        return postParamsMap;
    }




    private void paymentThroughPhonepeStandalone(){
        PhonePe.getInstance().makePayment(payUPhonePeCallback,this,payuConfig.getData(),true);
    }

    // Callback for PhonePe
    PayUPhonePeCallback payUPhonePeCallback = new PayUPhonePeCallback() {
        @Override
        public void onPaymentOptionFailure (String payuResponse) {
            Intent intent = new Intent();
            intent.putExtra("payu_response", payuResponse);
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }

        @Override
        public void onPaymentOptionInitialisationSuccess(boolean result) {
            super.onPaymentOptionInitialisationSuccess(result);
            // Merchants are advised to show PhonePe option on their UI after this callback is called.
        }

        @Override
        public void onPaymentOptionSuccess(String payuResponse) {
            Intent intent = new Intent();
            intent.putExtra("payu_response", payuResponse);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void onPaymentOptionInitialisationFailure (int errorCode, String description) {
            //Callback thrown in case PhonePe initialisation fails.
        }

    };






    private String calculateHash(String inputString) {
        try {
            StringBuilder hash = new StringBuilder();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(inputString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            for (byte hashByte : mdbytes) {
                hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
