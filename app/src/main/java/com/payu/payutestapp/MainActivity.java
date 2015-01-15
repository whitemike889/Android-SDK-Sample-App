package com.payu.payutestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends ActionBarActivity {

    private String amount = "1.0";
    private String txnid = "myTxn123";
    private String surl = "http://www.payu.in";
    private String productinfo = "myproduct";
    private String key = "smsplus";
    private String salt = "1b1b0";
    private String firstname = "";
    private String email = "";

    private String ccnum = "5326760120826933";
    private String ccexpmon = "5";
    private String ccexpyr = "2020";
    private String ccvv = "123";
    private String ccname = "testuser";

    private String pg= "CC";
    private String device_type = "1";
    private String bankcode = "CC";

    private String postData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postData = getPostData();

        setContentView(R.layout.activity_main);

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

    public void onClick(View view){

        Intent intent = new Intent(this, WebviewActivity.class);
        intent.putExtra("postData", postData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 100);

    }

    private String getPostData() {

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
            postData = "pg=" + pg +"&device_type=" + device_type + "&txnid=" + txnid + "&amount=" + amount + "&ccnum=" + ccnum + "&ccvv=" + ccvv + "&ccexpmon=" + ccexpmon + "&bankcode=" + bankcode + "&productinfo=" + productinfo + "&key=" + key + "&ccexpyr=" + ccexpyr + "&ccname=" + ccname + "&surl=" + URLEncoder.encode(surl, "UTF-8") + "&hash=" + hexString.toString();
            return postData;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
