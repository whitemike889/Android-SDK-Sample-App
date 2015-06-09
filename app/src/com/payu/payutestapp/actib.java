package com.payu.payutestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class actib extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activityone);
	}
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if(keyCode==KeyEvent.KEYCODE_BACK)
	{
		//startActivity(new Intent(actib.this,MainActivity.class));
		finish();
	}
	// TODO Auto-generated method stub
	return super.onKeyDown(keyCode, event);
}
}
