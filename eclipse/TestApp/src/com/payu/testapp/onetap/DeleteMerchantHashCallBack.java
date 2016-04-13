package com.payu.testapp.onetap;

/**
 * Created by rahulhooda on 23/2/16.
 */
public interface DeleteMerchantHashCallBack {

    /**
     * This will provide status after deleting  merchant hashes and card tokens.
     * */
    void deleteMerchantHashAPIResponse(String response);

}
