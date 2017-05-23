package com.example.payu.webportalsampleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private String url = "https://test.payu.in/_payment";//for testing
//  private String url = "https://secure.payu.in/_payment";//for production

    //Mandatory feilds for hash generation
//  private static String key = "0MQaQP";//for test production,will work only at PayU only
    private static String key = "gtKFFx"; //for testing
    private static String transaction_Id;
    private static String amount = "10.00 ";
    private static String product_info = "hary23";
    private static String f_Name = "hary";
    private static String email = "hary@hary.com";
    private static String s_Url = "https://payu.herokuapp.com/success";
    private static String f_Url = "https://payu.herokuapp.com/failure";
    private static String user_credentials = key + ":" + "hary@hary.com";
    private static String udf1 = "";
    private static String udf2 = "";
    private static String udf3 = "";
    private static String udf4 = "";
    private static String udf5 = "";
    private String ccnum; // add the card number
    private String ccexpmon;
    private String ccexpyr;
    private String ccvv;
    //optional feilds for hash generation
    private static String offer_key = " ";
    private static String cardBin = " ";

    private String ccname = "testuser";

    // pg value will be according to payment option
    private String pg = "CC";

    //For mobile callback
    private String device_type = "1";

    //bank code will be according to payment option
    private String bankcode = "CC";


    private static String phone = "1234567890";
    private static String payment_hash;

    private ProgressBar spinner;
    private String postData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
//       spinner.setVisibility(View.VISIBLE);

    }


    @Override
    protected void onResume() {
        super.onResume();
        transaction_Id = "" + System.currentTimeMillis();
        ((EditText) findViewById(R.id.tran_id)).setText(transaction_Id);
        ((EditText) findViewById(R.id.amount)).setText(10 + "");
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


    public void pay(View v) {

        // test card credentials

        if (((EditText) findViewById(R.id.amount)).getText() != null)
            amount = ((EditText) findViewById(R.id.amount)).getText().toString();

        if (((EditText) findViewById(R.id.cardnum)).getText() != null)
            ccnum = ((EditText) findViewById(R.id.cardnum)).getText().toString();

        if (((EditText) findViewById(R.id.cvv)).getText() != null)
            ccvv = ((EditText) findViewById(R.id.cvv)).getText().toString();

        if (((EditText) findViewById(R.id.exp_month)).getText() != null)
            ccexpmon = ((EditText) findViewById(R.id.exp_month)).getText().toString();

        if (((EditText) findViewById(R.id.exp_year)).getText() != null)
            ccexpyr = ((EditText) findViewById(R.id.exp_year)).getText().toString();


        if (((EditText) findViewById(R.id.tran_id)).getText() != null)
            transaction_Id = ((EditText) findViewById(R.id.tran_id)).getText().toString();

        postData = "txnid=" + transaction_Id +
                "&device_type=" + device_type +
                "&ismobileview=1" +
                "&productinfo=" + product_info +
                "&user_credentials=" + user_credentials +
                "&key=" + key +
                "&instrument_type=Put here Device info " +
                "&surl=" + s_Url +
                "&furl=" + f_Url + "" +
                "&instrument_id=7dd17561243c202" +
                "&firstname=" + f_Name +
                "&email=" + email +
                "&phone=" + phone +
                "&amount=" + amount +
                "&ccnum=" + ccnum +
                "&ccvv=" + ccvv +
                "&ccexpmon=" + ccexpmon +
                "&ccexpyr=" + ccexpyr +
                "&pg=" + pg +
                "&bankcode=" + bankcode +

//                "&bankcode=PAYUW" + //for PayU Money
//                "&pg=WALLET"+//for PayU Money
                "&hash=";
        generateHashFromServer();
    }

    public void generateHashFromServer() {

        // lets create the post params

        StringBuffer postParamsBuffer = new StringBuffer();
        postParamsBuffer.append(concatParams("key", key));
        postParamsBuffer.append(concatParams("amount", amount));
        postParamsBuffer.append(concatParams("txnid", transaction_Id));
        postParamsBuffer.append(concatParams("email", email));
        postParamsBuffer.append(concatParams("productinfo", product_info));
        postParamsBuffer.append(concatParams("firstname", f_Name));
        postParamsBuffer.append(concatParams("udf1", udf1));
        postParamsBuffer.append(concatParams("udf2", udf2));
        postParamsBuffer.append(concatParams("udf3", udf3));
        postParamsBuffer.append(concatParams("udf4", udf4));
        postParamsBuffer.append(concatParams("udf5", udf5));
        postParamsBuffer.append(concatParams("user_credentials", user_credentials));

        // for offer_key(optional)
        postParamsBuffer.append(concatParams("offer_key", offer_key));
        // for check_isDomestic(oprional)
        postParamsBuffer.append(concatParams("card_bin", cardBin));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();
        // make api call
        GetHashesFromServerTask getHashesFromServerTask = new GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);

    }

    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }


    /**
     * This Asyntask will hit API to get all hashes from server.
     */
    class GetHashesFromServerTask extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... postParams) {

            try {


                URL url = new URL("https://payu.herokuapp.com/get_hash");//replace this url with your server url for hash generation

                // get the payuConfig first
                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

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
                return response.getString("payment_hash");


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String payment_hash) {
            //  System.out.println("paymenthash" + payment_hash);
            spinner.setVisibility(View.GONE);
            Intent intent = new Intent(MainActivity.this, PaymentsActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("postData", postData + payment_hash);
            startActivityForResult(intent, 100);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Control will come back to this  place when transaction completed(for both fail and success)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
//success
                if (data != null) {

                    /**
                     * Here, data.getStringExtra("payu_response") ---> Implicit response sent by PayU
                     * data.getStringExtra("result") ---> Response received from merchant's Surl/Furl
                     *
                     * PayU sends the same response to merchant server and in app. In response check the value of key "status"
                     * for identifying status of transaction. There are two possible status like, success or failure
                     * */

                    new AlertDialog.Builder(this)
                            .setCancelable(false)

                            .setMessage("Payu's Data : " + data.getStringExtra("payu_response") + "\n\n\n Merchant's Data: " + data.getStringExtra("result"))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).show();
                }
            }
            if (resultCode == RESULT_CANCELED) {
//failed
                if (data != null)
                    Toast.makeText(this, "Failed" + data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
        }
    }


}

