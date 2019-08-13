package com.payu.payuui.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.payu.gpay.GPay;
import com.payu.gpay.callbacks.PayUGPayCallback;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.SwipeTab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.HashMap;


public class PayUBaseActivity extends FragmentActivity implements View.OnClickListener {

    public Bundle bundle;
    ArrayList<String> paymentOptionsList = new ArrayList<String>();
    PayuConfig payuConfig;
    PaymentParams mPaymentParams;
    PayuHashes mPayUHashes;
    PayuUtils mPayuUtils;
    public PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;
    private Button payNowButton;
    private Boolean smsPermission;
    private PostData mPostData;
    HashMap<String, String> oneClickCardTokens;
    private int storeOneClickHash;
    private boolean isGPaySupported = false;
    /**

     * Callback of payment availability while doing through GPay SDK.
     */
   PayUGPayCallback payUGPayCallback = new PayUGPayCallback(){

        @Override
        public void onPaymentInitialisationSuccess() {
            super.onPaymentInitialisationSuccess();
            isGPaySupported = true;
            paymentOptionsList.add(SdkUIConstants.TEZ);
            setupViewPager();

        }

        @Override
        public void onPaymentInitialisationFailure(int errorCode, String description) {
            super.onPaymentInitialisationFailure(errorCode, description);
            isGPaySupported = false;
        }
    };

    private void setupViewPager() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), paymentOptionsList);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTabLayout.setDistributeEvenly(false);

        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        slidingTabLayout.setViewPager(viewPager);
        payNowButton.setEnabled(true);
        payNowButton.setOnClickListener(this);
        findViewById(R.id.progress_bar).setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onClick(View view) {

        if (view.getId() == R.id.button_pay_now) {
            Log.v("PayU", "Payu btn ");
            mPostData = null;
            mPaymentParams.setHash(mPayUHashes.getPaymentHash());
            switch (paymentOptionsList.get(viewPager.getCurrentItem())) {
                case SdkUIConstants.TEZ:
                    makePaymentByTez();
                    break;
            }
                if(mPostData!=null && mPostData.getCode()==PayuErrors.NO_ERROR) {
                    payuConfig.setData(mPostData.getResult());
                    Intent intent = new Intent(this, PaymentsActivity.class);
                    intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                    intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
                    intent.putExtra(PayuConstants.SMS_PERMISSION, smsPermission);
                    startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payu_base);


        (payNowButton = (Button) findViewById(R.id.button_pay_now)).setOnClickListener(this);
        bundle = getIntent().getExtras();


        payuConfig = bundle.getParcelable(PayuConstants.PAYU_CONFIG);
        payuConfig = null != payuConfig ? payuConfig : new PayuConfig();

        mPayuUtils = new PayuUtils();


        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS); // Todo change the name to PAYMENT_PARAMS
        mPayUHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        smsPermission = bundle.getBoolean(PayuConstants.SMS_PERMISSION);

        oneClickCardTokens = (HashMap<String, String>) bundle.getSerializable(PayuConstants.ONE_CLICK_CARD_TOKENS);
        ((TextView) findViewById(R.id.textview_amount)).setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        ((TextView) findViewById(R.id.textview_txnid)).setText(SdkUIConstants.TXN_ID + ": " + mPaymentParams.getTxnId());
        initUsingGPaySDK();
    }

    /**
     * Payment by GPay
     */
    private void makePaymentByTez() {
        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.TEZ).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }

    }

    private void initUsingGPaySDK() {

        GPay.getInstance().checkForPaymentAvailability(this,payUGPayCallback,mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(),mPaymentParams.getKey(),mPaymentParams.getUserCredentials());

    }

}
