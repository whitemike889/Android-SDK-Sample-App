package com.payu.payuui.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;
import com.payu.magicretry.MagicRetryFragment;
import com.payu.payuui.R;
import com.payu.phonepe.PhonePe;
import com.payu.phonepe.callbacks.PayUPhonePeCallback;
import com.payu.upisdk.PaymentOption;
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
    boolean cancelTransaction = false;
    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;
    private String UTF = "UTF-8";
    private boolean viewPortWide = false;
    private PayuUtils mPayuUtils;
    private int storeOneClickHash;
    private String merchantHash;
    MagicRetryFragment magicRetryFragment;
    String txnId = null;
    String merchantKey;
    boolean isPhonePeOnlyFlow = false;
    String magicRetry;
    String userCredentials;
    private Upi upi;
    private static String TAG = "PayU";

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("PayU", "class name:" + getClass().getCanonicalName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("PayU", "oncreate class name:" + getClass().getCanonicalName());
        //  setContentView(R.layout.activity_payments);
      //  Toast.makeText(this, "PaymentsActivity", Toast.LENGTH_SHORT).show();
        if (savedInstanceState == null) {
            mPayuUtils = new PayuUtils();
            bundle = getIntent().getExtras();
            payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
            storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
            magicRetry = bundle.getString("magic_retry");
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
                        case "pg":
                            if (items[1].contentEquals("NB")) {
                                viewPortWide = true;
                            }
                            break;
                    }
                }
            }

        }
        paymentThroughSDKFlow();
      // paymentThroughCustomBrowserFlow();
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
        upiConfig.setPayuPostData(payuConfig.getData());
       // upiConfig.setPostUrl("https://test.payu.in");
        upi = Upi.getInstance();
        Log.v("PayU", "Data PayUConfig " + payuConfig.getData());
      ///  UpiUtil cbUtil = new UpiUtil();
        Log.v(TAG, "isPhonePay: pre");
        String bankCode = getDataFromPostData(payuConfig.getData()).get(UpiConstant.BANK_CODE);
        //TODO: HOW TO Change isPhonePeOnlyFlow
     /*   if (isPhonePeOnlyFlow && bankCode.equalsIgnoreCase(PaymentOption.PHONE_PE.getPaymentName())) {
            Log.v(TAG, "isPhonePay: " + isPhonePeOnlyFlow);
           // upiConfig.setPaymentType(PaymentOption.PHONE_PE.getPaymentName());
           initPhonePe(upiConfig.getPayuPostData(), true);
        } else {
            Log.v(TAG, "isPhonePay: else" + bankCode);
            if (bankCode.equalsIgnoreCase(PaymentOption.TEZ.getPaymentName())) {
                Log.v(TAG, " Payment Option " + bankCode);

                upiConfig.setPaymentType(UpiConstant.TEZ);
            }
            Log.v(TAG, "Mode " + bankCode);
            if (bankCode.equals(PaymentOption.UPI_INTENT.getPaymentName())) {
                Log.v("PayU", "Upi Intent");
                upiConfig.setPaymentType(PaymentOption.UPI_INTENT.getPaymentName());
            }
            if (bankCode.equals(PaymentOption.UPI_COLLECT.getPaymentName())) {
                upiConfig.setPaymentType(PaymentOption.UPI_COLLECT.getPaymentName());

            }
            if (bankCode.equals(PaymentOption.PHONE_PE.getPaymentName())) {
                Log.v("PayU", "PhonePe through upisdk");
                upiConfig.setPaymentType(PaymentOption.PHONE_PE.getPaymentName());
            }//

            if (bankCode.equals(PaymentOption.SAMSUNG_PAY.getPaymentName())) {
                upiConfig.setPaymentType(UpiConstant.SAM_PAY);

            }
            paymentThroughUpiSdk(payUUpiSdkCallbackUpiSdk, upiConfig);
        }*/

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
            if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void onPaymentTerminate() {
            super.onPaymentTerminate();

            finish();
        }

        @Override
        public void onPaymentSuccess(String payuResult, String merchantResponse) {
            super.onPaymentSuccess(payuResult, merchantResponse);
            Log.v("PayU", "onPaymentSuccess .. " + payuResult);
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.cb_result), merchantResponse);
            intent.putExtra(getString(R.string.cb_payu_response), payuResult);
            if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
            }
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

    private void initPhonePe(String postData, boolean isUserPhonePeCacheEnabled) {
        PayUPhonePeCallback payUPhonePeCallback = new PayUPhonePeCallback() {
            @Override
            public void onPaymentOptionSuccess(String payuResponse) {
                super.onPaymentOptionSuccess(payuResponse);
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.cb_result), "");
                intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                    intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onPaymentOptionFailure(String payuResponse) {
                super.onPaymentOptionFailure(payuResponse);
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.cb_result), "");
                intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                    intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                }
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        };
        PhonePe phonepe = PhonePe.getInstance();
        phonepe.makePayment(payUPhonePeCallback, this, postData, isUserPhonePeCacheEnabled);
    }

/*    private void initTez(String postData) {
       // PayULogger.createLogger("postData " + postData);
        PayUTezCallback payUTezCallback = new PayUTezCallback() {
            @Override
            public void onPaymentOptionSuccess(String payuResponse) {

                super.onPaymentOptionSuccess(payuResponse);
              //  PayULogger.createLogger("PayU Response " + payuResponse);
            }

            @Override
            public void onPaymentOptionFailure(String errorMsg) {
                super.onPaymentOptionFailure(errorMsg);
                PayULogger.createLogger("PayU Error " + errorMsg);
            }

            @Override
            public void onPaymentOptionInitialisationSuccess() {
                super.onPaymentOptionInitialisationSuccess();
            }

            @Override
            public void onPaymentOptionInitialisationFailure(int errorCode, String description) {
                super.onPaymentOptionInitialisationFailure(errorCode, description);
            }
        };
        //Tez.getInstance().makePayment(this, postData,payUTezCallback);
        // Tez.getInstance().initiateTezPayment(PaymentsActivity.this, postData, payUTezCallback);
    }*/

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
