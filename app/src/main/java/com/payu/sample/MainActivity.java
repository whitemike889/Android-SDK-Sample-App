package com.payu.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.PaymentListener;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements PaymentListener{

    String cardNumber;
    String expiryMonth;
    String expiryYear;
    String nameOnCard;
    String cvv;
    Payment payment;
    Payment.Builder builder = new Payment().new Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Constants.ENABLE_VAS){
            if(Constants.ENABLE_VAS && PayU.issuingBankDownBin == null){ // vas has not been called, lets fetch bank down time.
                HashMap<String, String> varList = new HashMap<String, String>();

                varList.put("var1", "default");
                varList.put("var2", "default");
                varList.put("var3", "default");

                List<NameValuePair> postParams = null;

                try {
                    postParams = PayU.getInstance(this).getParams(Constants.GET_VAS, varList);
                    GetResponseTask getResponse = new GetResponseTask(MainActivity.this);
                    getResponse.execute(postParams);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }

        builder.set(PayU.PRODUCT_INFO, "product");
        builder.set(PayU.AMOUNT, "5.0");
        builder.set(PayU.TXNID, String.valueOf("Production :" + System.currentTimeMillis()));
        builder.set(PayU.SURL, "http://www.google.com");
        builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.CC));

        findViewById(R.id.makePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Params requiredParams = new Params();

                cardNumber = String.valueOf(((EditText)findViewById(R.id.cardNumberEditText)).getText());
                expiryMonth = String.valueOf(((EditText)findViewById(R.id.expiryMonth)).getText());
                expiryYear = String.valueOf(((EditText)findViewById(R.id.expiryYear)).getText());
                nameOnCard = String.valueOf(((EditText)findViewById(R.id.nameOnCardEditText)).getText());
                cvv = String.valueOf(((EditText)findViewById(R.id.cvvEditText)).getText());

                requiredParams.put(PayU.CARD_NUMBER, cardNumber);
                requiredParams.put(PayU.EXPIRY_MONTH, String.valueOf(expiryMonth));
                requiredParams.put(PayU.EXPIRY_YEAR, String.valueOf(expiryYear));
                requiredParams.put(PayU.CARDHOLDER_NAME, nameOnCard);
                requiredParams.put(PayU.CVV, cvv);

                requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
                requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
                requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
                requiredParams.put(PayU.SURL, builder.get(PayU.SURL));

                try {
                    Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;

                    requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
                    payment = builder.create();

                    String postData = PayU.getInstance(MainActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(MainActivity.this, ProcessPaymentActivity.class);
                    intent.putExtra(Constants.POST_DATA, postData);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivityForResult(intent, PayU.RESULT);
                } catch (MissingParameterException e) {
                    e.printStackTrace();
                } catch (HashException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        ((EditText)findViewById(R.id.cardNumberEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 7 && PayU.issuingBankDownBin != null && PayU.issuingBankDownBin.has(s.toString().substring(0, 6))){
                    try {
                        ((TextView)findViewById(R.id.errorMessage)).setText("We are experiencing high failure rate for " + PayU.issuingBankDownBin.getString(s.toString().substring(0, 6)) + " cards at this time. We recommend you pay using any other means of payment.");
                        findViewById(R.id.errorMessage).setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    findViewById(R.id.errorMessage).setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
    public void onPaymentOptionSelected(PayU.PaymentMode paymentMode) {

    }

    @Override
    public void onGetResponse(String responseMessage) {
        Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
    }
}
