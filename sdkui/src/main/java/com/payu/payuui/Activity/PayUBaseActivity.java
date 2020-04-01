package com.payu.payuui.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
//import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
//import com.payu.india.PostParams.PaymentPostParams;
import com.payu.paymentparamhelper.PaymentParams;
import com.payu.paymentparamhelper.PaymentPostParams;
import com.payu.paymentparamhelper.PostData;
import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.SwipeTab.SlidingTabLayout;
import com.payu.phonepe.PhonePe;
import com.payu.phonepe.callbacks.PayUPhonePeCallback;

import java.util.ArrayList;


public class PayUBaseActivity extends FragmentActivity implements View.OnClickListener {

    public Bundle bundle;
    ArrayList<String> paymentOptionsList = new ArrayList<String>();
    PayuConfig payuConfig;
    PaymentParams mPaymentParams;
  //  PaymentPostParams paymentPostParams;
    PayuHashes mPayUHashes;
    PayuUtils mPayuUtils;
    public PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;
    private TextView amountTextView;
    private TextView transactionIdTextView;
    private Button payNowButton;
    private PostData mPostData;
    /**
     * Callback of payment availability while doing through PhonePE.
     */
    PayUPhonePeCallback callback = new PayUPhonePeCallback(){

        @Override
        public void onPaymentOptionInitialisationSuccess(boolean result) {
            super.onPaymentOptionInitialisationSuccess(result);
            paymentOptionsList.add(SdkUIConstants.PHONEPE);
            setupViewPagerAdapter();

        }

        @Override
        public void onPaymentOptionInitialisationFailure(int errorCode, String description) {
            super.onPaymentOptionInitialisationFailure(errorCode, description);
            Toast.makeText(PayUBaseActivity.this, description, Toast.LENGTH_SHORT).show();
            finish();
        }
    };


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

    private void setupViewPagerAdapter() {

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
        hide_keyboard();
        findViewById(R.id.progress_bar).setVisibility(View.GONE);

    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_pay_now) {
            Log.v("PayU", "Payu btn ");
            mPostData = null;
            mPaymentParams.setHash(mPayUHashes.getPaymentHash());
            switch (paymentOptionsList.get(viewPager.getCurrentItem())) {
                case SdkUIConstants.PHONEPE:
                    makePaymentByPhonePe();
                    break;
            }
            if(mPostData!=null && mPostData.getCode()==PayuErrors.NO_ERROR) {
                payuConfig.setData(mPostData.getResult());
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
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
        (amountTextView = (TextView) findViewById(R.id.textview_amount)).setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        (transactionIdTextView = (TextView) findViewById(R.id.textview_txnid)).setText(SdkUIConstants.TXN_ID + ": " + mPaymentParams.getTxnId());
        initUsingPhonePeSDK();

    }


    /**
     * Payment by PhonePe
     */
    private void makePaymentByPhonePe() {
        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.PHONEPE_INTENT).getPaymentPostParams();
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

    public void hide_keyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    // Check the availability of PhonePe
    private void initUsingPhonePeSDK() {

        PhonePe.getInstance().checkForPaymentAvailability(this,callback,mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(),mPaymentParams.getKey(),mPaymentParams.getUserCredentials());


    }

}
