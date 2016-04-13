package com.payu.testapp.onetap;

/**
 * Created by rahulhooda on 23/2/16.
 */
public interface StoreMerchantHashCallBack {
    /**
     * This will provide status after storing  merchant hashes and card tokens.
     * */
    void storeMerchantHashAPIResponse(String response);
}
