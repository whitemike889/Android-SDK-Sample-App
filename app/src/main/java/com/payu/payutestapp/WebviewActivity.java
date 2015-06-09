package com.payu.payutestapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import com.payu.custombrowser.Bank;
import com.payu.custombrowser.PayUWebChromeClient;
import org.apache.http.util.EncodingUtils;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;

public class WebviewActivity extends FragmentActivity {

    private WebView webView;
    private BroadcastReceiver mReceiver = null;
    private ProgressDialog progressDialog;
    private String checkValue;
    private boolean checkProgress;
    private Set<String> set;
    private int checkForInput;
    private String webviewUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(savedInstanceState!=null){
            super.onCreate(null);
            finish();//call activity u want to as activity is being destroyed it is restarted
        }else {
            super.onCreate(savedInstanceState);
        }

        setContentView(R.layout.activity_webview);


        final View activityRootView = findViewById(R.id.r_layout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    private final int DefaultKeyboardDP = 100;
                    private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);

                    private final Rect r = new Rect();

                    @Override
                    public void onGlobalLayout() {
                        // Convert the dp to pixels.
                        int estimatedKeyboardHeight = (int) TypedValue
                                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, activityRootView.getResources().getDisplayMetrics());

                        // Conclude whether the keyboard is shown or not.
                        activityRootView.getWindowVisibleDisplayFrame(r);
                        int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
                        boolean isShown = heightDiff >= estimatedKeyboardHeight;
                        Log.d("isshown==",""+isShown);
                        if (isShown) {
                            if(checkForInput==0) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
                                checkForInput = 1;
                            }
                        }

                    }
                });

        webView = (WebView) findViewById(R.id.webview);


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
                    progressBarVisibilityPayuChrome(View.GONE);
                    findViewById(R.id.parent).setVisibility(View.GONE);
                    findViewById(R.id.trans_overlay).setVisibility(View.GONE);
                }

                @Override
                public void onHelpAvailable() {
                    checkForInput=1;
                    findViewById(R.id.parent).setVisibility(View.VISIBLE);
                }

            };
            Bundle args = new Bundle();
            args.putInt("webView", R.id.webview);
            args.putInt("tranLayout",R.id.trans_overlay);
            args.putInt("mainLayout", R.id.r_layout);
            String [] list =  getIntent().getExtras().getString("postData").split("&");
            HashMap<String , String> intentMap = new HashMap<String , String>();
            for (String item : list) {
                String [] list1 =  item.split("=");
                intentMap.put(list1[0], list1[1]);
            }
            if(getIntent().getExtras().containsKey("txnid")) {
                args.putString(Bank.TXN_ID, getIntent().getStringExtra("txnid"));
            } else {
                args.putString(Bank.TXN_ID, intentMap.get("txnid"));
            }
            //args.putString(Bank.TXN_ID, "" + System.currentTimeMillis());
            if(getIntent().getExtras().containsKey("showCustom")) {
                args.putBoolean("showCustom", getIntent().getBooleanExtra("showCustom", false));
            }
            args.putBoolean("showCustom", true);
            bank.setArguments(args);
            findViewById(R.id.parent).bringToFront();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in,R.animator.face_out).add(R.id.parent, bank).commit();

            webView.setWebChromeClient(new PayUWebChromeClient(bank) {

            });

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon)
                {
                    super.onPageStarted(view, url, favicon);
                    webviewUrl=url;
                    if(set!=null && set.size()>0 && !set.contains(url)) {
                        checkProgress = true;
                    }
                    //progressBarVisibility(View.GONE);
                    if(checkValue!=null && url.contains(checkValue)) {
                        bank.update();
                    }
                }
            });



        }catch (Exception e)
        {
            e.printStackTrace();
        }

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.postUrl("https://secure.payu.in/_payment", EncodingUtils.getBytes(getIntent().getExtras().getString("postData"), "base64"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog!=null && progressDialog.isShowing())
        progressDialog.dismiss();
    }
}
