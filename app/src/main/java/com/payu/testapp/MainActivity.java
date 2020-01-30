package com.payu.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.payu.payuui.Activity.PayUBaseActivity;
import com.payu.upisdk.generatepostdata.PaymentParamsUpiSdk;
import com.payu.upisdk.generatepostdata.PostDataGenerate;
import com.payu.upisdk.util.UpiConstant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This activity prepares PaymentParams, fetches hashes from server and send it to PayuBaseActivity.java.
 * <p>
 * Implement this activity with OneClickPaymentListener only if you are integrating One Tap payments.
 */
public class MainActivity extends AppCompatActivity {

    private String merchantKey, userCredentials;

    // These will hold all the payment parameters

  //  private PaymentParams mPaymentParams;


    private PaymentParamsUpiSdk mPaymentParamsUpiSdk;

    // This sets the configuration
  //  private PayuConfig payuConfig;

    private Spinner environmentSpinner;

    private int Staging_Env = 2;
    private int Production_Env = 0;
    private int environment;
    private String paymentHash;
    private String paymentRelatedHash;
    String postDataFromUpiSdk;
    int requestCode = 123;
    String salt = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // lets set up the tool bar;
        Toolbar toolBar = (Toolbar) findViewById(R.id.app_bar);
        toolBar.setTitle("PayU Demo App");
        toolBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolBar);





        //Lets setup the environment spinner
        environmentSpinner = (Spinner) findViewById(R.id.spinner_environment);
        //  List<String> list = new ArrayList<String>();
        String[] environmentArray = getResources().getStringArray(R.array.environment_array);
/*        list.add("Test");
        list.add("Production");*/
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, environmentArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        environmentSpinner.setAdapter(dataAdapter);
        environmentSpinner.setSelection(0);

        environmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (environmentSpinner.getSelectedItem().equals("Production")) {
                    Toast.makeText(MainActivity.this, getString(R.string.use_live_key_in_production_environment), Toast.LENGTH_SHORT).show();

                    /* For test keys, please contact mobile.integration@payu.in with your app name and registered email id
                     */
                    // ((EditText) findViewById(R.id.editTextMerchantKey)).setText("0MQaQP");
                    ((EditText) findViewById(R.id.editTextMerchantKey)).setText("0MQaQP");
                }
                else{
                    //set the test key in test environment
                    ((EditText) findViewById(R.id.editTextMerchantKey)).setText("gtKFFX");
                    // ((EditText) findViewById(R.id.editTextMerchantKey)).setText("VgZldf");

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == requestCode) {
            if (data != null) {

                /**
                 * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                 * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                 *
                 * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                 * for identifying status of transaction. There are two possible status like, success or failure
                 * */
                new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();

            } else {
                Toast.makeText(this, getString(R.string.could_not_receive_data), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This method prepares all the payments params to be sent to PayuBaseActivity.java
     */
    public void navigateToBaseActivity(View view) {


        // merchantKey="";
        merchantKey = ((EditText) findViewById(R.id.editTextMerchantKey)).getText().toString();
        String amount = ((EditText) findViewById(R.id.editTextAmount)).getText().toString();
        String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();

        String value = environmentSpinner.getSelectedItem().toString();

        String TEST_ENVIRONMENT = getResources().getString(R.string.test);
        if (value.equals(TEST_ENVIRONMENT))
          environment = Staging_Env;
        else
            environment = Production_Env;

        userCredentials = merchantKey + ":" + email;

        //TODO Below are mandatory params for hash genetation
       mPaymentParamsUpiSdk = new PaymentParamsUpiSdk();
        mPaymentParamsUpiSdk.setKey(merchantKey);
        mPaymentParamsUpiSdk.setProductInfo("product_info");
        mPaymentParamsUpiSdk.setFirstName("firstname"); //Customer First name
        mPaymentParamsUpiSdk.setEmail("xyz@gmail.com"); //Customer Email
        mPaymentParamsUpiSdk.setTxnId("" + System.currentTimeMillis()); //Your transaction id
        mPaymentParamsUpiSdk.setAmount(amount); //Your transaction Amount(In Double as String)
        mPaymentParamsUpiSdk.setSurl("https://payuresponse.firebaseapp.com/success");
        mPaymentParamsUpiSdk.setFurl("https://payuresponse.firebaseapp.com/failure");
        mPaymentParamsUpiSdk.setUdf1("udf1");
        mPaymentParamsUpiSdk.setUdf2("udf2");
        mPaymentParamsUpiSdk.setUdf3("udf3");
        mPaymentParamsUpiSdk.setUdf4("udf4");
        mPaymentParamsUpiSdk.setUdf5("udf5");
        mPaymentParamsUpiSdk.setVpa(""); //In case of UPI Collect set customer vpa here
        mPaymentParamsUpiSdk.setUserCredentials(userCredentials);
      //  mPaymentParamsUpiSdk.setOfferKey("");
        mPaymentParamsUpiSdk.setPhone("");//Customer Phone Number
      //  mPaymentParamsUpiSdk.setHash(paymentHash);//Your Payment Hash

         postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                setPaymentMode(UpiConstant.UPI).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                build().toString();

        //TODO Below are mandatory params for hash genetation

        //TODO It is recommended to generate hash from server only. Keep your key and salt in server side hash generation code.
        // generateHashFromServer(mPaymentParams);

        /**
         * Below approach for generating hash is not recommended. However, this approach can be used to test in PRODUCTION_ENV
         * if your server side hash generation code is not completely setup. While going live this approach for hash generation
         * should not be used.
         * */
        if(environment== Staging_Env){
            salt = "eCwWELxi";
            //salt = "wpAo1AgO";
        }else {
            //Production Env
            salt = "13p0PXZk";
        }
//        String salt = "eCwWELxi";
        // String salt = "13p0PXZk";
        // String salt = "1b1b0";
        //
        generateHashFromSDK(mPaymentParamsUpiSdk, salt);

    }

    /******************************
     * Client hash generation
     ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(PaymentParamsUpiSdk mPaymentParamsUpiSdk, String salt) {


        String paymentHashString = mPaymentParamsUpiSdk.getKey() + "|" + mPaymentParamsUpiSdk.getTxnId() + "|" + mPaymentParamsUpiSdk.getAmount() + "|" + mPaymentParamsUpiSdk.getProductInfo() + "|" + mPaymentParamsUpiSdk.getFirstName() + "|" + mPaymentParamsUpiSdk.getEmail() + "|" + mPaymentParamsUpiSdk.getUdf1() + "|" + mPaymentParamsUpiSdk.getUdf2() + "|" + mPaymentParamsUpiSdk.getUdf3() + "|" + mPaymentParamsUpiSdk.getUdf4() + "|" + mPaymentParamsUpiSdk.getUdf5() + "||||||" + salt  ;

      paymentHash =  calculateHash(paymentHashString);

        String paymentRelatedHashString = mPaymentParamsUpiSdk.getKey() + "|" + "payment_related_details_for_mobile_sdk" + "|" + mPaymentParamsUpiSdk.getUserCredentials() + "|" + salt;

        paymentRelatedHash = calculateHash(paymentRelatedHashString);

        launchSdkUI();
    }

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



    /**
     * This method adds the Payuhashes and other required params to intent and launches the PayuBaseActivity.java
     *
    // * @param it contains all the hashes generated from merchant server
     */
    public void launchSdkUI() {

        Intent intent = new Intent(this, PayUBaseActivity.class);
       // intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra("postData",postDataFromUpiSdk);
       // intent.putExtra(PayuConstants.PAYMENT_PARAMS, mPaymentParams);
        intent.putExtra(UpiConstant.PAYMENT_PARAMS_UPI_SDK,mPaymentParamsUpiSdk);
        intent.putExtra("salt",salt);
        intent.putExtra("paymentHash", paymentHash);
        intent.putExtra("paymentRelatedHash",paymentRelatedHash);
        intent.putExtra("environment",environment);
        startActivityForResult(intent, requestCode);
    }


}
