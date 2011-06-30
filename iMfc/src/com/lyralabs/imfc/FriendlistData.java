package com.lyralabs.imfc;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
				this.online = new ArrayList<String>();
				for(int i = 0; i < on.length(); i++) {
					this.online.add(on.getString(i));
				}
			}
			if(off != null) {
				this.offline = new ArrayList<String>();
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
		while (i < all.length) {
			if(i >= online.size()) {
				all[i] = offline.get(i - online.size());
			} else {
				all[i] = online.get(i);
			}
			i++;
		}
		return all;
	}
}