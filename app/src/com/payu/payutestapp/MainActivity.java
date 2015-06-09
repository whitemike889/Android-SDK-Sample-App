package com.payu.payutestapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.sdk.Constants;
import com.payu.sdk.Params;
import com.payu.sdk.PayU;
import com.payu.sdk.Payment;
import com.payu.sdk.ProcessPaymentActivity;
import com.payu.sdk.exceptions.HashException;
import com.payu.sdk.exceptions.MissingParameterException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    Intent intent;
    ProgressDialog mProgressDialog;
    String txnId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(MainActivity.this);

        setContentView(R.layout.activity_main);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.params);
                linearLayout.addView(getLayoutInflater().inflate(R.layout.param, null), linearLayout.getChildCount() - 3);
            }
        });

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.params);
                final HashMap<String, String> params = new HashMap<String, String>();
                double amount = 10;
                for (int i = 0; i < linearLayout.getChildCount() - 3; i++) {
                    LinearLayout param = (LinearLayout) linearLayout.getChildAt(i);
                    if (((TextView) param.getChildAt(0)).getText().toString().equals("amount")) {
                        amount = Double.valueOf(((EditText) param.getChildAt(1)).getText().toString());
                    }
                    params.put(((TextView) param.getChildAt(0)).getText().toString(), ((EditText) param.getChildAt(1)).getText().toString());
                }
                params.remove("amount");

                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Calculating Hash. please wait..");
                mProgressDialog.show();

                final double finalAmount = amount;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost(Constants.FETCH_DATA_URL);
                            List<NameValuePair> postParams = new ArrayList<NameValuePair>(5);
                            postParams.add(new BasicNameValuePair("command", "mobileHashTestWs"));
                            postParams.add(new BasicNameValuePair("key", (getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData).getString("payu_merchant_id")));
                            postParams.add(new BasicNameValuePair("var1", params.get("txnid")));
                            postParams.add(new BasicNameValuePair("var2", String.valueOf(finalAmount)));
                            postParams.add(new BasicNameValuePair("var3", params.get("productinfo")));
                            postParams.add(new BasicNameValuePair("var4", params.get("user_credentials")));
                            postParams.add(new BasicNameValuePair("hash", "payu"));
                            httppost.setEntity(new UrlEncodedFormEntity(postParams));
                            JSONObject response = new JSONObject(EntityUtils.toString(httpclient.execute(httppost).getEntity()));


                            // set the hash values here.

                            if (response.has("result")) {
                                PayU.merchantCodesHash = response.getJSONObject("result").getString("merchantCodesHash");
                                PayU.paymentHash = response.getJSONObject("result").getString("paymentHash");
                                PayU.vasHash = response.getJSONObject("result").getString("mobileSdk");
                                PayU.ibiboCodeHash = response.getJSONObject("result").getString("detailsForMobileSdk");

                                if (response.getJSONObject("result").has("deleteHash")) {
                                    PayU.deleteCardHash = response.getJSONObject("result").getString("deleteHash");
                                    PayU.getUserCardHash = response.getJSONObject("result").getString("getUserCardHash");
                                    PayU.editUserCardHash = response.getJSONObject("result").getString("editUserCardHash");
                                    PayU.saveUserCardHash = response.getJSONObject("result").getString("saveUserCardHash");
                                }

                            }
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();

                            PayU.getInstance(MainActivity.this).startPaymentProcess(finalAmount, params);
//                                PayU.getInstance(MainActivity.this).startPaymentProcess(finalAmount, params, new PayU.PaymentMode[]{PayU.PaymentMode.CC, PayU.PaymentMode.NB});

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            if(mProgressDialog != null && mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                        }
                        return null;
                    }
                    }.execute();
            }
        });

        String version = "version 2.1 sms";

        Toast.makeText(MainActivity.this, Constants.DEBUG ? "Debug build " + version   : "Production build " + version, Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, Constants.SDK_HASH_GENERATION ? "SDK is generating Hash " : "SDK fetches hash form API", Toast.LENGTH_SHORT).show();
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

    public void next(View view){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.params);
        final Intent intent = new Intent(this, BaseActivity.class);
        for (int i = 0; i < linearLayout.getChildCount() - 3; i++) {
            LinearLayout param = (LinearLayout) linearLayout.getChildAt(i);
            intent.putExtra(((TextView) param.getChildAt(0)).getText().toString(), ((EditText) param.getChildAt(1)).getText().toString());
        }

        if(Constants.SDK_HASH_GENERATION) {
            startActivity(intent);
        }else{
            // get the hash from server. lets try with http connector.

            /*postParams.add(new BasicNameValuePair("command", "mobileHashTestWs"));
            postParams.add(new BasicNameValuePair("key", (getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData).getString("payu_merchant_id")));
            postParams.add(new BasicNameValuePair("var1", params.get("txnid")));
            postParams.add(new BasicNameValuePair("var2", String.valueOf(finalAmount)));
            postParams.add(new BasicNameValuePair("var3", params.get("productinfo")));
            postParams.add(new BasicNameValuePair("var4", params.get("user_credentials")));
            postParams.add(new BasicNameValuePair("hash", "payu")); "command=a&param2=b&param3=c";*/

            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Calculating Hash. please wait..");
            mProgressDialog.show();

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String merchantKey = (getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData).getString("payu_merchant_id");
//                        String urlParameters = "command=mobileHashTestWs&key=" + merchantKey + "&var1=" + intent.getStringExtra("txnid") + "&var2=" + intent.getStringExtra("amount") + "&var3=" + intent.getStringExtra("productinfo") + "&var4=" + intent.getStringExtra("user_credentials");

                       /* URL url = new URL(Constants.FETCH_DATA_URL);
                        Map<String, Object> mParams = new LinkedHashMap<>();
                        mParams.put("command", "mobileHashTestWs");
                        mParams.put("key", merchantKey);
                        mParams.put("var1", intent.getStringExtra("txnid"));
                        mParams.put("var2", intent.getStringExtra("amount"));
                        mParams.put("var3", intent.getStringExtra("productinfo"));
                        mParams.put("var4", intent.getStringExtra("user_credentials"));
                        mParams.put("hash", "payu");

                        StringBuilder postData = new StringBuilder();
                        for (Map.Entry<String, Object> param : mParams.entrySet()) {
                            if (postData.length() != 0) postData.append('&');
//                            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                            postData.append(param.getKey());
                            postData.append('=');
//                            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                            postData.append(param.getValue());
                        }
                        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                        conn.setDoOutput(true);
                        conn.getOutputStream().write(postDataBytes);

                        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        String responseString = "";
                        for (int c = in.read(); c != -1; c = in.read())
                            responseString = responseString + (char) c;

                        JSONObject response = new JSONObject(responseString);
                        if (response.has("result")) {
                            PayU.merchantCodesHash = response.getJSONObject("result").getString("merchantCodesHash");
                            PayU.paymentHash = response.getJSONObject("result").getString("paymentHash");
                            PayU.vasHash = response.getJSONObject("result").getString("mobileSdk");
                            PayU.ibiboCodeHash = response.getJSONObject("result").getString("detailsForMobileSdk");

                            if (response.getJSONObject("result").has("deleteHash")) {
                                PayU.deleteCardHash = response.getJSONObject("result").getString("deleteHash");
                                PayU.getUserCardHash = response.getJSONObject("result").getString("getUserCardHash");
                                PayU.editUserCardHash = response.getJSONObject("result").getString("editUserCardHash");
                                PayU.saveUserCardHash = response.getJSONObject("result").getString("saveUserCardHash");
                            }
                        }*/

                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(Constants.FETCH_DATA_URL);
                        List<NameValuePair> postParams = new ArrayList<NameValuePair>(5);
                        postParams.add(new BasicNameValuePair("command", "mobileHashTestWs"));
                        postParams.add(new BasicNameValuePair("key", (getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData).getString("payu_merchant_id")));
                        postParams.add(new BasicNameValuePair("var1", intent.getStringExtra("txnid")));
                        postParams.add(new BasicNameValuePair("var2", intent.getStringExtra("amount")));
                        postParams.add(new BasicNameValuePair("var3", intent.getStringExtra("productinfo")));
                        postParams.add(new BasicNameValuePair("var4", intent.getStringExtra("user_credentials")));
                        postParams.add(new BasicNameValuePair("hash", "payu"));
                        httppost.setEntity(new UrlEncodedFormEntity(postParams));
                        JSONObject response = new JSONObject(EntityUtils.toString(httpclient.execute(httppost).getEntity()));


                        // set the hash values here.

                        if (response.has("result")) {
                            PayU.merchantCodesHash = response.getJSONObject("result").getString("merchantCodesHash");
                            PayU.paymentHash = response.getJSONObject("result").getString("paymentHash");
                            PayU.vasHash = response.getJSONObject("result").getString("mobileSdk");
                            PayU.ibiboCodeHash = response.getJSONObject("result").getString("detailsForMobileSdk");

                            if (response.getJSONObject("result").has("deleteHash")) {
                                PayU.deleteCardHash = response.getJSONObject("result").getString("deleteHash");
                                PayU.getUserCardHash = response.getJSONObject("result").getString("getUserCardHash");
                                PayU.editUserCardHash = response.getJSONObject("result").getString("editUserCardHash");
                                PayU.saveUserCardHash = response.getJSONObject("result").getString("saveUserCardHash");
                            }

                        }
                        mProgressDialog.dismiss();

                        startActivity(intent);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // lets set the transaction id
        ((EditText) findViewById(R.id.txn)).setText(String.valueOf(System.currentTimeMillis()));
    }
}
