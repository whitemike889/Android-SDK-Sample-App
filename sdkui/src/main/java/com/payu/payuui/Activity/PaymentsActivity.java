package com.payu.payuui.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.payu.payuui.R;
import com.payu.upisdk.Upi;
import com.payu.upisdk.bean.UpiConfig;
import com.payu.upisdk.callbacks.PayUUPICallback;
import com.payu.upisdk.upi.IValidityCheck;
import com.payu.upisdk.util.UpiConstant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PaymentsActivity extends FragmentActivity {
    Bundle bundle;
    String url;
    String txnId = null;
    String prod_url = "https://secure.payu.in/_payment";
    String staging_url = "https://test.payu.in/_payment";
    private static int prodEnvironment =0;
    String merchantKey;
    String userCredentials;
    private Upi upi;
    UpiConfig upiConfig;
    String postData;
    private int environment;

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("PayU", "class name:" + getClass().getCanonicalName());
    }
;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("PayU", "oncreate class name:" + getClass().getCanonicalName());
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            environment = bundle.getInt("environment");
            upiConfig = bundle.getParcelable(UpiConstant.UPI_CONFIG);
            url = environment == prodEnvironment? prod_url : staging_url;
            postData = upiConfig.getPayuPostData();
            String[] list = upiConfig.getPayuPostData().split("&");

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
        paymentThroughSDKFlow();
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

    private void paymentThroughSDKFlow()
    {
        UpiConfig upiConfig = new UpiConfig();
        upiConfig.setMerchantKey(merchantKey);
        upiConfig.setPayuPostData(postData);
        upi = Upi.getInstance(); // setting the context of UPI
        paymentThroughUpiSdk(payUUpiSdkCallbackUpiSdk, upiConfig);


    }
    private void paymentThroughUpiSdk(PayUUPICallback payUUpiSdkCallbackUpiSdk, UpiConfig upiConfig) {
        upi.makePayment(payUUpiSdkCallbackUpiSdk, PaymentsActivity.this, upiConfig);
    }

    PayUUPICallback payUUpiSdkCallbackUpiSdk = new PayUUPICallback() {
        @Override
        public void onPaymentFailure(String payuResult, String merchantResponse) {
            super.onPaymentFailure(payuResult, merchantResponse);
            Log.v("PayU", "Paymentfail .. " + payuResult);
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.cb_result), merchantResponse);
            intent.putExtra(getString(R.string.cb_payu_response), payuResult);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void onPaymentTerminate() {
            super.onPaymentTerminate();
        }

        @Override
        public void onPaymentSuccess(String payuResult, String merchantResponse) {
            super.onPaymentSuccess(payuResult, merchantResponse);
            Log.v("PayU", "onPaymentSuccess .. " + payuResult);
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.cb_result), merchantResponse);
            intent.putExtra(getString(R.string.cb_payu_response), payuResult);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }



       /* @Override
        public void onVpaEntered(com.payu.upisdk.upi.IValidityCheck iValidityCheck, String vpa) {
            super.onVpaEntered(iValidityCheck, vpa);
            Log.v("PayU", "onVpaEntered .. ");
            String input = merchantKey + "|validateVPA|" + vpa + "|" + bundle.getString(PayuConstants.SALT);
            iValidityCheck.verifyVpa(calculateHash(input));
        }*/


        @Override
        public void onVpaEntered(String vpa, IValidityCheck iValidityCheck) {
            super.onVpaEntered(vpa, iValidityCheck);
            Log.v("PayU", "onVpaEntered .. ");
            String input = merchantKey + "|validateVPA|" + vpa + "|" + "1b1b0"; // recommended to generate the validate vpa hash from server
            iValidityCheck.verifyVpa(calculateHash(input));

           // bundle.getString(PayuConstants.SALT
        }

        @Override
        public void onUpiErrorReceived(int code, String errormsg) {
            super.onUpiErrorReceived(code, errormsg);
            Log.v("PayU", "onUpiErrorReceived .. " + errormsg);
        }


        @Override
        public void onBackButton(AlertDialog.Builder alertDialogBuilder) {
            super.onBackButton(alertDialogBuilder);
            Log.v("PayU", "onBackButton .. ");
        }

        @Override
        public void onBackApprove() {
            super.onBackApprove();
            Log.v("PayU", "onBackApprove .. ");
        }

        @Override
        public void onBackDismiss() {
            super.onBackDismiss();
            Log.v("PayU", "onBackDismiss .. ");
        }


        @Override
        public void isPaymentOptionAvailable(boolean isAvaialble, String paymentType) {
            super.isPaymentOptionAvailable(isAvaialble, paymentType);
            Log.v("PayU", "isPaymentOptionAvailable .. " + isAvaialble);
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
