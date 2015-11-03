package com.payu.payuui.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.payu.custombrowser.Bank;
import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.PayUWebViewClient;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.magicretry.Helpers.Util;
import com.payu.magicretry.MagicRetryFragment;
import com.payu.magicretry.WebClient.MagicRetryWebViewClient;
import com.payu.payuui.R;

import java.util.HashMap;
import java.util.Map;


public class PaymentsActivity extends AppCompatActivity implements MagicRetryFragment.ActivityCallback{

    Bundle bundle;
    WebView mWebView;
    String url;
    boolean cancelTransaction = false;

    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;

    private String UTF = "UTF-8";
    MagicRetryFragment  magicRetryFragment;
    String txnId = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * when the device runing out of memory we dont want the user to restart the payment. rather we close it and redirect them to previous activity.
         */
        if(savedInstanceState!=null){
            super.onCreate(null);
            finish();//call  u want to as activity is being destroyed it is restarted
        }else {
            super.onCreate(savedInstanceState);
        }
        setContentView(R.layout.activity_payments);

        //region Replace the whole code by the commented code if you are NOT using custombrowser
        // Replace the whole code by the commented code if you are NOT using custombrowser.

        bundle = getIntent().getExtras();
        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);

        mWebView = (WebView) findViewById(R.id.webview);

        url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;

        mWebView.postUrl(url, payuConfig.getData().getBytes());


        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        //endregion



        try {
            Class.forName("com.payu.custombrowser.Bank");
            final Bank bank = new Bank() {
                @Override
                public void registerBroadcast(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
                    mReceiver = broadcastReceiver;
                    registerReceiver(broadcastReceiver, filter);
                }

                @Override
                public void unregisterBroadcast(BroadcastReceiver broadcastReceiver) {
                    if(mReceiver != null){
                        unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }
                }

                @Override
                public void onHelpUnavailable() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onBankError() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onHelpAvailable() {
                    findViewById(R.id.parent).setVisibility(View.VISIBLE);
                }
            };
            Bundle args = new Bundle();
            args.putInt("webView", R.id.webview);
            args.putInt("tranLayout",R.id.trans_overlay);
            args.putInt("mainLayout",R.id.r_layout);

            String [] list =  payuConfig.getData().split("&");

            String merchantKey = null;
            String pg;
            for (String item : list) {
                String k = item.split("=")[0];
                switch (k){
                    case "txnid":
                        txnId = item.split("=")[1];
                        break;
                    case "key":
                        merchantKey = item.split("=")[1];
                        break;
                    case "pg":
                        if(item.split("=")[1].contentEquals("NB"))
                            mWebView.getSettings().setUseWideViewPort(true);
                        break;
                }
            }
            args.putString(Bank.TXN_ID, txnId == null ? String.valueOf(System.currentTimeMillis()) : txnId);
            args.putString("merchantid", null != merchantKey ? merchantKey : "could not find");
            PayUSdkDetails payUSdkDetails = new PayUSdkDetails();
            args.putString("sdk_details", "VersionCode: " + payUSdkDetails.getSdkVersionCode() + ", VersionName: " + payUSdkDetails.getSdkVersionName());
            if(getIntent().getExtras().containsKey("showCustom")) {
                args.putBoolean("showCustom", getIntent().getBooleanExtra("showCustom", false));
            }
            args.putBoolean("showCustom", true);
            bank.setArguments(args);
            findViewById(R.id.parent).bringToFront();
            try {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.cb_face_out).add(R.id.parent, bank).commit();
            }catch(Exception e)
            {
                e.printStackTrace();
                finish();
            }
            // MR Integration
            initMagicRetry();

            mWebView.setWebChromeClient(new PayUWebChromeClient(bank));
            mWebView.setWebViewClient(new PayUWebViewClient(bank, magicRetryFragment));
            // After webview initialization
            // MR Integration
            magicRetryFragment.setWebView(mWebView);
            // MR Integration - initMRSettingsFromSharedPreference
            magicRetryFragment.initMRSettingsFromSharedPreference(this);

        } catch (ClassNotFoundException e) {
            mWebView.getSettings().setSupportMultipleWindows(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void onSuccess() {
                    onSuccess("");
                }

                @JavascriptInterface
                public void onSuccess(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
//                }
                    });
                }

                @JavascriptInterface
                public void onFailure() {
                    onFailure("");
                }

                @JavascriptInterface
                public void onFailure(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.putExtra("result", result);
                            setResult(RESULT_CANCELED, intent);
                            finish();
                        }
                    });
                }
            }, "PayU");

            mWebView.setWebChromeClient(new WebChromeClient() {

            });
            mWebView.setWebViewClient(new WebViewClient());
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        url = payuConfig.getEnvironment() == PayuConstants.PRODUCTION_ENV?  PayuConstants.PRODUCTION_PAYMENT_URL : PayuConstants.MOBILE_TEST_PAYMENT_URL ;
        mWebView.postUrl(url, payuConfig.getData().getBytes());
        //WebView.setWebContentsDebuggingEnabled(true);
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

    private void initMagicRetry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        magicRetryFragment = new MagicRetryFragment();
        Bundle newInformationBundle = new Bundle();
        newInformationBundle.putString(MagicRetryFragment.KEY_TXNID, txnId);
        magicRetryFragment.setArguments(newInformationBundle);

        Map<String, String> urlList = new HashMap<String, String>();
        urlList.put(url, payuConfig.getData());
        magicRetryFragment.setUrlListWithPostData(urlList);

        fragmentManager.beginTransaction().add(R.id.magic_retry_container, magicRetryFragment, "magicRetry").commit();
        // magicRetryFragment = (MagicRetryFragment) fragmentManager.findFragmentBy(R.id.magicretry_fragment);

        toggleFragmentVisibility(Util.HIDE_FRAGMENT);

        magicRetryFragment.isWhiteListingEnabled(true);
    }


    public void toggleFragmentVisibility(int flag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!isFinishing()) {
            if (flag == Util.SHOW_FRAGMENT) {
                // Show fragment
                ft.show(magicRetryFragment).commitAllowingStateLoss();
            } else if (flag == Util.HIDE_FRAGMENT) {
                // Hide fragment
                ft.hide(magicRetryFragment).commitAllowingStateLoss();
                // ft.hide(magicRetryFragment);
                Log.v("#### PAYU", "hidhing magic retry");
            }
        }
    }
    @Override
    public void showMagicRetry() {
        toggleFragmentVisibility(Util.SHOW_FRAGMENT);
    }

    @Override
    public void hideMagicRetry() {
        toggleFragmentVisibility(Util.HIDE_FRAGMENT);
    }
}
