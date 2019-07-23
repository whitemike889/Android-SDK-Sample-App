package com.payu.payuui.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.payu.india.Interfaces.PaymentRelatedDetailsListener;
import com.payu.india.Interfaces.ValueAddedServiceApiListener;
import com.payu.india.Model.Emi;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentDetails;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.PostParams.PaymentPostParams;
import com.payu.india.Tasks.GetPaymentRelatedDetailsTask;
import com.payu.india.Tasks.ValueAddedServiceTask;
import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.SwipeTab.SlidingTabLayout;
import com.payu.samsungpay.PayUSUPI;
import com.payu.samsungpay.PayUSUPIPostData;
import com.payu.samsungpay.PayUSamsungPay;
import com.payu.samsungpay.PayUSamsungPayCallback;
import com.payu.upisdk.PaymentOption;
import com.payu.upisdk.Upi;
import com.payu.upisdk.callbacks.PayUUPICallback;
import com.payu.upisdk.generatepostdata.PaymentParamsUpiSdk;
import com.payu.upisdk.generatepostdata.PostDataGenerate;
import com.payu.upisdk.util.UpiConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PayUBaseActivity extends FragmentActivity implements PaymentRelatedDetailsListener, ValueAddedServiceApiListener, View.OnClickListener {

    public Bundle bundle;
    ArrayList<String> paymentOptionsList = new ArrayList<String>();
    ArrayList<String> paymentOptionsSet = new ArrayList<String>();
    PayuConfig payuConfig;
    PaymentParams mPaymentParams;
   // PaymentParamsUpiSdk mPaymentParamsUpiSdk;
    PayuHashes mPayUHashes;
    //PayuHashUpiSdk mPayUHashUpiSdk;
    PayuResponse mPayuResponse;
    PayuUtils mPayuUtils;
    PayuResponse valueAddedResponse;
    private int mTabCount;
    public PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;
    private TextView amountTextView;
    private TextView transactionIdTextView;
    private Button payNowButton;
    private Spinner spinnerNetbanking;
    private String bankCode;
    private ArrayList<PaymentDetails> netBankingList;
    private PostData postData;
    private ValueAddedServiceTask valueAddedServiceTask;
    private ArrayList<StoredCard> savedCards;
    private Boolean smsPermission;
    private PostData mPostData;
    HashMap<String, String> oneClickCardTokens;
    private int storeOneClickHash;
    private String TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    private String PAYU_VPA = "samsungtest.payu@axisbank";
    private String PAYU_MERCHANT_NAME = "PayU";
    private final String TAG = "PayuBaseActivity";
    private PayUSUPI payUSUPI;
    private boolean isSamsungPaySupported = false;
    private boolean isPhonePeSupported = false;
    /**
     * Callback of payment availability while doing through CB.
     */
   /* PayUCustomBrowserCallback callback = new PayUCustomBrowserCallback() {
        @Override
        public void isPaymentOptionAvailable(CustomBrowserResultData resultData) {
            Log.d(TAG, "isPaymentOptionAvailable: " + resultData.isPaymentOptionAvailable() + " " + resultData.getPaymentOption().getPaymentName());
            Log.v("PayU", "IsPayment Available " + resultData.isPaymentOptionAvailable() + "   " + resultData.getPaymentOption().getPaymentName());
            switch (resultData.getPaymentOption()) {
                case SAMSUNGPAY:
                    isSamsungPaySupported = resultData.isPaymentOptionAvailable();
                    break;
                case PHONEPE:
                    isPhonePeSupported = resultData.isPaymentOptionAvailable();
                    break;

            }
        }

        @Override
        public void onPaymentFailure(String payuResult, String merchantResponse) {
            super.onPaymentFailure(payuResult, merchantResponse);
           // L.v("PayU"," onpayment failure callback paybase");
        }
    };*/
    /**
     * Callback of payment availability while doing through UPISDK.
     */
    PayUUPICallback payUUpiSdkCallback = new PayUUPICallback() {



        @Override
        public void isPaymentOptionAvailable(boolean isAvailable, PaymentOption paymentOption) {
            super.isPaymentOptionAvailable(isAvailable, paymentOption);
            switch (paymentOption){
                case PHONEPE:
                    isPhonePeSupported = isAvailable;
                    break;
            }

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

    @Override
    public void onValueAddedServiceApiResponse(PayuResponse payuResponse) {
        valueAddedResponse = payuResponse;
        if (mPayuResponse != null)
            setupViewPagerAdapter(mPayuResponse, valueAddedResponse);
            payNowButton.setEnabled(true);

    }

    @Override
    public void onPaymentRelatedDetailsResponse(PayuResponse payuResponse) {
        mPayuResponse = payuResponse;

        if (valueAddedResponse != null)
            setupViewPagerAdapter(mPayuResponse, valueAddedResponse);

        MerchantWebService valueAddedWebService = new MerchantWebService();
        valueAddedWebService.setKey(mPaymentParams.getKey());
        valueAddedWebService.setCommand(PayuConstants.VAS_FOR_MOBILE_SDK);
        valueAddedWebService.setHash(mPayUHashes.getVasForMobileSdkHash());
        valueAddedWebService.setVar1(PayuConstants.DEFAULT);
        valueAddedWebService.setVar2(PayuConstants.DEFAULT);
        valueAddedWebService.setVar3(PayuConstants.DEFAULT);

        if ((postData = new MerchantWebServicePostParams(valueAddedWebService).getMerchantWebServicePostParams()) != null && postData.getCode() == PayuErrors.NO_ERROR) {

            payuConfig.setData(postData.getResult());
          //  Log.v("PayU", "Post Data Line no 182 " + postData.getResult());
            valueAddedServiceTask = new ValueAddedServiceTask(this);
            valueAddedServiceTask.execute(payuConfig);
        } else {
            Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
        }


    }

    private void setupViewPagerAdapter(final PayuResponse payuResponse, PayuResponse valueAddedResponse) {

        if (payuResponse.isResponseAvailable() && payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) { // ok we are good to go
            Toast.makeText(this, payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();


            if (payuResponse.isUpiAvailable()) { // adding UPI
                paymentOptionsList.add(SdkUIConstants.UPI);
            }
            if (payuResponse.isGoogleTezAvailable()) { // adding UPI
                paymentOptionsList.add(SdkUIConstants.TEZ);
            }


            if (isPhonePeSupported) {
                paymentOptionsList.add(SdkUIConstants.PHONEPE);
            }
            if (payuResponse.isGenericIntentAvailable()) {
                paymentOptionsList.add(SdkUIConstants.GENERICINTENT);
            }
//            if (payuResponse.isSamsungPayAvailable()) {
//                paymentOptionsList.add(SdkUIConstants.SAMSUNG_PAY);
//            }
//            if (isSamsungPaySupported) {
//                paymentOptionsList.add(SdkUIConstants.SAMSUNG_PAY);
//            }
            // Emi UI is will go with subvention EMI
            /*if(payuResponse.isEmiAvailable()){
                paymentOptionsList.add(SdkUIConstants.EMI);
            }*/


        } else {
            Toast.makeText(this, "Something went wrong : " + payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), paymentOptionsList, payuResponse, valueAddedResponse, oneClickCardTokens);

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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                removeEditTextError(R.id.et_virtual_address);
                switch (paymentOptionsList.get(position)) {

                    case SdkUIConstants.UPI:
                        payNowButton.setEnabled(true);
                        hide_keyboard();
                        break;
                    case SdkUIConstants.TEZ:
                        payNowButton.setEnabled(true);
                        hide_keyboard();
                        break;

                    case SdkUIConstants.PHONEPE:
                        payNowButton.setEnabled(true);
                        hide_keyboard();
                        break;
                    case SdkUIConstants.GENERICINTENT:
                        payNowButton.setEnabled(true);
                        hide_keyboard();
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // removeEditTextError(R.id.et_virtual_address);
            }


        });

        findViewById(R.id.progress_bar).setVisibility(View.GONE);

    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_pay_now) {
            Log.v("PayU", "Payu btn ");
            mPostData = null;
            mPaymentParams.setHash(mPayUHashes.getPaymentHash());
            //  mPaymentParamsUpiSdk.setHash(mPayUHashUpiSdk.getPaymentHash());

            switch (paymentOptionsList.get(viewPager.getCurrentItem())) {

                case SdkUIConstants.UPI:
                    makePaymentByUPI();
                    break;
                case SdkUIConstants.TEZ:
                    makePaymentByTez();
                    break;
                case SdkUIConstants.PHONEPE:
                    makePaymentByPhonePe();
                    break;

                case SdkUIConstants.GENERICINTENT:
                    makePaymentByGenericIntent();
                    break;
            }
            //L.v("PayU", "PostData " + postDataFromUpiSdk);
            if (postDataFromUpiSdk != null) {
                // if (mPostData.getCode() == PayuErrors.NO_ERROR) {
                //  payuConfig.setData(mPostData.getResult());
                payuConfig.setData(postDataFromUpiSdk);
                //   Log.v("PayU","Post Data through upi sdk "+mPostDataUpiSdk.getResult());
                //Log.v("PayU", "Post Data through upisdk " + postDataFromUpiSdk);
                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
                intent.putExtra(PayuConstants.SMS_PERMISSION, smsPermission);
               // intent.putExtra(PayuConstants.SALT, bundle.getString(PayuConstants.SALT)); // Recommended to store on your server
                //Log.v("PayU", "Salt:: " + bundle.getString(PayuConstants.SALT));
                intent.putExtra("magic_retry", bundle.getString("magic_retry"));
                startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
            } else {
                if(postData!=null && postData.getCode()==PayuErrors.NO_ERROR) {
                    payuConfig.setData(mPostData.getResult());
                    //   Log.v("PayU","Post Data through upi sdk "+mPostDataUpiSdk.getResult());
                  //  Log.v("PayU", "Post Data through upisdk " + postDataFromUpiSdk);
                    Intent intent = new Intent(this, PaymentsActivity.class);
                   // Log.v("PayU","Payu config "+payuConfig.getData());
                    intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
                    intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, storeOneClickHash);
                    intent.putExtra(PayuConstants.SMS_PERMISSION, smsPermission);
                    intent.putExtra(PayuConstants.SALT, bundle.getString(PayuConstants.SALT));
                    Log.v("PayU", "Salt:: " + bundle.getString(PayuConstants.SALT));
                    intent.putExtra("magic_retry", bundle.getString("magic_retry"));
                    startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
                    //  Toast.makeText(this, mPostData.getResult(), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // Log.v("PayU", "class name:" + getClass().getCanonicalName());
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


        // TODO add null pointer check here
//        mPaymentDefaultParams = bundle.getParcelable(PayuConstants.PAYMENT_DEFAULT_PARAMS);
        mPaymentParams = bundle.getParcelable(PayuConstants.PAYMENT_PARAMS); // Todo change the name to PAYMENT_PARAMS
        mPayUHashes = bundle.getParcelable(PayuConstants.PAYU_HASHES);
      //  mPaymentParamsUpiSdk = bundle.getParcelable(UpiConstant.PAYMENT_PARAMS_UPI_SDK); // Todo change the name to PAYMENT_PARAMS
        //   mPayUHashUpiSdk = bundle.getParcelable("payuhashsdk");

        storeOneClickHash = bundle.getInt(PayuConstants.STORE_ONE_CLICK_HASH);
        smsPermission = bundle.getBoolean(PayuConstants.SMS_PERMISSION);

        oneClickCardTokens = (HashMap<String, String>) bundle.getSerializable(PayuConstants.ONE_CLICK_CARD_TOKENS);


        (amountTextView = (TextView) findViewById(R.id.textview_amount)).setText(SdkUIConstants.AMOUNT + ": " + mPaymentParams.getAmount());
        (transactionIdTextView = (TextView) findViewById(R.id.textview_txnid)).setText(SdkUIConstants.TXN_ID + ": " + mPaymentParams.getTxnId());

        initUsingUpiSDK();
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials() == null ? "default" : mPaymentParams.getUserCredentials());


        merchantWebService.setHash(mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash());

        // Dont fetch the data if calling activity is PaymentActivity

        // fetching for the first time.// dont fetch the data if its been called from payment activity.
        if (null == savedInstanceState) {
            PostData postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();
            if (postData.getCode() == PayuErrors.NO_ERROR) {
                // ok we got the post params, let make an api call to payu to fetch the payment related details
                payuConfig.setData(postData.getResult());

//                 lets set the visibility of progress bar
                findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                GetPaymentRelatedDetailsTask paymentRelatedDetailsForMobileSdkTask = new GetPaymentRelatedDetailsTask(this);
                paymentRelatedDetailsForMobileSdkTask.execute(payuConfig);
            } else {
                Toast.makeText(this, postData.getResult(), Toast.LENGTH_LONG).show();
//                 close the progress bar
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }
        }

    }


    //This method will initialize Samsung Pay
  /*  private void initSamsungPay() {

        String userCredentials = mPaymentParams.getUserCredentials() == null ? "default" : mPaymentParams.getUserCredentials();

        String postData =
//                "device_type=1&"+
                "key=" + mPaymentParams.getKey() + "&txnid=" + mPaymentParams.getTxnId() + "&amount=" + mPaymentParams.getAmount() + "&" +
                        "productinfo=" + mPaymentParams.getProductInfo() + "&firstname=" + mPaymentParams.getFirstName() + "&email=" + mPaymentParams.getEmail() + "&" +
                        "user_credentials=" + userCredentials + "&surl=" + mPaymentParams.getSurl() + "&furl=" + mPaymentParams.getFurl() +
                        "&" + "hash=" + mPayUHashes.getPaymentHash() +
                        "&udf1=" + mPaymentParams.getUdf1() + "&udf2=" + mPaymentParams.getUdf2() + "&" +
                        "udf3=" + mPaymentParams.getUdf3() + "&udf4=" + mPaymentParams.getUdf4() + "&udf5=" + mPaymentParams.getUdf5();
//                + "&pg=SAMPAY&" + "bankcode=SAMPAY&txn_s2s_flow=1&burl=123";

//        try {
//            mPaymentParams.setHash(mPayUHashes.getPaymentHash());
//            mPaymentParams.setUserCredentials(userCredentials);
//            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.SAMSUNG_PAY).getPaymentPostParams();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        PayUSamsungPayCallback listener = new PayUSamsungPayCallback() {

            @Override
            public void onSamsungPaySuccess(String payuResponse) {
                Log.d(TAG, "onSamsungPaySuccess: payuResponse:" + payuResponse);
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.cb_result), "");
                intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }


            @Override
            public void onSamsungPayFailure(String payuResponse) {
                Log.d(TAG, "onSamsungPayFailure: payuResponse:" + payuResponse);
                Intent intent = new Intent();
                intent.putExtra(getString(R.string.cb_result), "");
                intent.putExtra(getString(R.string.cb_payu_response), payuResponse);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();

            }

            @Override
            public void onSamsungPayInitialisationSuccess(String vpa) {
                isSamsungPaySupported = true;
                PayUBaseActivity.this.payUSUPI = payUSUPI;
            }

            @Override
            public void onSamsungPayInitialisationFailure(int errorCode, String description) {
                isSamsungPaySupported = false;
                Toast.makeText(PayUBaseActivity.this, errorCode + "-" + description, Toast.LENGTH_SHORT).show();
            }

        };*/

//        PayUSUPIPostData.PostDataBuilder builder = new PayUSUPIPostData.PostDataBuilder();
////        builder.setMerchantKey(mPaymentParams.getKey());
//        builder.setPostData(postData);
////        builder.setPostData(mPostData.getResult());
////        builder.setEnvironment(PayUSUPIConstant.STAGING_ENV);//Optional
////        builder.setMerchantPaymentOptionHash(mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash());  //set value of paymentRelatedDetailsHash
//        PayUSUPIPostData payUSUPIPostData = null;
//        try {
//            payUSUPIPostData = builder.build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        new PayUSamsungPay().init(PayUBaseActivity.this, payUSUPIPostData, listener);
       // PayUSamsungPay.getInstance().checkForSamsungPayAvailiability(listener, this, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), mPaymentParams.getKey(), userCredentials);

  //  }

  /*  private void makePaymentBySamsungPay() {

        String userCredentials = mPaymentParams.getUserCredentials() == null ? "default" : mPaymentParams.getUserCredentials();
        String postData =
//                "device_type=1&"+
                "key=" + mPaymentParams.getKey() + "&txnid=" + mPaymentParams.getTxnId() + "&amount=" + mPaymentParams.getAmount() + "&" +
                        "productinfo=" + mPaymentParams.getProductInfo() + "&firstname=" + mPaymentParams.getFirstName() + "&email=" + mPaymentParams.getEmail() + "&" +
                        "user_credentials=" + userCredentials + "&surl=" + mPaymentParams.getSurl() + "&furl=" + mPaymentParams.getFurl() +
                        "&" + "hash=" + mPayUHashes.getPaymentHash() +
                        "&udf1=" + mPaymentParams.getUdf1() + "&udf2=" + mPaymentParams.getUdf2() + "&" +
                        "udf3=" + mPaymentParams.getUdf3() + "&udf4=" + mPaymentParams.getUdf4() + "&udf5=" + mPaymentParams.getUdf5() + "&bankcode=SAMPAY&pg=SAMPAY";
        postDataFromUpiSdk = postData;
        PayUSUPIPostData.Builder builder = new PayUSUPIPostData.Builder();
//        builder.setMerchantKey(mPaymentParams.getKey());
        builder.setPostData(postData);
        PayUSUPIPostData payUSUPIPostData = null;
        try {
            payUSUPIPostData = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
      /*  ProgressDialog progressDialog = new ProgressDialog(PayUBaseActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        try {
            Log.v("PayU","PayuBaseActivity payUSUPIPostData "+payUSUPIPostData);
          //  PayUSamsungPay.getInstance().init(this, payUSUPIPostData);
//             payUSUPI.startPayment();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

  //  }



    /**
     * Payment by PhonePe
     */
    private void makePaymentByPhonePe() {
        try {
           /* postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.PHONEPE_INTENT).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();*/
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.PHONEPE_INTENT).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate VPA and Calculate post data
     */
    private void makePaymentByUPI() {

        EditText etVirtualAddress = (EditText) findViewById(R.id.et_virtual_address);
        // Virtual address Check (vpa check)
        // 1)Vpa length should be less than or equal to 50
        // 2)It can be alphanumeric and can contain a dot(.).
        // 3)It should contain a @
        if (etVirtualAddress.getText() != null && etVirtualAddress.getText().toString().trim().length() == 0) {
            etVirtualAddress.requestFocus();
            etVirtualAddress.setError(getBaseContext().getText(R.string.error_fill_vpa));

        } else {
            if (etVirtualAddress.getText().toString().trim().length() > PayuConstants.MAX_VPA_SIZE) {
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            } else if (!etVirtualAddress.getText().toString().trim().contains("@")) {
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            } else {
                String userVirtualAddress = etVirtualAddress.getText().toString().trim();
                //Pattern pattern = Pattern.compile("^([A-Za-z0-9\\.])+\\@[A-Za-z0-9]+$");
                Pattern pattern = Pattern.compile(".+@.+");
                Matcher matcher = pattern.matcher(userVirtualAddress);
                if (matcher.matches()) {
                    mPaymentParams.setVpa(userVirtualAddress);
                   // mPaymentParamsUpiSdk.setVpa(userVirtualAddress);
                    try {
                       /* postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                                setPaymentMode(UpiConstant.UPI).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                                build().toString();*/
                        mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI).getPaymentPostParams();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
                }


            }
        }


    }

    private void makePaymentByTez() {
        //TODO Uncomment below when Intent flow is available
//        if (isTezAvailable()) {
//            //TODO Call PayU Api to generate TxnId before calling below
//            Intent intent = new Intent();
//            intent.setPackage(TEZ_PACKAGE_NAME);
//            String uri = "upi://pay?pa=" + PAYU_VPA + "&am=" + mPaymentParams.getAmount() + "&pn=" + PAYU_MERCHANT_NAME;
//            intent.setData(Uri.parse(uri));
//            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
//        } else {
        try {
            /*postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.TEZ).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();*/


            //  mPostDataUpiSdk= new PaymentPostParamsUpiSdk(mPaymentParamsUpiSdk, UpiConstant.UPI_, this).getPaymentPostParams();

            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.TEZ).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String postDataFromUpiSdk;

    private void makePaymentByGenericIntent() {
        //TODO Uncomment below when Intent flow is available
//        if (isTezAvailable()) {
//            //TODO Call PayU Api to generate TxnId before calling below
//            Intent intent = new Intent();
//            intent.setPackage(TEZ_PACKAGE_NAME);
//            String uri = "upi://pay?pa=" + PAYU_VPA + "&am=" + mPaymentParams.getAmount() + "&pn=" + PAYU_MERCHANT_NAME;
//            intent.setData(Uri.parse(uri));
//            startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
//        } else {
        try {
           /* postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.INTENT).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();*/

            //  mPostDataUpiSdk= new PaymentPostParamsUpiSdk(mPaymentParamsUpiSdk, "INTENT", this).getPaymentPostParams();
            // L.v("PayU","Generate postDataFromUpiSdk  "+mPostDataUpiSdk.getStatus());

            //   mPostDataUpiSdk.getResult()
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI_INTENT).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



  /* private void makePaymentByEMI() {

        Spinner emiDurationSpinner = (Spinner) findViewById(R.id.spinner_emi_duration);
        EditText cardNumberEditText = (EditText) findViewById(R.id.edit_text_emi_card_number);
        EditText nameOnCardEditText = (EditText) findViewById(R.id.edit_text_emi_name_on_card);
        EditText cvvEditText = (EditText) findViewById(R.id.edit_text_emi_cvv);
        EditText expiryMonthEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_month);
        EditText expiryYearEditText = (EditText) findViewById(R.id.edit_text_emi_expiry_year);*/


     /*   Emi selectedEmi = (Emi) emiDurationSpinner.getSelectedItem();
        bankCode = selectedEmi.getBankCode();

        mPaymentParams.setCardNumber(cardNumberEditText.getText().toString());
        mPaymentParams.setNameOnCard(nameOnCardEditText.getText().toString());
        mPaymentParams.setExpiryMonth(expiryMonthEditText.getText().toString());
        mPaymentParams.setExpiryYear(expiryYearEditText.getText().toString());
        mPaymentParams.setCvv(cvvEditText.getText().toString());
        mPaymentParams.setBankCode(bankCode);

        try {
            mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.EMI).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();*/
       /* }*/

    /*}*/

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

    /**
     * Remove EditText Error
     *
     * @param resID resource ID of EditText
     */
    public void removeEditTextError(int resID) {
        //R.id.et_virtual_address
        View etVirtualAddress = findViewById(resID);
        if (etVirtualAddress != null) {
            ((EditText) etVirtualAddress).setError(null);
        }
    }

    public boolean isTezAvailable() {
        //Tez Available for Kitkat and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                getPackageManager().getPackageInfo(TEZ_PACKAGE_NAME, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    // Check the availability of PhonePe
    private void initUsingUpiSDK() {
        Upi upi = Upi.getInstance();
        upi.checkForPaymentAvailability(this, PaymentOption.PHONEPE, payUUpiSdkCallback, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), mPaymentParams.getKey(), mPaymentParams.getUserCredentials());
        upi.checkForPaymentAvailability(this, PaymentOption.TEZ, payUUpiSdkCallback, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), mPaymentParams.getKey(), mPaymentParams.getUserCredentials());
        upi.checkForPaymentAvailability(this, PaymentOption.UPI_INTENT, payUUpiSdkCallback, mPayUHashes.getPaymentRelatedDetailsForMobileSdkHash(), mPaymentParams.getKey(), mPaymentParams.getUserCredentials());

    }

}
