package com.payu.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.payu.sdk.Constants;
import com.payu.sdk.GetResponseTask;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.PaymentListener;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.adapters.NetBankingAdapter;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements PaymentListener{

    Payment payment;
    Payment.Builder builder = new Payment().new Builder();
    Params requiredParams = new Params();

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(com.payu.sdk.R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        builder.set(PayU.PRODUCT_INFO, "product");
        builder.set(PayU.AMOUNT, "5.0");
        builder.set(PayU.TXNID, "mytxn1");
        builder.set(PayU.SURL, "http://www.google.com");
        builder.set(PayU.MODE, String.valueOf(PayU.PaymentMode.NB));

        requiredParams.put(PayU.AMOUNT, builder.get(PayU.AMOUNT));
        requiredParams.put(PayU.PRODUCT_INFO, builder.get(PayU.PRODUCT_INFO));
        requiredParams.put(PayU.TXNID, builder.get(PayU.TXNID));
        requiredParams.put(PayU.SURL, builder.get(PayU.SURL));


        List<NameValuePair> postParams = null;

        HashMap<String, String> varList = new HashMap<String, String>();
        varList.put(Constants.VAR1, Constants.DEFAULT);

        try {

            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            requiredParams.put(PayU.MERCHANT_KEY, bundle.getString("payu_merchant_id"));
            payment = builder.create();

            postParams = PayU.getInstance(this).getParams(Constants.GET_IBIBO_CODES, varList);
            GetResponseTask getResponse = new GetResponseTask((com.payu.sdk.PaymentListener) this);
            getResponse.execute(postParams);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (MissingParameterException e) {
            e.printStackTrace();
        }

        ((ListView)findViewById(R.id.netBankingListView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    requiredParams.put(PayU.BANKCODE, ((JSONObject) parent.getAdapter().getItem(position)).getString("code"));

                    String postData = PayU.getInstance(MainActivity.this).createPayment(payment, requiredParams);

                    Intent intent = new Intent(MainActivity.this, ProcessPaymentActivity.class);
                    intent.putExtra(Constants.POST_DATA, postData);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivityForResult(intent, PayU.RESULT);
                } catch (MissingParameterException e) {
                    e.printStackTrace();
                } catch (HashException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    public void onGetAvailableBanks(JSONArray response) {
        NetBankingAdapter netBankingAdapter= new NetBankingAdapter(this, PayU.availableBanks);
        ((ListView)findViewById(R.id.netBankingListView)).setAdapter(netBankingAdapter);
        if(mProgressDialog != null);
        mProgressDialog.dismiss();

    }

    @Override
    public void onGetStoreCardDetails(JSONArray response) {

    }
}
