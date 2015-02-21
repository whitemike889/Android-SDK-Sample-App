package com.payu.eclipseSdkTest;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.PayU;

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
//                String hash = calculateHash(params);
                params.remove("amount");

                PayU.getInstance(MainActivity.this).startPaymentProcess(amount, params);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayU.RESULT) {
            if(resultCode == RESULT_OK) {
                //success
                if(data != null )
                    Toast.makeText(this, "Success" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_CANCELED) {
                //failed
                if(data != null)
                    Toast.makeText(this, "Failed" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
        }
    }   
}
