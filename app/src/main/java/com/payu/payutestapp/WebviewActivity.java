package com.payu.payutestapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.payu.custombrowser.Bank;
import com.payu.custombrowser.PayUWebChromeClient;

import org.apache.http.util.EncodingUtils;


public class WebviewActivity extends ActionBarActivity {

    WebView webView;
    ProgressDialog mProgressDialog;
    private BroadcastReceiver mReceiver = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);

        setContentView(R.layout.activity_webview);

        webView = (WebView) findViewById(R.id.webview);


        try {
            Class.forName("com.payu.custombrowser.Bank");

            Bank bank = new Bank() {
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
                }

                @Override
                public void onBankError() {
                    findViewById(R.id.parent).setVisibility(View.GONE);
                }

                @Override
                public void onHelpAvailable() {
                    findViewById(R.id.parent).setVisibility(View.VISIBLE);
                }
            };

            Bundle args = new Bundle();
            args.putInt("webView", R.id.webview);
            if(getIntent().getExtras().containsKey("showCustom")) {
                args.putString("showCustom", getIntent().getStringExtra("showCustom"));
            }
            bank.setArguments(args);

            getSupportFragmentManager().beginTransaction().add(R.id.parent, bank).commit();

            webView.setWebChromeClient(new PayUWebChromeClient(bank) {
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    mProgressDialog.setMessage("Please wait");
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    if (newProgress == 100) {
                        mProgressDialog.dismiss();
                    }
                }

            });
        } catch (ClassNotFoundException e) {
            webView.getSettings().setSupportMultipleWindows(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.addJavascriptInterface(new Object() {
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
//                }
                    });
                }
            }, "PayU");

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onCreateWindow (WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                    return false;
                }

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    mProgressDialog.setMessage("Please wait..");
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    if (newProgress == 100) {
                        mProgressDialog.dismiss();
                    }
                }
            });
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.postUrl("https://secure.payu.in/_payment", EncodingUtils.getBytes(getIntent().getExtras().getString("postData"), "base64"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);
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
}
