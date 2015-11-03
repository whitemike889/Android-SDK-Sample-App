package com.payu.payuui.Fragment;


import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.payu.india.Interfaces.DeleteCardApiListener;
import com.payu.india.Model.CardStatus;
import com.payu.india.Model.MerchantWebService;
import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PayuConfig;
import com.payu.india.Model.PayuHashes;
import com.payu.india.Model.PayuResponse;
import com.payu.india.Model.PostData;
import com.payu.india.Model.StoredCard;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.PostParams.MerchantWebServicePostParams;
import com.payu.india.Tasks.DeleteCardTask;
import com.payu.india.Tasks.GetStoredCardTask;
import com.payu.payuui.Activity.PayuBaseActivity;
import com.payu.payuui.Adapter.PagerAdapter;
import com.payu.payuui.Adapter.SavedCardItemFragmentAdapter;
import com.payu.payuui.SdkuiUtil.SdkUIConstants;
import com.payu.payuui.Widget.CirclePageIndicator;
import com.payu.payuui.Interfaces.ClickListener;
import com.payu.payuui.Widget.PageIndicator;
import com.payu.payuui.R;

import com.payu.payuui.Widget.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SavedCardsFragment extends Fragment implements View.OnClickListener, DeleteCardApiListener {


    private SavedCardItemFragmentAdapter mAdapter;
    private ViewPager mPager;
    private ArrayList<StoredCard> mStoreCards;
    private Button deleteButton;
    private CirclePageIndicator indicator;
    private View mView;
    private TextView titleText;

    private PayuHashes payuHashes;
    private PaymentParams mPaymentParams;
    private PayuConfig payuConfig;
    private Bundle mBundle;
    private HashMap<String, CardStatus> valueAddedHashMap;


    public SavedCardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle fragmentBundle = getArguments();
        mStoreCards = fragmentBundle.getParcelableArrayList(PayuConstants.STORED_CARD);
        valueAddedHashMap = (HashMap<String, CardStatus>) fragmentBundle.getSerializable(SdkUIConstants.VALUE_ADDED);

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(this.isVisible()) {
//            getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
//        }
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_saved_cards, container, false);
//        mStoreCards = getArguments().getParcelableArrayList(PayuConstants.STORED_CARD);
        mBundle = ((PayuBaseActivity) getActivity()).bundle;
        payuHashes = mBundle.getParcelable(PayuConstants.PAYU_HASHES);
        mPaymentParams = mBundle.getParcelable(PayuConstants.PAYMENT_PARAMS);
        payuConfig = mBundle.getParcelable(PayuConstants.PAYU_CONFIG);



        mAdapter = new SavedCardItemFragmentAdapter(getChildFragmentManager(), mStoreCards, valueAddedHashMap);
        mPager = (ViewPager) mView.findViewById(R.id.pager_saved_card);
        mPager.setAdapter(mAdapter);
        mPager.setClipToPadding(false);
        //mPager.setPadding(50,0,50,0);
        (deleteButton = (Button) mView.findViewById(R.id.button_delete)).setOnClickListener(this);
        titleText = (TextView) mView.findViewById(R.id.edit_text_title);





//        getActivity().findViewById(R.id.button_pay_now).setEnabled(true);

        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        indicator = (CirclePageIndicator)mView.findViewById(R.id.indicator);
//        mIndicator = indicator;
        indicator.setViewPager(mPager);



        final float density = getResources().getDisplayMetrics().density;
        indicator.setBackgroundColor(0xFFFFFFFF);
//        indicator.setRadius(10 * density);
        indicator.setRadius(3 * density);
        indicator.setPageColor(0xFFC6C6C6);
        indicator.setFillColor(0xFF363535);
//        indicator.setStrokeColor(0xFF000000);
//        indicator.setStrokeWidth(2 * density);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(mStoreCards.get(position).getCardType().equals("SMAE")) {
                    getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
                }
                else{
                    getActivity().findViewById(R.id.button_pay_now).setEnabled(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(mStoreCards.size() == 0){
            deleteButton.setVisibility(View.GONE);
            mPager.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);
            titleText.setText("You have no Stored Cards");

        }

        if(mStoreCards.size()!=0 && mStoreCards.get(0).getCardType().equals("SMAE")){
            getActivity().findViewById(R.id.button_pay_now).setEnabled(true);
        }
        return mView;
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.button_delete){
            int position = mPager.getCurrentItem();
            Log.d("Harmeet##SAVEDCARD", position + " "+mStoreCards.get(position).getMaskedCardNumber());
            deleteCard(mStoreCards.get(position));
        }
    }


    private void deleteCard(StoredCard storedCard) {
        MerchantWebService merchantWebService = new MerchantWebService();
        merchantWebService.setKey(mPaymentParams.getKey());
        merchantWebService.setCommand(PayuConstants.DELETE_USER_CARD);
        merchantWebService.setVar1(mPaymentParams.getUserCredentials());
        merchantWebService.setVar2(storedCard.getCardToken());
        merchantWebService.setHash(payuHashes.getDeleteCardHash());

        PostData postData = null;
        postData = new MerchantWebServicePostParams(merchantWebService).getMerchantWebServicePostParams();

        if (postData.getCode() == PayuErrors.NO_ERROR) {
            // ok we got the post params, let make an api call to payu to fetch
            // the payment related details
            payuConfig.setData(postData.getResult());
            payuConfig.setEnvironment(payuConfig.getEnvironment());

            DeleteCardTask deleteCardTask = new DeleteCardTask(this);
            deleteCardTask.execute(payuConfig);
        } else {
            Toast.makeText(getActivity(), postData.getResult(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteCardApiResponse(PayuResponse payuResponse) {

        if (payuResponse.isResponseAvailable()) {
            Toast.makeText(getActivity(), payuResponse.getResponseStatus().getResult(), Toast.LENGTH_LONG).show();
        }
        if (payuResponse.getResponseStatus().getCode() == PayuErrors.NO_ERROR) {
            // there is no error, lets fetch te cards list.

//            mStoreCards = null;
//            mStoreCards = payuResponse.getStoredCards();
//            mAdapter = new SavedCardItemFragmentAdapter(getChildFragmentManager(), mStoreCards, valueAddedHashMap);



            mStoreCards.remove(mPager.getCurrentItem());
            mPager.getAdapter().notifyDataSetChanged();
//            mAdapter.updateAdapter(mStoreCards);
//            mPager.getAdapter().notifyDataSetChanged();
//            mPager.invalidate();
//            mAdapter = new SavedCardItemFragmentAdapter(getChildFragmentManager(), mStoreCards, valueAddedHashMap);

//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.detach(this).attach(this).commit();


//            mPager.getAdapter().notifyDataSetChanged();
//            mPager.destroyDrawingCache();
//            // mPager.invalidate();
//            mPager.requestLayout();
            // mPager.refreshDrawableState();


//
//            mAdapter = new SavedCardItemFragmentAdapter(getChildFragmentManager(),mStoreCards, valueAddedHashMap);
//
//            mPager = (ViewPager) mView.findViewById(R.id.pager_saved_card);
//            mPager.setAdapter(mAdapter);
//            mPager.setClipToPadding(false);
//
//            mPager.setPageTransformer(true, new ZoomOutPageTransformer());
//
//            indicator = (CirclePageIndicator)mView.findViewById(R.id.indicator);
//            indicator.setViewPager(mPager);
            if(mStoreCards.size() == 0){
                deleteButton.setVisibility(View.GONE);
                mPager.setVisibility(View.GONE);
                indicator.setVisibility(View.GONE);
                titleText.setText("You have no Stored Cards");

            }

        } else {
                Toast.makeText(getActivity(), "Error While Deleting Card", Toast.LENGTH_LONG).show();

        }

    }


}
