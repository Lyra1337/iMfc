package com.google.android.c2dm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class C2DMBroadcastReceiver extends BroadcastReceiver {
    @Override
    public final void onReceive(Context context, Intent intent) {
        // To keep things in one place.
//    	try {
//    		Toast.makeText(context, "push!", Toast.LENGTH_SHORT);
//    	} catch(Exception ex) {
//    		ex.printStackTrace();
//    	}
        C2DMBaseReceiver.runIntentInService(context, intent);
        setResult(Activity.RESULT_OK, null /* data */, null /* extra */);
    	Log.e("lyra-c2dm", "got PUSH!!!11elf");    
    }
}
