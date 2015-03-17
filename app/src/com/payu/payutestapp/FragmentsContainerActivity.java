package com.payu.www.payutestapp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.payu.sdk.fragments.CreditCardDetailsFragment;
import com.payu.sdk.fragments.DeleteCardFragment;
import com.payu.sdk.fragments.EditCardFragment;
import com.payu.sdk.PayU;
import com.payu.sdk.fragments.StoreCardFragment;
import com.payu.sdk.fragments.StoredCardFragment;

public class FragmentsContainerActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragments_container);

        Bundle extras = getIntent().getExtras();

        Bundle bundle = new Bundle();
        if(getIntent().getExtras().getString(PayU.USER_CREDENTIALS) != null)
            bundle.putString(PayU.USER_CREDENTIALS, getIntent().getExtras().getString(PayU.USER_CREDENTIALS));
        if(getIntent().getExtras().getString(PayU.OFFER_KEY) != null)
            bundle.putString(PayU.OFFER_KEY, getIntent().getExtras().getString(PayU.OFFER_KEY));

        if (savedInstanceState == null){
            if (extras.getString("from").contentEquals("makePaymentUsingCreditCard")) {
                CreditCardDetailsFragment creditCardDetailsFragment = new CreditCardDetailsFragment();
                creditCardDetailsFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentsContainerLayout, creditCardDetailsFragment, "creditCard").commit();
            }
            else if (extras.getString("from").contentEquals("makePaymentUsingStoredCard")) {
                StoredCardFragment storedCardFragment = new StoredCardFragment();
                storedCardFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentsContainerLayout, storedCardFragment, "storedCard").commit();
            }
            else if (extras.getString("from").contentEquals("storeCard")) {
                StoreCardFragment storeCardFragment = new StoreCardFragment();
                storeCardFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentsContainerLayout, storeCardFragment, "storeCard").commit();
            }
            else if (extras.getString("from").contentEquals("editCard")) {
                EditCardFragment editCardFragment = new EditCardFragment();
                editCardFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentsContainerLayout, editCardFragment, "editCard").commit();
            }
            else if (extras.getString("from").contentEquals("deleteCard")) {
                DeleteCardFragment deleteCardFragment = new DeleteCardFragment();
                deleteCardFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentsContainerLayout, deleteCardFragment, "deleteCard").commit();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragments_container, menu);
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
}
