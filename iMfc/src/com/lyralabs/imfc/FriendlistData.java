package com.lyralabs.imfc;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class FriendlistData {
	private ArrayList<String> online = null;
	private ArrayList<String> offline = null;
	
	public FriendlistData(String json) {
		Log.w("FriendlistJson", json);
		try {
			JSONObject obj = new JSONObject(json);
			
			JSONArray on  = null;
			JSONArray off = null;

			this.online = new ArrayList<String>();
			this.offline = new ArrayList<String>();
			
			try {
				on  = obj.getJSONArray("online");
				off = obj.getJSONArray("offline");
			} catch(Exception ex) {
				ex.printStackTrace();
				try {
					on  = obj.getJSONArray("Online");
					off = obj.getJSONArray("Offline");
				} catch(Exception ex2) {
					ex2.printStackTrace();
				}
			}
			if(on != null) {
				for(int i = 0; i < on.length(); i++) {
					this.online.add(on.getString(i));
				}
			}
			if(off != null) {
				for(int i = 0; i < off.length(); i++) {
					this.offline.add(off.getString(i));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getOnline() {
		return this.online;
	}
	
	public ArrayList<String> getOffline() {
		return this.offline;
	}
	
	public CharSequence[] getAll() {
		CharSequence[] all = new CharSequence[this.offline.size() + this.online.size()];
		int i = 0;
		
		ForegroundColorSpan onlineColor  = new ForegroundColorSpan(0xFF00DD00);
		ForegroundColorSpan offlineColor = new ForegroundColorSpan(0xFFDD0000);
		
		while (i < all.length) {
			if(i >= this.online.size()) {
				SpannableString ss = new SpannableString(this.offline.get(i - this.online.size()));
				ss.setSpan(offlineColor, 0, ss.length(), 0);
				all[i] = ss;
			} else {
				SpannableString ss = new SpannableString(this.online.get(i));
				ss.setSpan(onlineColor, 0, ss.length(), 0);
				all[i] = ss;
			}
			i++;
		}
		return all;
	}
}