package com.payu.testapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.payu.india.CallBackHandler.OnetapCallback;
import com.payu.india.Extras.PayUSdkDetails;
import com.payu.india.Interfaces.OneClickPaymentListener;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Payu.Payu;
import com.payu.india.Payu.PayuConstants;
import com.payu.payuui.Activity.PayUBaseActivity;
import com.payu.testapp.hash.GetHashesFromSDK;
import com.payu.testapp.hash.GetHashesFromServerTask;
import com.payu.testapp.hash.HashGenerationCallBack;
import com.payu.testapp.onetap.DeleteMerchantHashCallBack;
import com.payu.testapp.onetap.FetchMerchantHashes;
import com.payu.testapp.onetap.FetchMerchantHashesCallBack;
import com.payu.testapp.onetap.StoreMerchantHashCallBack;
import com.payu.testapp.onetap.StoreMerchantHashTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        DeleteMerchantHashCallBack, FetchMerchantHashesCallBack, StoreMerchantHashCallBack
        , HashGenerationCallBack, OneClickPaymentListener {

    // STAGING_ENV,PRODUCTION_ENV
    /**
     * This variable is used to switch between production or testing environment.
     */
    private int env = PayuConstants.PRODUCTION_ENV;

    /**
     * gtKFFx key is PayU's test key and salt for this key is : eCwWELxi
     */
    private String testKey = "gtKFFx";


    /**
     * This is test salt for gtKFFx key.<br>
     * <b>NOTE :</b> Never put salt in your app and
     * Hash generation from SDK is not recommended
     */
    private String testSalt = "eCwWELxi";

    /**
     * 0MQaQP key is PayU's production  key and salt for this key is : 13p0PXZk
     */
    private String productionKey = "0MQaQP";


    /**
     * This is production salt for 0MQaQP key.<br>
     * <b>NOTE :</b> Never put salt in your app and
     * Hash generation from SDK is not recommended
     */
    private String productionSalt = "13p0PXZk";


    // TODO: 12/30/15 add server side hash generation link
    /**
     * This variable is used to enable Hash generation from SDK,
     * hash should always be generated from merchant server<br>
     * <b>NOTE :</b> Hash generation from SDK is not recommended, you should
     * always set this variable false
     */
    private boolean flagGenerateHashFromSDK = false;

    private String merchantKey =
            env == PayuConstants.PRODUCTION_ENV ? productionKey : testKey;

    private String merchantSalt;
    private String var1;
    private Intent intent;

    private String paramsKeys[] = {PayuConstants.KEY,
            PayuConstants.AMOUNT,
            PayuConstants.PRODUCT_INFO,
            PayuConstants.FIRST_NAME,
            PayuConstants.EMAIL,
            PayuConstants.PHONE,
            PayuConstants.TXNID,
            PayuConstants.SURL,
            PayuConstants.FURL,
            PayuConstants.USER_CREDENTIALS,
            PayuConstants.UDF1,
            PayuConstants.UDF2,
            PayuConstants.UDF3,
            PayuConstants.UDF4,
            PayuConstants.UDF5,
            PayuConstants.ENV,
            PayuConstants.STORE_ONE_CLICK_HASH,
            "sdk_hash_generation"
    };

    private String paramsValues[] = {merchantKey,
            "10.0",//AMOUNT
            "myproduct",//PRODUCT_INFO
            "rahul",//FIRST_NAME
            "rahul@rahul.com",//EMAIL
            "1234567890",//PHONE
            "" + System.currentTimeMillis(),//TXNID
            "https://payu.herokuapp.com/success",//SURL
            "https://payu.herokuapp.com/failure",//FURL
            merchantKey + ":payutest@payu.in",//USER_CREDENTIALS
            "udf1",//UDF1
            "udf2",//UDF2
            "udf3",//UDF3
            "udf4",//UDF4
            "udf5",//UDF5
            "" + env,//ENV
            String.valueOf(PayuConstants.STORE_ONE_CLICK_HASH_SERVER),//STORE_ONE_CLICK_HASH
            String.valueOf(flagGenerateHashFromSDK)//it should always be false
    };

    /**
     * This url should be merchant's server url, all hashes will be get from merchant's server using this url.
     */
    private String urlForHash = "https://payu.herokuapp.com/get_hash";//change with your server url


    /**
     * This url should be merchant's server url.
     * For one tap payment, merchant hash and card token will be stored at merchant's server using this url.
     */
    private String urlForStoreMerchantHash = "https://payu.herokuapp.com/store_merchant_hash";

    /**
     * This url should be merchant's server url.
     * For one tap payment, all stored merchant hashes and card tokens
     * will be fetched from merchant's server using this url.
     */
    private String urlForFetchingMerchantHash = "https://payu.herokuapp.com/get_merchant_hashes";

    /**
     * This url should be merchant's server url.
     * For one tap payment, deleting merchant hashes and card tokens
     */
    private String urlForDeleteMerchantHash = "https://payu.herokuapp.com/delete_merchant_hash";

    private Toolbar toolBar;
    private ScrollView mainScrollView;
    private LinearLayout rowContainerLinearLayout;
    private Button addButton;
    private Button nextButton;
    private EditText leftChild;
    private EditText rightChild;


    //TODO add samll discription about these variables
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private PayuHashes mPayuHashes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        OnetapCallback.setOneTapCallback(this);
        
        //Must set this to initialize
        Payu.setInstance(this);

        // lets set up the tool bar
       // toolBar = (Toolbar) findViewById(R.id.app_bar);
       // setSupportActionBar(toolBar);

        // lets initialize the views
        addButton = (Button) findViewById(R.id.button_add);
        nextButton = (Button) findViewById(R.id.button_next);
        rowContainerLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);
        mainScrollView = (ScrollView) findViewById(R.id.scroll_view_main);

        // lets set the on click listener to the buttons
        addButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);


        //initializing variables for payu
        payuInit();

        // filling up the ui with the values.
        //just for sample app
        fillUI();

        // lets tell the people what version of sdk we are using
        PayUSdkDetails payUSdkDetails = new PayUSdkDetails();

        Toast.makeText(this, "Build No: " + payUSdkDetails.getSdkBuildNumber() + "\n Build Type: " + payUSdkDetails.getSdkBuildType() + " \n Build Flavor: " + payUSdkDetails.getSdkFlavor() + "\n Application Id: " + payUSdkDetails.getSdkApplicationId() + "\n Version Code: " + payUSdkDetails.getSdkVersionCode() + "\n Version Name: " + payUSdkDetails.getSdkVersionName(), Toast.LENGTH_SHORT).show();

    }


    // filling up the ui with the values.
    //just for sample app
    private void fillUI() {
        for (int i = 0; i < paramsKeys.length; i++) {
            addView();
            LinearLayout currentLayout = (LinearLayout) rowContainerLinearLayout.getChildAt(i);
            leftChild = ((EditText) currentLayout.getChildAt(0));
            rightChild = ((EditText) currentLayout.getChildAt(1));
            leftChild.setText(paramsKeys[i]);
            if (null != paramsValues[i])
                rightChild.setText(paramsValues[i]);

            //for testing only,when env change from prod to test or vice-versa
            if (paramsKeys[i].equals(PayuConstants.ENV)) {
                rightChild.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //changes key value and user credentials value in sample app UI
                        if ((s.toString().equals(String.valueOf(PayuConstants.PRODUCTION_ENV)))
                                || (s.toString().equals(String.valueOf(PayuConstants.STAGING_ENV)))
                                || (s.toString().equals(String.valueOf(PayuConstants.MOBILE_STAGING_ENV)))) {
                            updateUIOnChangeEnv(Integer.valueOf(s.toString()));
                        }
                    }
                });
            }


            //for testing only,when key change from production key to test key or vice-versa
            if (paramsKeys[i].equals(PayuConstants.KEY)) {
                rightChild.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //changes key value and user credentials value in sample app UI
                        if ((s.toString().equals(testKey))
                                || (s.toString().equals(productionKey)))
                        {
                            updateUIOnChangeUserCredentials(s.toString());
                        }
                    }
                });
            }

        }
        
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

        switch (id) {
            case R.id.action_exit:
                break;
            case R.id.action_next:
                nextButtonClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        controlBack(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                addView();
                break;
            case R.id.button_next:
                nextButton.setEnabled(false); // lets not allow the user to click the button again and again.
                nextButtonClick();
                break;
        }
    }

    private void addView() {
        rowContainerLinearLayout.addView(getLayoutInflater().inflate(R.layout.row, null));
        findViewById(R.id.scroll_view_main).post(new Runnable() {
            @Override
            public void run() {
                mainScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void payuInit()
    {
        mPaymentParams = new PaymentParams();
        payuConfig = new PayuConfig();
        mPayuHashes=null;
    }

    private void nextButtonClick() {

        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);
        String inputData = null;
        int childNodeCount = rowContainerLayout.getChildCount();

        for (int i = 0; i < childNodeCount; i++) {
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            inputData = ((EditText) linearLayout.getChildAt(1)).getText().toString();
            switch (((EditText) linearLayout.getChildAt(0)).getText().toString()) {
                case PayuConstants.KEY:
                    mPaymentParams.setKey(inputData);
                    merchantKey = inputData;
                    break;
                case PayuConstants.AMOUNT:
                    mPaymentParams.setAmount(inputData);
                    break;
                case PayuConstants.PRODUCT_INFO:
                    mPaymentParams.setProductInfo(inputData);
                    break;
                case PayuConstants.FIRST_NAME:
                    mPaymentParams.setFirstName(inputData);
                    break;
                case PayuConstants.EMAIL:
                    mPaymentParams.setEmail(inputData);
                    break;
                case PayuConstants.TXNID:
                    mPaymentParams.setTxnId(inputData);
                    break;
                case PayuConstants.SURL:
                    mPaymentParams.setSurl(inputData);
                    break;
                case PayuConstants.FURL:
                    mPaymentParams.setFurl(inputData);
                    break;
                case PayuConstants.UDF1:
                    mPaymentParams.setUdf1(inputData);
                    break;
                case PayuConstants.UDF2:
                    mPaymentParams.setUdf2(inputData);
                    break;
                case PayuConstants.UDF3:
                    mPaymentParams.setUdf3(inputData);
                    break;
                case PayuConstants.UDF4:
                    mPaymentParams.setUdf4(inputData);
                    break;
                case PayuConstants.UDF5:
                    mPaymentParams.setUdf5(inputData);
                    break;

                // in case store user card
                case PayuConstants.USER_CREDENTIALS:
                    var1 = inputData;
                    mPaymentParams.setUserCredentials(inputData);
                    break;

                // for offer key
                case PayuConstants.OFFER_KEY:
                    mPaymentParams.setOfferKey(inputData);
                    break;

                // stetting up the environment
                case PayuConstants.ENV:
                    payuConfig.setEnvironment(Integer.valueOf(inputData));
                    break;

                //for one tap payment
                case PayuConstants.STORE_ONE_CLICK_HASH:
                    try {
                        mPaymentParams.setEnableOneClickPayment(Integer.parseInt(inputData));
                    } catch (Exception e) {
                        mPaymentParams.setEnableOneClickPayment(PayuConstants.STORE_ONE_CLICK_HASH_NONE);
                    }
                    break;

                case PayuConstants.PHONE:
                    mPaymentParams.setPhone(inputData);
                    break;

                //if user manually enter salt in UI
                case PayuConstants.SALT:
                    merchantSalt = inputData;
                    break;

                //if user manually enable "sdk_hash_generation" in UI
                case "sdk_hash_generation":
                    if (inputData.equalsIgnoreCase("true")) {
                        if (merchantKey.equalsIgnoreCase(testKey))
                            merchantSalt = testSalt;
                        else if (merchantKey.equalsIgnoreCase(productionKey))
                            merchantSalt = productionSalt;

                        flagGenerateHashFromSDK = true;
                    } else {
                        flagGenerateHashFromSDK = false;
                        merchantSalt = null;
                    }
                    break;

                /*
                * if you have any other payment default param please add them here. something like
                *
                * case PayuConstants.ADDRESS1:
                * mPaymentParams.setAddress1(((EditText) linearLayout.getChildAt(1)).getText().toString());
                * break;
                *
                * */

            }

        }


        //calling hash generation process
        generateHashes(this, flagGenerateHashFromSDK, urlForHash, mPaymentParams, merchantSalt);
    }


    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);

        int childNodeCount = rowContainerLayout.getChildCount();
        // we need a unique txnid every time..
        for (int i = 0; i < childNodeCount; i++) {
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            switch (((EditText) linearLayout.getChildAt(0)).getText().toString()) {
                case PayuConstants.TXNID: // lets set up txnid.
                    ((EditText) linearLayout.getChildAt(1)).setText("" + System.currentTimeMillis());
                    break;
            }

        }
    }


    //just update UI when env is changed from prod to test or vice-versa
    //this is only for sample app
    private void updateUIOnChangeEnv(int env) {
        Log.i("GetData", "updateUIOnChangeEnv");
        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);
        int childNodeCount = rowContainerLayout.getChildCount();

        for (int i = 0; i < childNodeCount; i++) {
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            switch (((EditText) linearLayout.getChildAt(0)).getText().toString()) {
                case PayuConstants.KEY:
                    ((EditText) linearLayout.getChildAt(1)).setText(merchantKey = env == PayuConstants.PRODUCTION_ENV ? productionKey : testKey);
                    break;
                case PayuConstants.USER_CREDENTIALS:
                    ((EditText) linearLayout.getChildAt(1)).setText(merchantKey + ":payutest@payu.in");
                    break;
            }

        }
    }

    //just update UI when merchant key is changed,user credentials are changed.
    //this is only for sample app
    private void updateUIOnChangeUserCredentials(String key) {
        Log.i("GetData", "updateUIOnChangeUserCredentials");
        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);
        int childNodeCount = rowContainerLayout.getChildCount();

        for (int i = 0; i < childNodeCount; i++) {
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            switch (((EditText) linearLayout.getChildAt(0)).getText().toString()) {
                case PayuConstants.USER_CREDENTIALS:
                    ((EditText) linearLayout.getChildAt(1)).setText(key + ":payutest@payu.in");
                    break;
            }

        }
    }


    private void controlBack(int requestCode, int resultCode, final Intent data) {
         /*After transaction completion success/failure contol will come back in this method*/
        if (requestCode == PayuConstants.PAYU_REQUEST_CODE) {
            if (data != null) {

                try {
                    //call if surl-furl is hit
                    //Toast.makeText(this, "RESULT\n"+data.getStringExtra("result"), Toast.LENGTH_SHORT).show();

                    JSONObject jsonObject = new JSONObject(data.getStringExtra(PayuConstants.PAYU_RESPONSE));
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("PayUResponse : "+jsonObject.toString()+"\n\n Merchant Data : "+data.getStringExtra("result"))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).show();

                    if (mPaymentParams.getEnableOneClickPayment() == PayuConstants.STORE_ONE_CLICK_HASH_SERVER
                            && jsonObject.has(PayuConstants.CARD_TOKEN)
                            && jsonObject.has(PayuConstants.MERCHANT_HASH)) {
                        // we have merchant hash, lets store it merchant server.
                        new StoreMerchantHashTask(this, urlForStoreMerchantHash, jsonObject.getString(PayuConstants.CARD_TOKEN), jsonObject.getString(PayuConstants.MERCHANT_HASH), mPaymentParams).execute();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
            } else {
                Toast.makeText(this, "Could not receive data", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void generateHashes(HashGenerationCallBack hashGenerationCallBack, boolean flagGenerateHashFromSDK, String urlForHash, PaymentParams paymentParams, String merchantSalt) {
        if (flagGenerateHashFromSDK) {
            //hash generation from SDK(not recommended)
            new GetHashesFromSDK().generateHashFromSDK(hashGenerationCallBack, paymentParams, merchantSalt);
        } else {

            //hash generation from  merchant's server
            // make api call for hash generation
            new GetHashesFromServerTask(hashGenerationCallBack, urlForHash)
                    .execute(paymentParams);

        }
    }

    public void launchSdkUI(PayuHashes payuHashes, PayuConfig payuConfig, PaymentParams paymentParams, HashMap<String, String> oneClickTokens) {
        Intent intent = new Intent(this, PayUBaseActivity.class);
        // lets add the other params which i might use from other activity
        intent.putExtra(PayuConstants.PAYU_CONFIG, payuConfig);
        intent.putExtra(PayuConstants.PAYMENT_PARAMS, paymentParams);
        intent.putExtra(PayuConstants.PAYU_HASHES, payuHashes);
        intent.putExtra(PayuConstants.STORE_ONE_CLICK_HASH, paymentParams.getEnableOneClickPayment());

        if (oneClickTokens != null)
            intent.putExtra(PayuConstants.ONE_CLICK_CARD_TOKENS, oneClickTokens);


        startActivityForResult(intent, PayuConstants.PAYU_REQUEST_CODE);
    }


    @Override
    public void fetchMerchantHashesAPIResponse(HashMap<String, String> oneClickTokens) {
        Log.i("GetData", "fetchMerchantHashesCompletion ");
        launchSdkUI(mPayuHashes, payuConfig, mPaymentParams, oneClickTokens);
    }

    @Override
    public void storeMerchantHashAPIResponse(String response) {
        Log.i("GetData", "storeMerchantHashCompletion ");
    }

    @Override
    public void deleteMerchantHashAPIResponse(String response) {
        Log.i("GetData", "deleteMerchantHashCompletion ");
    }

    @Override
    public void hashGenerationAPIResponse(PayuHashes payuHashes) {
        Log.i("GetData", "hashGenerationCompletionResponse ");
        mPayuHashes=payuHashes;
        nextButton.setEnabled(true); // lets allow the user to click the button again.
        if (mPaymentParams.getEnableOneClickPayment() == PayuConstants.STORE_ONE_CLICK_HASH_SERVER)
            new FetchMerchantHashes(this, urlForFetchingMerchantHash, mPaymentParams).execute();
        else
            launchSdkUI(payuHashes, payuConfig, mPaymentParams, null);
    }
    

    /**
     * Returns a HashMap object of cardToken and one click hash from merchant server.
     *
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * The function should return a cardToken and corresponding one click hash as a hashMap.
     *
     *@param userCreds   a string giving the user credentials of user.
     *@return            the Hash Map of cardToken and one Click hash.
     **/

    @Override
    public HashMap<String, String > getAllOneClickHash(String userCreds){
        // 1. GET http request from your server
        // GET params - merchant_key, user_credentials.
        // 2. In response we get a
        // this is a sample code for fetching one click hash from merchant server.
        return getAllOneClickHashHelper(merchantKey, userCreds);
    }

    @Override
    public void getOneClickHash(String cardToken, String merchantKey, String userCredentials) {

    }

    /**
     *
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function save the oneClickHash corresponding to its cardToken
     *
     *@param cardToken     a string containing the card token
     *@param oneClickHash  a string containing the one click hash.
     **/

    @Override
    public void saveOneClickHash(String cardToken, String oneClickHash) {
        // 1. POST http request to your server
        // POST params - merchant_key, user_credentials,card_token,merchant_hash.
        // 2. In this POST method the oneclickhash is stored corresponding to card token in merchant server.
        // this is a sample code for storing one click hash on merchant server.

        storeMerchantHash(cardToken, oneClickHash);

    }

    /**
     *
     * This method will be called as a async task, regardless of merchant implementation.
     * Hence, not to call this function as async task.
     * This function delete’s the oneClickHash from the merchant server
     *
     *@param cardToken     a string containing the card token
     *@param userCredentials  a string containing the user credentials.
     **/

    @Override
    public void deleteOneClickHash(String cardToken,  String userCredentials) {

        // 1. POST http request to your server
        // POST params  - merchant_hash.
        // 2. In this POST method the oneclickhash is deleted in merchant server.
        // this is a sample code for deleting one click hash from merchant server.

        deleteMerchantHash(cardToken);

    }
    private void storeMerchantHash(String cardToken, String merchantHash){

        final String postParams = "merchant_key="+merchantKey+"&user_credentials="+var1+"&card_token="+cardToken+"&merchant_hash="+merchantHash;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //  https://mobiledev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://payu.herokuapp.com/store_merchant_hash");

                    byte[] postParamsByte = postParams.getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postParamsByte);

                    InputStream responseInputStream = conn.getInputStream();
                    StringBuffer responseStringBuffer = new StringBuffer();
                    byte[] byteContainer = new byte[1024];
                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                        responseStringBuffer.append(new String(byteContainer, 0, i));
                    }

                    JSONObject response = new JSONObject(responseStringBuffer.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                this.cancel(true);
            }
        }.execute();
    }

    private void deleteMerchantHash(String cardToken){

        final String postParams = "card_token="+cardToken;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //  https://mobiledev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

                    URL url = new URL("https://payu.herokuapp.com/delete_merchant_hash");

                    byte[] postParamsByte = postParams.getBytes("UTF-8");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postParamsByte);

                    InputStream responseInputStream = conn.getInputStream();
//                    StringBuffer responseStringBuffer = new StringBuffer();
//                    byte[] byteContainer = new byte[1024];
//                    for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
//                        responseStringBuffer.append(new String(byteContainer, 0, i));
//                    }
//
//                    JSONObject response = new JSONObject(responseStringBuffer.toString());


//                } catch (JSONException e) {
//                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                this.cancel(true);
            }
        }.execute();
    }

    public HashMap<String, String > getAllOneClickHashHelper(String merchantKey, String userCredentials) {

        // now make the api call.
        final String postParams = "merchant_key=" + merchantKey + "&user_credentials=" + userCredentials ;
        final Intent baseActivityIntent = intent;
        HashMap<String, String> cardTokens  = new HashMap<String, String>();;

        try {
            //  https://mobiled ev.payu.in/admin/wis.php?action=add&uid=124&mid=457&token=74588&cvvhash=0123456789031

            URL url = new URL("https://payu.herokuapp.com/get_merchant_hashes");

            byte[] postParamsByte = postParams.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postParamsByte);

            InputStream responseInputStream = conn.getInputStream();
            StringBuffer responseStringBuffer = new StringBuffer();
            byte[] byteContainer = new byte[1024];
            for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                responseStringBuffer.append(new String(byteContainer, 0, i));
            }

            JSONObject response = new JSONObject(responseStringBuffer.toString());

            JSONArray oneClickCardsArray = response.getJSONArray("data");
            int arrayLength;
            if((arrayLength = oneClickCardsArray.length()) >= 1) {
                for (int i = 0; i < arrayLength; i++) {
                    cardTokens.put(oneClickCardsArray.getJSONArray(i).getString(0), oneClickCardsArray.getJSONArray(i).getString(1));
                }

            }
            // pass these to next activity

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cardTokens;
    }

}
