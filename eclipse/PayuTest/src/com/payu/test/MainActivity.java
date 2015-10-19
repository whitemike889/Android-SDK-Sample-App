package com.payu.test;

import org.apache.cordova.DroidGap;

import android.os.Bundle;
import android.webkit.JavascriptInterface;



public class MainActivity extends DroidGap {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		initWebView();
	}

	/**
	 * Function initializes  webview & does the necessary settings for webview
	 */

	private void initWebView(){
		// loading application url
		appView.loadUrl("file:///android_asset/main.html");
		// Adding javascript interface
		appView.addJavascriptInterface(new Object(){
			@JavascriptInterface
			public void onResponse(final String postParams){
				appView.post(new Runnable(){
					@Override
					public void run() {
						// We have received the post params. 
						// lets make payment.
						makePayment(postParams);
					}
					
					
				});
			}
			
		}, "PayU");
	}
	
	
	public void makePayment(String postParams){
		// production url https://secure.payu.in/_payment
		appView.postUrl("https://secure.payu.in/_payment", postParams.toString().getBytes());
	}

}