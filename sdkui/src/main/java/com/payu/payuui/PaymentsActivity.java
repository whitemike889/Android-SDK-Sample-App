package com.payu.payuui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaymentsActivity extends AppCompatActivity {

    Bundle bundle;
    String url;
    boolean cancelTransaction = false;
    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;
    private String UTF = "UTF-8";
    private  boolean viewPortWide = false;
    private WebView mWebView;
    private PayuUtils mPayuUtils;

    private int storeOneClickHash;
    private Boolean smsPermission;

    private String merchantHash;
    String txnId = null;

    private String payuReponse; // response received from payu js interface
    private String merchantResponse; // response received from surl/furl js interface
    private Boolean isSuccessTransaction; // status of the transaction should be set from payu's js interface functions.
    private ProgressDialog mProgressDialog;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */

        if(savedInstanceState!=null){
            super.onCreate(null);
            finish();//call activity u want to as activity is being destroyed it is restarted
        }else {
            super.onCreate(savedInstanceState);
        }
        setContentView(R.layout.activity_payments);
        mWebView = (WebView) findViewById(R.id.webview);

        mPayuUtils = new PayuUtils();

        //region Replace the whole code by the commented code if you are NOT using custombrowser
        // Replace the whole code by the commented code if you are NOT using custombrowser.

        bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        mWebView = (WebView) findViewById(R.id.webview);

        switch (payuConfig.getEnvironment()) {
            case PayuConstants.PRODUCTION_ENV:
                url = PayuConstants.PRODUCTION_PAYMENT_URL;
                break;
            case PayuConstants.MOBILE_STAGING_ENV:
                url = PayuConstants.MOBILE_TEST_PAYMENT_URL;
                break;
            case PayuConstants.STAGING_ENV:
                url = PayuConstants.TEST_PAYMENT_URL;
                break;
            default:
                url = PayuConstants.PRODUCTION_PAYMENT_URL;
                break;
        }

        String [] list =  payuConfig.getData().split("&");

        String merchantKey = null;
        for (String item : list) {
            String[] items = item.split("=");
            if(items.length >= 2) {
                String id = items[0];
                switch (id) {
                    case "txnid":
                        txnId = items[1];
                        break;
                    case "key":
                        merchantKey = items[1];
                        break;
                    case "pg":
                        if (items[1].contentEquals("NB")) {
                            viewPortWide = true;
                        }
                        break;

                }
            }
        }

            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setSupportMultipleWindows(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            // Setting view port for NB
            if(viewPortWide){
                mWebView.getSettings().setUseWideViewPort(viewPortWide);
            }
            // Hiding the overlay
            View transOverlay = findViewById(R.id.trans_overlay);
            transOverlay.setVisibility(View.GONE);

            mWebView.addJavascriptInterface(new Object() {

                /**
                 * Call back from surl - sucess transaction
                 * with no argument.
                 * just send empty string back to calling activity
                 */
                @JavascriptInterface
                public void onSuccess() {
                    onSuccess("");
                }

                /**
                 * call back function from surl - success transaction.
                 * keep the data in {@link PaymentsActivity#merchantResponse}
                 * @param result
                 */
                @JavascriptInterface
                public void onSuccess(final String result) {
                    merchantResponse = result;
                }

                /**
                 * Attempt to deprecate surl.
                 * Javascript interface call from payu server.
                 * Lets keep the data in local variable and pass it to main activity.
                 * @param result json data of post param.
                 */

                @JavascriptInterface
                public void onPayuSuccess(final String result) {

                    isSuccessTransaction = true;
                    payuReponse = result;

                    if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_MOBILE) { // store it only if i need to store it
                        try {
                            JSONObject hashObject = new JSONObject(payuReponse);
                            // store the cvv in shared preferences.
                            new PayuUtils().storeInSharedPreferences(PaymentsActivity.this, hashObject.getString(PayuConstants.CARD_TOKEN), hashObject.getString(PayuConstants.MERCHANT_HASH));
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                    callTimer();
                }

                /**
                 * Call back from furl - failure transaction
                 * with no argument.
                 * just send empty string back to calling activity
                 */
                @JavascriptInterface
                public void onFailure() {
                    onFailure("");
                }


                /**
                 * call back function from furl - failure transaction.
                 * keep the value in {@link PaymentsActivity#merchantResponse}
                 * @param result
                 */
                @JavascriptInterface
                public void onFailure(final String result) {
                    merchantResponse = result;
                }

                /**
                 * Attempt to deprecate furl.
                 * Javascript call from payu server.
                 * Lets keep the data in local variable and pass it to calling activity.
                 * @param result
                 */
                @JavascriptInterface
                public void onPayuFailure(final String result) {
                    isSuccessTransaction = false;
                    payuReponse = result;
                    callTimer();
                }

                @JavascriptInterface
                @Deprecated
                public void onMerchantHashReceived(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (storeOneClickHash) {
                                case PayuConstants.STORE_ONE_CLICK_HASH_MOBILE:
                                    try {
                                        JSONObject hashObject = new JSONObject(result);
                                        // store the cvv in shared preferences.
                                        new PayuUtils().storeInSharedPreferences(PaymentsActivity.this, hashObject.getString(PayuConstants.CARD_TOKEN), hashObject.getString(PayuConstants.MERCHANT_HASH));
                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }
                                    break;
                                case PayuConstants.STORE_ONE_CLICK_HASH_SERVER:
                                    merchantHash = result;
                                    break;
                                case PayuConstants.STORE_ONE_CLICK_HASH_NONE:
                                    break;
                            }
                        }
                    });
                }
            }, "PayU");


            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mWebView.setWebChromeClient(new WebChromeClient() );
            mWebView.setWebViewClient(new WebViewClient() {
                // flag to tell whether surl or furl loaded.
                private boolean isMerchantUrlStarted;

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    mProgressDialog.show();
                    if(isPayuResponseReceived()) // loading either surl or furl.
                        isMerchantUrlStarted = true;

                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mProgressDialog.dismiss();
                    if(isMerchantUrlStarted){ // finishing surl or furl
                        // finish the activity.
                        onMerchantUrlFinished();
                    }
                }
            });
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.postUrl(url, payuConfig.getData().getBytes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payments, menu);
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

    @Override
    public void onBackPressed(){
        if(cancelTransaction){
            cancelTransaction = false;
            Intent intent = new Intent();
            intent.putExtra("result", "Transaction canceled due to back pressed!");
            setResult(RESULT_CANCELED, intent);
            super.onBackPressed();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);;
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Do you really want to cancel the transaction ?");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTransaction = true;
                dialog.dismiss();
                onBackPressed();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    /**
     * Helper function return the availablity of payu response given by nPayuSuccess(String)
     * @return true or false.
     */
    boolean isPayuResponseReceived(){
        if(null != payuReponse) return true;
        return false;
    }


    /**
     * Just to make sure we finish activity even if the merchant's url got into trouble,
     * should be called from onPayuSuccess(String) or onPayuFailure(String)
     */
    void callTimer(){
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                // tick tick tick tick....
            }

            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            onMerchantUrlFinished();
                        }
                    }
                });

            }
        }.start();
    }

    /**
     * This function takes care of sending the data back to calling activity with the status, merchantResponse, payuresponse.
     */
    public void onMerchantUrlFinished(){
        // finish the activity.
        // finish the activity.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    Intent intent = new Intent();
                    intent.putExtra("result", merchantResponse);
                    intent.putExtra("payu_response", payuReponse);
                    if (storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash) {
                        intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                    }
                    if (isSuccessTransaction)
                        setResult(Activity.RESULT_OK, intent);
                    else
                        setResult(Activity.RESULT_CANCELED, intent);


                    finish();
                }
            }
        });
    }
}