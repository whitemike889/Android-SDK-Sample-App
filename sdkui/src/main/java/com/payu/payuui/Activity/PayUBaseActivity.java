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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.R;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.SwipeTab.SlidingTabLayout;
import com.payu.samsungpay.PayUSUPI;
import com.payu.upisdk.PaymentOption;
import com.payu.upisdk.Upi;
import com.payu.upisdk.bean.UpiConfig;
import com.payu.upisdk.callbacks.PayUUPICallback;
import com.payu.upisdk.generatepostdata.PaymentParamsUpiSdk;
import com.payu.upisdk.generatepostdata.PostDataGenerate;
import com.payu.upisdk.generatepostdata.PostDataUpiSdk;
import com.payu.upisdk.util.UpiConstant;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PayUBaseActivity extends FragmentActivity implements View.OnClickListener {

    public Bundle bundle;
    ArrayList<String> paymentOptionsList = new ArrayList<String>();
    ArrayList<String> paymentOptionsSet = new ArrayList<String>();
    PaymentParamsUpiSdk mPaymentParamsUpiSdk;
    private String paymentHash;
    private String paymentRelatedDetailsHash;
    private int MAX_VPA_SIZE=50;
    private int environment;



    UpiConfig upiConfig;
    private int mTabCount;
    public PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SlidingTabLayout slidingTabLayout;
    private TextView amountTextView;
    private TextView transactionIdTextView;
    private Button payNowButton;
    private Spinner spinnerNetbanking;
    private String bankCode;
    private PostDataUpiSdk postDataUpiSdk;
    private Boolean smsPermission;
    private String TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    private String PAYU_VPA = "samsungtest.payu@axisbank";
    private String PAYU_MERCHANT_NAME = "PayU";
    private final String TAG = "PayuBaseActivity";
    private PayUSUPI payUSUPI;
   /* private boolean isSamsungPaySupported = false;
    private boolean isPhonePeSupported = false;
    private boolean isUPISupported = false;
    private boolean isTezSupported = false;
    private boolean isUPIIntentSupported = false;*/
    int requestCode = 123;
    private String smsPermission1= "false";
    /**

    /**
     * Callback of payment availability while doing through UPISDK.
     */
    PayUUPICallback payUUpiSdkCallback = new PayUUPICallback() {



        @Override
        public void isPaymentOptionAvailable(boolean isAvailable, PaymentOption paymentOption) {
            super.isPaymentOptionAvailable(isAvailable, paymentOption);
            if(isAvailable) {
                switch (paymentOption) {
                    
                    case PHONEPE:
                        paymentOptionsList.add(SdkUIConstants.PHONEPE);
                        break;
                    case TEZ:
                        paymentOptionsList.add(SdkUIConstants.TEZ);
                        break;

                }
                setupViewPagerAdapter();
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





    private void setupViewPagerAdapter() {

        if(null == pagerAdapter) {
            pagerAdapter = new PagerAdapter(getSupportFragmentManager(), paymentOptionsList);
        }else {
            pagerAdapter.setmTitles(paymentOptionsList);
        }

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
            postDataFromUpiSdk = null;
          //  mPaymentParams.setHash(mPayUHashes.getPaymentHash());
              mPaymentParamsUpiSdk.setHash(paymentHash);

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
                upiConfig.setPayuPostData(postDataFromUpiSdk);}
                //   Log.v("PayU","Post Data through upi sdk "+mPostDataUpiSdk.getResult());
                //Log.v("PayU", "Post Data through upisdk " + postDataFromUpiSdk);


                Intent intent = new Intent(this, PaymentsActivity.class);
                intent.putExtra(UpiConstant.UPI_CONFIG, upiConfig);
                intent.putExtra("environment",environment);

                intent.putExtra(smsPermission1, smsPermission);
               // intent.putExtra(PayuConstants.SALT, bundle.getString(PayuConstants.SALT)); // Recommended to store on your server
                //Log.v("PayU", "Salt:: " + bundle.getString(PayuConstants.SALT));

                startActivityForResult(intent, requestCode);

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


        upiConfig = bundle.getParcelable(UpiConstant.UPI_CONFIG);
        upiConfig = null != upiConfig ? upiConfig : new UpiConfig();
        paymentHash = bundle.getString("paymentHash");
        paymentRelatedDetailsHash = bundle.getString("paymentRelatedHash");
        environment = bundle.getInt("environment");





        // TODO add null pointer check here

        mPaymentParamsUpiSdk = bundle.getParcelable(UpiConstant.PAYMENT_PARAMS_UPI_SDK); // Todo change the name to PAYMENT_PARAMS
        paymentHash = bundle.getString("paymentHash");
        mPaymentParamsUpiSdk = bundle.getParcelable(UpiConstant.PAYMENT_PARAMS_UPI_SDK); // Todo change the name to PAYMENT_PARAMS
          // mPayUHashUpiSdk = bundle.getParcelable("payuhashsdk");

        smsPermission = bundle.getBoolean(smsPermission1);



        (amountTextView = (TextView) findViewById(R.id.textview_amount)).setText(SdkUIConstants.AMOUNT + ": " + mPaymentParamsUpiSdk.getAmount());
        (transactionIdTextView = (TextView) findViewById(R.id.textview_txnid)).setText(SdkUIConstants.TXN_ID + ": " + mPaymentParamsUpiSdk.getTxnId());

        paymentOptionsList.add(SdkUIConstants.UPI);
        paymentOptionsList.add(SdkUIConstants.GENERICINTENT);

        initUsingUpiSDK();


    }

    /**
     * Payment by PhonePe
     */
    private void makePaymentByPhonePe() {
        try {
            postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.PHONEPE_INTENT).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();
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
            if (etVirtualAddress.getText().toString().trim().length() > MAX_VPA_SIZE) {
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            } else if (!etVirtualAddress.getText().toString().trim().contains("@")) {
                etVirtualAddress.setError(getBaseContext().getText(R.string.error_invalid_vpa));
            } else {
                String userVirtualAddress = etVirtualAddress.getText().toString().trim();
                //Pattern pattern = Pattern.compile("^([A-Za-z0-9\\.])+\\@[A-Za-z0-9]+$");
                Pattern pattern = Pattern.compile(".+@.+");
                Matcher matcher = pattern.matcher(userVirtualAddress);
                if (matcher.matches()) {
                    mPaymentParamsUpiSdk.setVpa(userVirtualAddress);
                   // mPaymentParamsUpiSdk.setVpa(userVirtualAddress);
                    try {
                        postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                                setPaymentMode(UpiConstant.UPI).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                                build().toString();
                     //   mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI).getPaymentPostParams();
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
            postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.TEZ).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();



          //  mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.TEZ).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

   // private String postDataFromUpiSdk;

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
            postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.INTENT).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();

             // mPostDataUpiSdk= new PaymentPostParamsUpiSdk(mPaymentParamsUpiSdk, "INTENT", this).getPaymentPostParams();
            // L.v("PayU","Generate postDataFromUpiSdk  "+mPostDataUpiSdk.getStatus());

              // mPostDataUpiSdk.getResult()
          //  mPostData = new PaymentPostParams(mPaymentParams, PayuConstants.UPI_INTENT).getPaymentPostParams();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String postDataFromUpiSdk;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCode) {
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
        upi.checkForPaymentAvailability(this, PaymentOption.PHONEPE, payUUpiSdkCallback, paymentRelatedDetailsHash, mPaymentParamsUpiSdk.getKey(), mPaymentParamsUpiSdk.getUserCredentials());
        upi.checkForPaymentAvailability(this, PaymentOption.TEZ, payUUpiSdkCallback, paymentRelatedDetailsHash, mPaymentParamsUpiSdk.getKey(), mPaymentParamsUpiSdk.getUserCredentials());
       // upi.checkForPaymentAvailability(this, PaymentOption.UPI_INTENT, payUUpiSdkCallback, paymentRelatedDetailsHash, mPaymentParamsUpiSdk.getKey(), mPaymentParamsUpiSdk.getUserCredentials());

    }

}
