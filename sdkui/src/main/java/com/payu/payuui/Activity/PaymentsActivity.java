package com.payu.payuui.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import com.payu.custombrowser.Bank;
import com.payu.custombrowser.CustomBrowser;
import com.payu.custombrowser.PayUCustomBrowserCallback;
import com.payu.custombrowser.PayUWebChromeClient;
import com.payu.custombrowser.PayUWebViewClient;
import com.payu.custombrowser.bean.CustomBrowserConfig;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuUtils;
import com.payu.magicretry.MagicRetryFragment;
import com.payu.payuui.R;
import java.util.HashMap;
import java.util.Map;

public class PaymentsActivity extends FragmentActivity {
    Bundle bundle;
    String url;
    boolean cancelTransaction = false;
    PayuConfig payuConfig;
    private BroadcastReceiver mReceiver = null;
    private String UTF = "UTF-8";
    private  boolean viewPortWide = false;
    private PayuUtils mPayuUtils;
    private int storeOneClickHash;
    private String merchantHash;
    MagicRetryFragment magicRetryFragment;
    String txnId = null;
    String merchantKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mPayuUtils = new PayuUtils();
            bundle = getIntent().getExtras();
            payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
            storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
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
                        case "pg":
                            if (items[1].contentEquals("NB")) {
                                viewPortWide = true;
                            }
                            break;

                    }
                }
            }

            //set callback to track important events
            PayUCustomBrowserCallback payUCustomBrowserCallback = new PayUCustomBrowserCallback() {

                /**
                 * This method will be called after a failed transaction.
                 * @param  payuResponse response sent by PayU in App
                 * @param merchantResponse response received from Furl
                 * */
                @Override
                public void onPaymentFailure(String payuResponse,String merchantResponse) {
                    Intent intent = new Intent();
                    intent.putExtra(getString(R.string.cb_result), merchantResponse);
                    intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                    if(storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash){
                        intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                    }
                    setResult(Activity.RESULT_CANCELED, intent);
                    finish();
                }

                @Override
                public void onPaymentTerminate() {
                }

                /**
                 * This method will be called after a successful transaction.
                 * @param  payuResponse response sent by PayU in App
                 * @param merchantResponse response received from Furl
                 * */
                @Override
                public void onPaymentSuccess(String payuResponse,String merchantResponse) {
                    Intent intent = new Intent();
                    intent.putExtra(getString(R.string.cb_result), merchantResponse);
                    intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                    if(storeOneClickHash == PayuConstants.STORE_ONE_CLICK_HASH_SERVER && null != merchantHash){
                        intent.putExtra(PayuConstants.MERCHANT_HASH, merchantHash);
                    }
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onCBErrorReceived(int code, String errormsg) {
                }


                @Override
                public void setCBProperties(WebView webview, Bank payUCustomBrowser) {
                    webview.setWebChromeClient(new PayUWebChromeClient(payUCustomBrowser));
                    webview.setWebViewClient(new PayUWebViewClient(payUCustomBrowser,merchantKey));
                }

                @Override
                public void onBackApprove() {
                    PaymentsActivity.this.finish();
                }

                @Override
                public void onBackDismiss() {
                    super.onBackDismiss();
                }

                /**
                 * This callback method will be invoked when setDisableBackButtonDialog is set to true.
                 * @param alertDialogBuilder a reference of AlertDialog.Builder to customize the dialog
                 * */
                @Override
                public void onBackButton(AlertDialog.Builder alertDialogBuilder) {
                    super.onBackButton(alertDialogBuilder);
                }

                @Override
                public void initializeMagicRetry(Bank payUCustomBrowser,WebView webview,MagicRetryFragment magicRetryFragment){
                    webview.setWebViewClient(new PayUWebViewClient(payUCustomBrowser, magicRetryFragment,merchantKey));
                    Map<String, String> urlList = new HashMap<String, String>();
                    urlList.put(url, payuConfig.getData());
                    payUCustomBrowser.setMagicRetry(urlList);
                }

            };

            //Sets the configuration of custom browser
            CustomBrowserConfig customBrowserConfig = new CustomBrowserConfig(merchantKey,txnId);
            customBrowserConfig.setViewPortWideEnable(viewPortWide);

            //TODO don't forgot to set AutoApprove and AutoSelectOTP to true for One Tap payments
            customBrowserConfig.setAutoApprove(false);
            customBrowserConfig.setAutoSelectOTP(false);

            //Set below flag to true to disable the default alert dialog of Custom Browser and use your custom dialog
            customBrowserConfig.setDisableBackButtonDialog(false);

            //Below flag is used for One Click Payments. It should always be set to CustomBrowserConfig.STOREONECLICKHASH_MODE_SERVER
            customBrowserConfig.setStoreOneClickHash(CustomBrowserConfig.STOREONECLICKHASH_MODE_SERVER);

            //Set it to true to enable run time permission dialog to appear for all Android 6.0 and above devices
            customBrowserConfig.setMerchantSMSPermission(false);

            //Set it to true to enable Magic retry
            customBrowserConfig.setmagicRetry(true);

            //Set the first url to open in WebView
            customBrowserConfig.setPostURL(url);

            ////Set the data to be posted to the WebView
            customBrowserConfig.setPayuPostData(payuConfig.getData());

            new CustomBrowser().addCustomBrowser(PaymentsActivity.this,customBrowserConfig , payUCustomBrowserCallback);
        }
    }
}