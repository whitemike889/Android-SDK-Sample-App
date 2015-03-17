package com.payu.www.payutestapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.PayU;

import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            setContentView(R.layout.activity_main);
        }
        ((EditText) findViewById(R.id.txn)).setText("" + System.currentTimeMillis());
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.params);
                linearLayout.addView(getLayoutInflater().inflate(R.layout.param, null), linearLayout.getChildCount() - 2);
            }
        });

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.params);
                HashMap<String, String> params = new HashMap<String, String>();
                double amount = 10;
                for (int i = 0; i < linearLayout.getChildCount() - 2; i++) {
                    LinearLayout param = (LinearLayout) linearLayout.getChildAt(i);
                    if(((TextView) param.getChildAt(0)).getText().toString().equals("amount")) {
                        amount = Double.valueOf(((EditText) param.getChildAt(1)).getText().toString());
                    }
                    params.put(((TextView) param.getChildAt(0)).getText().toString(), ((EditText) param.getChildAt(1)).getText().toString());
                }
                params.remove("amount");
                PayU.getInstance(MainActivity.this).startPaymentProcess(amount, params);
            }
        });

//        intent =  new Intent(MainActivity.this, PaymentOptionsActivity.class);
//        params.put(PayU.TXNID, "0nf7" + System.currentTimeMillis());
//        params.put(PayU.PRODUCT_INFO, "product");
//        params.put(PayU.SURL, "http://pay4india.com/check.aspx");
//        params.put(PayU.USER_CREDENTIALS, "smsplus:1msdf");
//        params.put(PayU.FURL, "http://103.15.179.17/AggregatorResponseHandler/aggresphandler/initPayuTrans/app/initPayuResps");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    public void withOutUserDefinedModes(View view) {
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params);
//    }
//
//    public void CcDcNbCash(View view) {
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params, new PayU.PaymentMode[] {PayU.PaymentMode.CC, PayU.PaymentMode.DC, PayU.PaymentMode.NB, PayU.PaymentMode.CASH});
//    }
//
//    public void DcCashNbCc(View view) {
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params, new PayU.PaymentMode[] {PayU.PaymentMode.DC, PayU.PaymentMode.CASH, PayU.PaymentMode.NB, PayU.PaymentMode.CC});
//    }
//
//    public void PayuMoneyStoredCardsCc(View view) {
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params, new PayU.PaymentMode[] {PayU.PaymentMode.PAYU_MONEY, PayU.PaymentMode.STORED_CARDS, PayU.PaymentMode.CC});
//    }
//
//    public void makePaymentUsingCreditCard(View view){
//        intent = new Intent(this, FragmentsContainerActivity.class);
//        intent.putExtra("from", "makePaymentUsingCreditCard");
//        intent.putExtra(PayU.AMOUNT, 100.00);
//        intent.putExtra(PayU.TXNID, "0nf7" + System.currentTimeMillis());
//        intent.putExtra(PayU.PRODUCT_INFO, "product");
//        intent.putExtra(PayU.SURL, "https://dl.dropboxusercontent.com/u/14534468/index.html");
//        intent.putExtra(PayU.FURL, "https://dl.dropboxusercontent.com/u/14534468/index.html");
//        startActivity(intent);
//    }
//
//    public void makePaymentUsingStoredCard(View view){
//        intent = new Intent(this, FragmentsContainerActivity.class);
//        intent.putExtra("from", "makePaymentUsingStoredCard");
//        intent.putExtra(PayU.AMOUNT, 100.00);
//        intent.putExtra(PayU.TXNID, "0nf7" + System.currentTimeMillis());
//        intent.putExtra(PayU.PRODUCT_INFO, "product");
//        intent.putExtra(PayU.SURL, "http://fiddle.jshell.net/u7v4Lqzu/show/");
//        intent.putExtra(PayU.USER_CREDENTIALS, "smsplus:1msdf");
//        intent.putExtra(PayU.FURL, "https://dl.dropboxusercontent.com/u/14534468/index.html");
//        startActivity(intent);
//    }
//
//    public void storeCard(View view){
//        intent = new Intent(this, FragmentsContainerActivity.class);
//        intent.putExtra("from", "storeCard");
//        intent.putExtra(PayU.USER_CREDENTIALS, "smsplus:1msdf");
//        startActivity(intent);
//    }
//
//    public void editCard(View view){
//        intent = new Intent(this, FragmentsContainerActivity.class);
//        intent.putExtra("from", "editCard");
//        intent.putExtra(PayU.USER_CREDENTIALS, "smsplus:1msdf");
//        startActivity(intent);
//    }
//
//    public void deleteCard(View view){
//        intent = new Intent(this, FragmentsContainerActivity.class);
//        intent.putExtra("from", "deleteCard");
//        intent.putExtra(PayU.USER_CREDENTIALS, "smsplus:1msdf");
//        startActivity(intent);
//    }
//
//    public void testEnforce(View view){
//        params.put(PayU.ENFORCE_PAYMETHOD, "creditcard|ICIB|LVRB|TMBB|EMIIC6|EMIIC12|EMIA3|YPAY|DONE|AMON|");
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params);
//    }
//
//    public void dropCategory(View view){
//        params.put(PayU.DROP_CATEGORY, "CC|MAST, DC|VISA|MAST, NB|LVRB|KRKB|ADBB|TMBB, EMI|EMIIC6|EMIIC12|EMIA3, CASH|AMON");
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params);
//    }
//
//    public void testOffer(View view){
//        params.put(PayU.OFFER_KEY, "ujjaltest@4240");
//        PayU.getInstance(MainActivity.this).startPaymentProcess(100, params);
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            if(resultCode == RESULT_OK) {
                //success
                Toast.makeText(this, "Success" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_CANCELED) {
                //failed
                Toast.makeText(this, "Failed" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
        }
    }
}
