package com.lyralabs.imfc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;;

public class C2DMReceiver extends C2DMBaseReceiver {
    public C2DMReceiver() {
        super("tomthebig1337@googlemail.com");
    }

    @Override
    public void onRegistered(Context context, String registration) {
    	Log.e("lyra-c2dm", "regId: " + registration);
    	Util.setPushId(registration);
    	Util.registerWithServer(registration);
    }

    @Override
    public void onUnregistered(Context context) {
    }

    @Override
    public void onError(Context context, String errorId) {
    	Log.w("lyra-c2dm", "error: " + errorId);
        //context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    @Override
    public void onMessage(Context context, Intent intent) {
    	Log.i("lyra-c2dm", "push_received");
    	Bundle extras = intent.getExtras();
    	
    	if (extras != null) {
            String sender = (String) extras.get("sender");
            String receiver = (String) extras.get("receiver");
            String authId = (String) extras.get("auth");
            String collapseKey = (String) extras.get("collapse_key");
            
            Log.e("lyra-c2dm", "sender: " + sender + " | " + 
            	  "receiver: " + receiver + " | " + 
            	  "authId: " + authId + " | " + 
            	  "collapseKey: " + collapseKey);
            
            Toast.makeText(context, "sender: " + sender + " | " + 
            	  "receiver: " + receiver + " | " + 
            	  "authId: " + authId + " | " + 
            	  "collapseKey: " + collapseKey, Toast.LENGTH_SHORT).show();
            
            //Intent i = new Intent(context, Login.class);
            //Util.nofify(context, new ChatLogItem(sender, receiver, "", "", false));
            //context.startActivity(i);
        } else {
        	Log.w("lyra-c2dm", "push-data is empty");
            
            Toast.makeText(context, "push-data is empty", Toast.LENGTH_SHORT).show();
        }
    }
}