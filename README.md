# webportalsampleapp
This app uses PayU web portal(web pages) for payment process using CB without SDK. 

**Note :** In this app only CB is integrated,SDK is not integrated.

If you are using only CB then you need only payment hash.

**What is Hash?**

Every transaction (payment or non-payment) needs a hash by the merchant before sending the transaction details to PayU. This is required for PayU to validate the authenticity of the transaction. This should be done on your server.

**Formula to generate payment hash**

￼Payment Hash

    sha512(key|txnid|amount|productinfo|firstname|email|udf1|udf2|udf3|udf4|udf5||||||salt)


For more information regarding PayU APIs and mandatory parameters,refer [Integration Document ver2.5](https://drive.google.com/a/payu.in/file/d/0B-2lbJ9wv91JWTdOODl4c25PWHM/view)
