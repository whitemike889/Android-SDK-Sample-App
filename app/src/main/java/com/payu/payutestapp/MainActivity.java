package com.payu.payutestapp;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private String amount;
    private String txnid; //Unique transaction ID
    private String surl = "https://dl.dropboxusercontent.com/s/dtnvwz5p4uymjvg/success.html"; // Add the surl-Success URL here
    private String furl = "https://dl.dropboxusercontent.com/s/z69y7fupciqzr7x/furlWithParams.html"; //Add the furl-failure URL here
    private String productinfo = "myproduct";
    private String key = "0MQaQP"; // Add the key here
    private String firstname = "";
    private String email = "";
    private String ccnum; // add the card number
    private String ccexpmon;
    private String ccexpyr;
    private String ccvv;
    private String ccname = "testuser";
    private String pg= "CC";
    private String device_type = "1";
    private String bankcode = "CC";
    private String postData;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#368ecd")));
    }

    @Override
    protected void onResume() {
        super.onResume();
        txnid=""+System.currentTimeMillis();
        ((EditText)findViewById(R.id.tran_id)).setText(txnid);
        ((EditText)findViewById(R.id.amount)).setText(10+"");
    }
    public void onClick(View view){

        if(((EditText)findViewById(R.id.amount)).getText()!=null)
            amount=((EditText)findViewById(R.id.amount)).getText().toString();

        if(((EditText)findViewById(R.id.cardnum)).getText()!=null)
            ccnum=((EditText)findViewById(R.id.cardnum)).getText().toString();

        if(((EditText)findViewById(R.id.cvv)).getText()!=null)
            ccvv=((EditText)findViewById(R.id.cvv)).getText().toString();

        if(((EditText)findViewById(R.id.exp_month)).getText()!=null)
            ccexpmon=((EditText)findViewById(R.id.exp_month)).getText().toString();

        if(((EditText)findViewById(R.id.exp_year)).getText()!=null)
            ccexpyr=((EditText)findViewById(R.id.exp_year)).getText().toString();

        if(((EditText)findViewById(R.id.tran_id)).getText()!=null)
            txnid=((EditText)findViewById(R.id.tran_id)).getText().toString();

         postData();
    }

    @Override
    public void onBackPressed(){
        boolean disableBack = false;
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            if(bundle!=null)
            disableBack = bundle.containsKey("payu_disable_back") && bundle.getBoolean("payu_disable_back");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(!disableBack) {

            Intent intent = new Intent();
            intent.putExtra("result", "");
            setResult(RESULT_CANCELED, intent);
            super.onBackPressed();
        }
    }

    private void postData(){

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected void onPreExecute() {
                progressBarVisibilityPayuChrome(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids){
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    //"https://mobiletest.payu.in/merchant/postservice?form=2";
                    //https://info.payu.in/merchant/postservice.php?form=2
                    HttpPost httppost = new HttpPost("https://info.payu.in/merchant/postservice.php?form=2");
                    List<NameValuePair> postParams = new ArrayList<NameValuePair>(6);
                    postParams.add(new BasicNameValuePair("command", "mobileHashTestWs"));
                    postParams.add(new BasicNameValuePair("key", key));
                    postParams.add(new BasicNameValuePair("var1", txnid));
                    postParams.add(new BasicNameValuePair("var2", amount));
                    postParams.add(new BasicNameValuePair("var3", productinfo));
                //    postParams.add(new BasicNameValuePair("var4","default"));
                    postParams.add(new BasicNameValuePair("hash", "payu"));
                    httppost.setEntity(new UrlEncodedFormEntity(postParams));
                    JSONObject response = new JSONObject(EntityUtils.toString(httpclient.execute(httppost).getEntity()));
                    // set the hash values here.

                    if(response.has("result") ){
                        //Hash generated
                       postData = response.getJSONObject("result").getString("paymentHash");

                    }
//              PayU.getInstance(MainActivity.this).startPaymentProcess(finalAmount, params, new PayU.PaymentMode[]{PayU.PaymentMode.CC, PayU.PaymentMode.NB});

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                                /*if(mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();*/
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                                /*if(mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();*/
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBarVisibilityPayuChrome(View.GONE);
                try{
              if(postData!=null)
              {
                  //add all parameter to one URL and post it
                  String data = "pg=" + pg + "&device_type=" + device_type + "&txnid=" + txnid + "&amount=" + amount + "&ccnum=" + ccnum + "&ccvv=" + ccvv + "&ccexpmon=" + ccexpmon + "&bankcode=" + bankcode + "&productinfo=" + productinfo + "&key=" + key + "&ccexpyr=" + ccexpyr + "&ccname=" + ccname + "&surl=" + URLEncoder.encode(surl, "UTF-8") +"&furl="+URLEncoder.encode(furl, "UTF-8")+"&hash=" +postData;

//                  String data = "pg=" + pg +"&device_type=" + device_type + "&txnid=" + txnid + "&amount=" + amount + "&ccnum=" + ccnum + "&ccvv=" + ccvv + "&ccexpmon=" + ccexpmon + "&bankcode=" + bankcode + "&productinfo=" + productinfo + "&key=" + key + "&ccexpyr=" + ccexpyr + "&ccname=" + ccname + "&surl=" + URLEncoder.encode(surl, "UTF-8") + "&hash=" +postData+"&furl="+URLEncoder.encode(furl, "UTF-8");
//                String data="device_type=1&ccexpmon=5&bankcode=CC&productinfo=product&ccexpyr=2017&pg=CC&txnid=mytxn1&amount=5.0&ccnum=5123456789012346&ccvv=123&key=smsplus&instrument_type=Manufacturer%3A+unknown+Model%3A+Android+SDK+built+for+x86++Product%3A+sdk_google_phone_x86&ccname=helo&furl=https%253A%252F%252Fdl.dropboxusercontent.com%252Fs%252Fz69y7fupciqzr7x%252FfurlWithParams.html&surl=https%253A%252F%252Fdl.dropboxusercontent.com%252Fs%252Fdtnvwz5p4uymjvg%252Fsuccess.html&instrument_id=bac2cb385007abc7&hash=7d5a09092f1670cace40f4b196ba7e2edf1ec8e8cf8f55ee3a7c75ac37ec4ed8e54abb251f7e70a25550e55f5a4d1e7f8cec30faee2057acd12c1e90f97e252a";
                  Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                  intent.putExtra("postData", data);
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  startActivityForResult(intent, 100);
              }else{
                  Toast.makeText(MainActivity.this,"Not able to process your request", Toast.LENGTH_LONG).show();
              }
              }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    /*private String getPostData() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            String postData = key + "|" + txnid + "|" + amount + "|" + productinfo + "|" + firstname + "|" + email + "|";
            for (int i = 1; i <= 10; i++) {
                postData +=  "|";
            }
            postData += salt;
            messageDigest.update(postData.getBytes());
            byte[] mdbytes = messageDigest.digest();
            StringBuffer hexString = new StringBuffer();
            for (byte hashByte : mdbytes) {
                hexString.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            postData = "";
            postData = "pg=" + pg +"&device_type=" + device_type + "&txnid=" + txnid + "&amount=" + amount + "&ccnum=" + ccnum + "&ccvv=" + ccvv + "&ccexpmon=" + ccexpmon + "&bankcode=" + bankcode + "&productinfo=" + productinfo + "&key=" + key + "&ccexpyr=" + ccexpyr + "&ccname=" + ccname + "&surl=" + URLEncoder.encode(surl, "UTF-8") + "&hash=" + hexString.toString()+"&furl="+URLEncoder.encode(furl, "UTF-8");
            return postData;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public void progressBarVisibilityPayuChrome(int visibility)
    {
        if(getBaseContext()!=null && !isFinishing() ) {
            if (visibility == View.GONE || visibility == View.INVISIBLE) {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            } else if (progressDialog==null || !progressDialog.isShowing()) {
                progressDialog = showProgress(this);
            }
        }
    }
    public ProgressDialog showProgress(Context context) {
        if (getBaseContext() != null && !isFinishing()) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            final Drawable[] drawables = {getResources().getDrawable(R.drawable.l_icon1),
                    getResources().getDrawable(R.drawable.l_icon2),
                    getResources().getDrawable(R.drawable.l_icon3),
                    getResources().getDrawable(R.drawable.l_icon4)
            };

            View layout = mInflater.inflate(R.layout.prog_dialog, null);
            final ImageView imageView;
            imageView = (ImageView) layout.findViewById(R.id.imageView);
            ProgressDialog progDialog = new ProgressDialog(context, R.style.ProgressDialog);

            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int i = -1;

                @Override
                synchronized public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            i++;
                            if (i >= drawables.length) {
                                i = 0;
                            }
                            imageView.setImageBitmap(null);
                            imageView.destroyDrawingCache();
                            imageView.refreshDrawableState();
                            imageView.setImageDrawable(drawables[i]);
                        }
                    });

                }
            }, 0, 500);

            progDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    timer.cancel();
                }
            });
            progDialog.show();
            progDialog.setContentView(layout);
            progDialog.setCancelable(true);
            progDialog.setCanceledOnTouchOutside(false);
            return progDialog;
        }
        return null;
    }
}
