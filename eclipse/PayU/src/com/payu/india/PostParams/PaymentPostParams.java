package com.payu.india.PostParams;

import com.payu.india.Model.PaymentParams;
import com.payu.india.Model.PostData;
import com.payu.india.Payu.PayuConstants;
import com.payu.india.Payu.PayuErrors;
import com.payu.india.Payu.PayuUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by franklin on 27/07/15.
 * Making one common class which takes all the inputs required to make a payment.
 * Inorder to create an object of this kind user needs to provide two inputs.
 */
public class PaymentPostParams extends PayuUtils{

    private PaymentParams mPaymentParams;
    private String mPaymentMode;
    private PostData postData;
    private StringBuffer post;

    /**
     * Not allowing the user to create empty object.
     */
    private PaymentPostParams(){}

    /**
     * PaymentPostParams will accept object of {@link PaymentParams} and a payment mode {@link com.payu.india.Payu.PayuConstants#NB}
     * @param paymentParams
     */
    public PaymentPostParams(PaymentParams paymentParams, String paymentMode){
        this.mPaymentParams = paymentParams;
        this.mPaymentMode = paymentMode;
    }

    public PostData getPaymentPostParams(){
        postData = new PostData();
        post = new StringBuffer();

        // lets validate the pg using payment mode.
        if(PayuConstants.PG_SET.contains(this.mPaymentMode)) { // valid pg good to go.
            post.append(concatParams(PayuConstants.PG, mPaymentMode));
        }else{
           return getReturnData(PayuErrors.INVALID_PG);
        }


        // lets set the default Parameters
        post.append(concatParams(PayuConstants.DEVICE_TYPE, "1"));
        //TODO we gotta set Instrument_type and instrument_id something like
//        post.append(concatParams(PayuConstants.INSTRUMENT_TYPE, ""));
//        post.append(concatParams(PayuConstants.INSTRUMENT_ID, ""));

        // lets begin with the mandatory default params.
        // TODO apply the validation according to the pg, payment mode!
        for (int i = 0; i < PayuConstants.PAYMENT_PARAMS_ARRAY.length; i++) {
            switch (PayuConstants.PAYMENT_PARAMS_ARRAY[i]) {
//                PayuConstants.KEY, PayuConstants.TXNID, PayuConstants.AMOUNT, PayuConstants.PRODUCT_INFO, PayuConstants.FIRST_NAME, PayuConstants.EMAIL, PayuConstants.SURL, PayuConstants.FURL, PayuConstants.HASH
                case PayuConstants.KEY: // TODO add validation for key
                    if (mPaymentParams.getKey() == null || mPaymentParams.getKey().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_KEY_IS_MISSING);
                    post.append(concatParams(PayuConstants.KEY, mPaymentParams.getKey()));
                    break;
                case PayuConstants.TXNID: // TODO add validation for txnid
                    if (mPaymentParams.getTxnId() == null || mPaymentParams.getTxnId().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_TXNID_IS_MISSING);
                    post.append(concatParams(PayuConstants.TXNID, mPaymentParams.getTxnId()));
                    break;
                case PayuConstants.AMOUNT: // validation for amount
                    Double amount = 0.0;
                    try { // this will take care of null check also!
                        amount = mPaymentParams != null ? Double.parseDouble(mPaymentParams.getAmount()) : 0.0;
                    } catch (NumberFormatException e) {
                        return getReturnData(PayuErrors.NUMBER_FORMAT_EXCEPTION, PayuErrors.INVALID_AMOUNT);
                    } catch (NullPointerException e) {
                        return getReturnData(PayuErrors.INVALID_AMOUNT_EXCEPTION, PayuErrors.INVALID_AMOUNT);
                    }
                    if (amount < 1) {
                        return getReturnData(PayuErrors.INVALID_AMOUNT_EXCEPTION, PayuErrors.INVALID_AMOUNT);
                    }
                    post.append(concatParams(PayuConstants.AMOUNT, mPaymentParams.getAmount()));
                    break;
                case PayuConstants.PRODUCT_INFO: // TODO add validation for product info
                    if (mPaymentParams.getProductInfo() == null || mPaymentParams.getProductInfo().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_PRODUCT_INFO_IS_MISSING);
                    post.append(concatParams(PayuConstants.PRODUCT_INFO, mPaymentParams.getProductInfo()));
                    break;
                case PayuConstants.FIRST_NAME: // TODO add validation for first name
                    if (mPaymentParams.getFirstName() == null) // empty string is allowed
                        return getReturnData(PayuErrors.MANDATORY_PARAM_FIRST_NAME_IS_MISSING);
                    post.append(concatParams(PayuConstants.FIRST_NAME, mPaymentParams.getFirstName()));
                    break;
                case PayuConstants.EMAIL: // TODO add validation for email
                    if (mPaymentParams.getEmail() == null)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_EMAIL_IS_MISSING);
                    post.append(concatParams(PayuConstants.EMAIL, mPaymentParams.getEmail()));
                    break;
                case PayuConstants.SURL: // TODO add validation for SURL
                    if (mPaymentParams.getSurl() == null || mPaymentParams.getSurl().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_SURL_IS_MISSING);
                    // we gotta encode surl
                    try {
                        post.append(PayuConstants.SURL + "=" + URLEncoder.encode(mPaymentParams.getSurl(), "UTF-8") + "&");
                    } catch (UnsupportedEncodingException e) {
                        return getReturnData(PayuErrors.UN_SUPPORTED_ENCODING_EXCEPTION, PayuConstants.SURL + PayuErrors.INVALID_URL);
                    }
                    break;
                case PayuConstants.FURL: // TODO add validation for FURL
                    if (mPaymentParams.getFurl() == null || mPaymentParams.getFurl().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_FURL_IS_MISSING);
                    // we gotta encode furl
                    try {
                        post.append(PayuConstants.FURL + "=" + URLEncoder.encode(mPaymentParams.getFurl(), "UTF-8") + "&");
                    } catch (UnsupportedEncodingException e) {
                        return getReturnData(PayuErrors.UN_SUPPORTED_ENCODING_EXCEPTION, PayuConstants.FURL + PayuErrors.INVALID_URL);
                    }
                    break;
                case PayuConstants.HASH: // TODO add validation for Hash
                    if (mPaymentParams.getHash() == null || mPaymentParams.getHash().length() < 1)
                        return getReturnData(PayuErrors.MANDATORY_PARAM_HASH_IS_MISSING);
                    post.append(concatParams(PayuConstants.HASH, mPaymentParams.getHash()));
                    break;
                case PayuConstants.UDF1:
                    if (mPaymentParams.getUdf1() == null)
                        return getReturnData(PayuErrors.INVALID_UDF1);
                    post.append(concatParams(PayuConstants.UDF1, mPaymentParams.getUdf1()));
                    break;
                case PayuConstants.UDF2: // TODO add validation for UDF2
                    if (mPaymentParams.getUdf2() == null)
                        return getReturnData(PayuErrors.INVALID_UDF2);
                    post.append(concatParams(PayuConstants.UDF2, mPaymentParams.getUdf2()));
                    break;
                case PayuConstants.UDF3: // TODO add validation for UDF3
                    if (mPaymentParams.getUdf3() == null)
                        return getReturnData(PayuErrors.INVALID_UDF3);
                    post.append(concatParams(PayuConstants.UDF3, mPaymentParams.getUdf3()));
                    break;
                case PayuConstants.UDF4: // TODO add validation for UDF4
                    if (mPaymentParams.getUdf4() == null)
                        return getReturnData(PayuErrors.INVALID_UDF4);
                    post.append(concatParams(PayuConstants.UDF4, mPaymentParams.getUdf4()));
                    break;
                case PayuConstants.UDF5: // TODO add validation for UDF5
                    if (mPaymentParams.getUdf5() == null)
                        return getReturnData(PayuErrors.INVALID_UDF5);
                    post.append(concatParams(PayuConstants.UDF5, mPaymentParams.getUdf5()));
                    break;
            }
        }

        if (mPaymentParams.getPhone() != null) { // TODO add phone number validation
            post.append(concatParams(PayuConstants.PHONE, mPaymentParams.getPhone()));
        }

        // optional fields.
        post.append(mPaymentParams.getOfferKey() != null ? concatParams(PayuConstants.OFFER_KEY, mPaymentParams.getOfferKey()) : "");
        post.append(mPaymentParams.getLastName() != null ? concatParams(PayuConstants.LASTNAME, mPaymentParams.getLastName()) : "");
        post.append(mPaymentParams.getAddress1() != null ? concatParams(PayuConstants.ADDRESS1, mPaymentParams.getAddress1()) : "");
        post.append(mPaymentParams.getAddress2() != null ? concatParams(PayuConstants.ADDRESS2, mPaymentParams.getAddress2()) : "");
        post.append(mPaymentParams.getCity() != null ? concatParams(PayuConstants.CITY, mPaymentParams.getCity()) : "");
        post.append(mPaymentParams.getState() != null ? concatParams(PayuConstants.STATE, mPaymentParams.getState()) : "");
        post.append(mPaymentParams.getCountry() != null ? concatParams(PayuConstants.COUNTRY, mPaymentParams.getCountry()) : "");
        post.append(mPaymentParams.getZipCode() != null ? concatParams(PayuConstants.ZIPCODE, mPaymentParams.getZipCode()) : "");
        post.append(mPaymentParams.getCodUrl() != null ? concatParams(PayuConstants.CODURL, mPaymentParams.getCodUrl()) : "");
        post.append(mPaymentParams.getDropCategory() != null ? concatParams(PayuConstants.DROP_CATEGORY, mPaymentParams.getDropCategory()) : "");
        post.append(mPaymentParams.getEnforcePayMethod() != null ? concatParams(PayuConstants.ENFORCE_PAYMETHOD, mPaymentParams.getEnforcePayMethod()) : "");
        post.append(mPaymentParams.getCustomNote() != null ? concatParams(PayuConstants.CUSTOM_NOTE, mPaymentParams.getCustomNote()) : "");
        post.append(mPaymentParams.getNoteCategory() != null ? concatParams(PayuConstants.NOTE_CATEGORY, mPaymentParams.getNoteCategory()) : "");
        post.append(mPaymentParams.getShippingFirstName() != null ? concatParams(PayuConstants.SHIPPING_FIRSTNAME, mPaymentParams.getShippingFirstName()) : "");
        post.append(mPaymentParams.getShippingLastName() != null ? concatParams(PayuConstants.SHIPPING_LASTNAME, mPaymentParams.getShippingLastName()) : "");
        post.append(mPaymentParams.getShippingAddress1() != null ? concatParams(PayuConstants.SHIPPING_ADDRESS1, mPaymentParams.getShippingAddress1()) : "");
        post.append(mPaymentParams.getShippingAddress2() != null ? concatParams(PayuConstants.SHIPPING_ADDRESS2, mPaymentParams.getShippingAddress2()) : "");
        post.append(mPaymentParams.getShippingCity() != null ? concatParams(PayuConstants.SHIPPING_CITY, mPaymentParams.getShippingCity()) : "");
        post.append(mPaymentParams.getShippingState() != null ? concatParams(PayuConstants.SHIPPING_STATE, mPaymentParams.getShippingState()) : "");
        post.append(mPaymentParams.getShippingCounty() != null ? concatParams(PayuConstants.SHIPPING_CONTRY, mPaymentParams.getShippingCounty()) : "");
        post.append(mPaymentParams.getShippingZipCode() != null ? concatParams(PayuConstants.SHIPPING_ZIPCODE, mPaymentParams.getShippingZipCode()) : "");
        post.append(mPaymentParams.getShippingPhone() != null ? concatParams(PayuConstants.SHIPPING_PHONE, mPaymentParams.getShippingPhone()) : "");

        // lets setup the user inputs.

        switch (mPaymentMode){
            case PayuConstants.CC:  //credit/debit/stored card
                post.append(concatParams(PayuConstants.BANK_CODE, PayuConstants.CC));
                if (null != this.mPaymentParams.getCardNumber() && validateCardNumber(this.mPaymentParams.getCardNumber())) { // card payment
                    // okay its a valid card number
                    post.append(concatParams(PayuConstants.CC_NUM, this.mPaymentParams.getCardNumber()));
                    // if card number is not smae then validate cvv and expiry.
                    if (!getIssuer(this.mPaymentParams.getCardNumber()).contentEquals(PayuConstants.SMAE)) {
                        if (validateCvv(this.mPaymentParams.getCardNumber(), this.mPaymentParams.getCvv())) {
                            post.append(concatParams(PayuConstants.C_CVV, this.mPaymentParams.getCvv()));
                        } else {
                            return getReturnData(PayuErrors.INVALID_CVV_EXCEPTION, PayuErrors.INVALID_CVV);
                        }
                        try {
                            if (validateExpiry(Integer.parseInt(this.mPaymentParams.getExpiryMonth()), Integer.parseInt(this.mPaymentParams.getExpiryYear()))) {
                                post.append(concatParams(PayuConstants.CC_EXP_YR, this.mPaymentParams.getExpiryYear()));
                                post.append(concatParams(PayuConstants.CC_EXP_MON, this.mPaymentParams.getExpiryMonth()));
                            } else {
                                return getReturnData(PayuErrors.CARD_EXPIRED_EXCEPTION, PayuErrors.CARD_EXPIRED);
                            }
                        } catch (NumberFormatException e) {
                            return getReturnData(PayuErrors.NUMBER_FORMAT_EXCEPTION, PayuErrors.CARD_EXPIRED); // todo wrong expiry format
                        } catch (Exception e){
                            return getReturnData(PayuErrors.MISSING_PARAMETER_EXCEPTION, PayuErrors.CARD_EXPIRED);
                        }
                    }else{ // some smae might have cvv, make sure that we have have added them in our post params.
                        if (validateCvv(this.mPaymentParams.getCardNumber(), this.mPaymentParams.getCvv())) {
                            post.append(concatParams(PayuConstants.C_CVV, this.mPaymentParams.getCvv()));
                        }
                        try {
                            if (validateExpiry(Integer.parseInt(this.mPaymentParams.getExpiryMonth()), Integer.parseInt(this.mPaymentParams.getExpiryYear()))) {
                                post.append(concatParams(PayuConstants.CC_EXP_YR, this.mPaymentParams.getExpiryYear()));
                                post.append(concatParams(PayuConstants.CC_EXP_MON, this.mPaymentParams.getExpiryMonth()));
                            }
                        }catch (Exception e){

                        }
                    }

                    // if name on card is not give use default name on card as "PayuUser"
                    String nameOnCard = null != this.mPaymentParams.getNameOnCard() ? this.mPaymentParams.getNameOnCard() : "PayuUser";
                    // if card name is not given use name on card instead
                    String cardName = null != this.mPaymentParams.getCardName() ? this.mPaymentParams.getCardName() : nameOnCard;
                    post.append(concatParams(PayuConstants.CC_NAME, nameOnCard));
                    if (this.mPaymentParams.getStoreCard() == 1) {
                        if (this.mPaymentParams.getUserCredentials() != null && this.mPaymentParams.getUserCredentials().contains(this.mPaymentParams.getKey() + ":")) {
                            post.append(concatParams(PayuConstants.CARD_NAME, cardName));
                            post.append(this.mPaymentParams.getUserCredentials() != null ? concatParams(PayuConstants.USER_CREDENTIALS, this.mPaymentParams.getUserCredentials()) : "");
                            post.append(this.mPaymentParams.getStoreCard() == 1 ? concatParams(PayuConstants.STORED_CARD, "" + this.mPaymentParams.getStoreCard()) : "");
                        } else {
                            return getReturnData(PayuErrors.USER_CREDENTIALS_NOT_FOUND);
                        }
                    }
                    // TODO add validation for store_card and user_credentials
                    // thats it we can return post Data
                    return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, trimAmpersand(post.toString()));
                }else if (null != this.mPaymentParams.getCardToken()) {
                    // its stored card payment! we gotta verify user credentials
                    if (this.mPaymentParams.getUserCredentials() != null && this.mPaymentParams.getUserCredentials().contains(this.mPaymentParams.getKey() + ":")) {
                        post.append(concatParams(PayuConstants.USER_CREDENTIALS, this.mPaymentParams.getUserCredentials()));
                        post.append(concatParams(PayuConstants.STORE_CARD_TOKEN, this.mPaymentParams.getCardToken()));
                        if (this.mPaymentParams.getCardBin() != null) {
                            // here we have the card bin we can validate cvv, expiry
                            if (!getIssuer(this.mPaymentParams.getCardBin()).contentEquals(PayuConstants.SMAE)) {
                                if (this.mPaymentParams.getCvv() == null) {
                                    return getReturnData(PayuErrors.INVALID_CVV);
                                }
                                if (!validateExpiry(Integer.parseInt(this.mPaymentParams.getExpiryMonth()), Integer.parseInt(this.mPaymentParams.getExpiryYear()))) {
                                    return getReturnData(PayuErrors.CARD_EXPIRED);
                                }
                            }
                        }
                        post.append(this.mPaymentParams.getCvv() != null ? concatParams(PayuConstants.C_CVV, this.mPaymentParams.getCvv()) : concatParams(PayuConstants.C_CVV, "123")); // its not necessary that all the stored cards should have a cvv && we dont have card number so no validation.
                        post.append(this.mPaymentParams.getExpiryMonth() != null ? concatParams(PayuConstants.CC_EXP_MON, this.mPaymentParams.getExpiryMonth()) : concatParams(PayuConstants.CC_EXP_MON, "12"));
                        post.append(this.mPaymentParams.getExpiryYear() != null ? concatParams(PayuConstants.CC_EXP_YR, this.mPaymentParams.getExpiryYear()) : concatParams(PayuConstants.CC_EXP_MON, "2080"));

                        post.append(this.mPaymentParams.getNameOnCard() == null ? concatParams(PayuConstants.CC_NAME, "PayuUser") : concatParams(PayuConstants.CC_NAME, mPaymentParams.getNameOnCard()));
                        // okey we have data
                        return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, trimAmpersand(post.toString()));
                    } else {
                        return getReturnData(PayuErrors.USER_CREDENTIALS_NOT_FOUND_EXCEPTION, PayuErrors.USER_CREDENTIALS_MISSING);
                    }
                } else {
                    return getReturnData(PayuErrors.INVALID_CARD_NUMBER_EXCEPTION, PayuErrors.INVALID_CARD_NUMBER);
                }
            case PayuConstants.NB: // netbanking
                if (this.mPaymentParams.getBankCode() != null && this.mPaymentParams.getBankCode().length() > 1) { // assuming we have a valid bank code now.
                    post.append(concatParams(PayuConstants.BANK_CODE, this.mPaymentParams.getBankCode()));
                } else {
                    return getReturnData(PayuErrors.INVALID_BANKCODE_EXCEPTION, PayuErrors.INVALID_BANK_CODE);
                }
                return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, trimAmpersand(post.toString()));
            case PayuConstants.EMI: // emi
                if (this.mPaymentParams.getBankCode() != null && this.mPaymentParams.getBankCode().length() > 1) { // TODO: add proper validation for bankcode.
                    post.append(concatParams(PayuConstants.PG, PayuConstants.EMI));
                    post.append(concatParams(PayuConstants.BANK_CODE, this.mPaymentParams.getBankCode()));
                    // lets validate card number
                    if (validateCardNumber("" + this.mPaymentParams.getCardNumber())) {
                        // okay its a valid card number
                        post.append(concatParams(PayuConstants.CC_NUM, "" + this.mPaymentParams.getCardNumber()));
                        // if card number is not smae then validate cvv and expiry.
                        if (!getIssuer("" + this.mPaymentParams.getCardNumber()).contentEquals(PayuConstants.SMAE)) {
                            if (validateCvv("" + this.mPaymentParams.getCardNumber(), "" + this.mPaymentParams.getCvv())) {
                                post.append(concatParams(PayuConstants.C_CVV, "" + this.mPaymentParams.getCvv()));
                            } else {
                                return getReturnData(PayuErrors.INVALID_CVV_EXCEPTION, PayuErrors.INVALID_CVV);
                            }
                            try {
                                if (validateExpiry(Integer.parseInt(this.mPaymentParams.getExpiryMonth()), Integer.parseInt(this.mPaymentParams.getExpiryYear()))) {
                                    post.append(concatParams(PayuConstants.CC_EXP_YR, "" + this.mPaymentParams.getExpiryYear()));
                                    post.append(concatParams(PayuConstants.CC_EXP_MON, "" + this.mPaymentParams.getExpiryMonth()));
                                } else {
                                    return getReturnData(PayuErrors.CARD_EXPIRED_EXCEPTION, PayuErrors.CARD_EXPIRED);
                                }
                            } catch (NumberFormatException e) {
                                return getReturnData(PayuErrors.NUMBER_FORMAT_EXCEPTION, PayuErrors.CARD_EXPIRED); // TODO add proper message cast exception
                            }
                        }

                        post.append(this.mPaymentParams.getNameOnCard() == null ? concatParams(PayuConstants.CC_NAME, "PayuUser") : concatParams(PayuConstants.CC_NAME, mPaymentParams.getNameOnCard()));
                        if (this.mPaymentParams.getStoreCard() == 1) {
                            if (this.mPaymentParams.getUserCredentials() != null && this.mPaymentParams.getUserCredentials().contains(this.mPaymentParams.getKey() + ":")) {
                                post.append(this.mPaymentParams.getCardName() == null ? concatParams(PayuConstants.CARD_NAME, "PayuUser") : concatParams(PayuConstants.NAME_ON_CARD, mPaymentParams.getCardName()));
                                post.append(this.mPaymentParams.getUserCredentials() != null ? concatParams(PayuConstants.USER_CREDENTIALS, this.mPaymentParams.getUserCredentials()) : "");
                                post.append(this.mPaymentParams.getStoreCard() == 1 ? concatParams(PayuConstants.STORED_CARD, "" + this.mPaymentParams.getStoreCard()) : "");
                            } else {
                                return getReturnData(PayuErrors.USER_CREDENTIALS_NOT_FOUND);
                            }
                        }
                        // TODO add validation for store_card and user_credentials
                        // thats it we can return post Data
                        return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, trimAmpersand(post.toString()));
                    } else {
                        return getReturnData(PayuErrors.INVALID_CARD_NUMBER_EXCEPTION, PayuErrors.INVALID_CARD_NUMBER);
                    }
                } else {
                    return getReturnData(PayuErrors.INVALID_EMI_DETAILS);
                }
            case PayuConstants.CASH: // cash
                post.append(concatParams(PayuConstants.PG, PayuConstants.CASH)); // cash card
                // lets validate payment bank code
                if (this.mPaymentParams != null && this.mPaymentParams.getBankCode() != null && this.mPaymentParams.getBankCode().length() > 1) { // assuming we have a valid bank code now.
                    post.append(concatParams(PayuConstants.BANK_CODE, this.mPaymentParams.getBankCode()));
                } else {
                    return getReturnData(PayuErrors.INVALID_BANKCODE_EXCEPTION, PayuErrors.INVALID_BANK_CODE);
                }
                return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, post.toString());
            case PayuConstants.PAYU_MONEY: // payu money.
                post.append(concatParams(PayuConstants.BANK_CODE, PayuConstants.PAYUW.toLowerCase()));
                post.append(concatParams(PayuConstants.PG, PayuConstants.WALLET));
                return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, post.toString());
        }

        return getReturnData(PayuErrors.NO_ERROR, PayuConstants.SUCCESS, post.toString());
    }
}
