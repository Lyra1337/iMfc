package com.lyralabs.imfc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.util.ByteArrayBuffer;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.android.c2dm.C2DMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity {
	private AdView adView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.login);
		
		this.adView = new AdView(this, AdSize.BANNER, "a14e574f8901e4c");
		LinearLayout layout = (LinearLayout)findViewById(R.id.mainLinearLayout);      
		layout.addView(adView);       
		this.adView.loadAd(new AdRequest());
		
		this.setTitle("Lyra` MobileKnuddels [/w br1ght]");
		/*String regId = C2DMessaging.getRegistrationId(this);

		if (regId == null || regId.length() == 0) {
			C2DMessaging.register(this, "tomthebig1337@googlemail.com");
			regId = C2DMessaging.getRegistrationId(this);
		} else {
			Log.e("RegId", "regId: " + regId);
		}
		
		Util.setPushId(regId);
		Util.registerWithServer(regId);*/

		//Util.getHash(this);
		
		final TextView txtUser = (TextView)this.findViewById(R.id.txtUsername);
		final TextView txtPass = (TextView)this.findViewById(R.id.txtPassword);
		final TextView txtChan = (TextView)this.findViewById(R.id.txtChannel);
		final Spinner txtChatsystem = (Spinner)this.findViewById(R.id.txtChatsytem);
		
		int pos = 0;
		try {
			pos = Integer.parseInt(Util.readChatsystem(this).trim());
		} catch(Throwable ex) {
			ex.printStackTrace();
		}
		txtUser.setText(Util.readUsername(this).trim());
		txtPass.setText(Util.readPassword(this).trim());
		txtChan.setText(Util.readChannel(this).trim());
		txtChatsystem.setSelection(pos, true);
		
        final Button btn = (Button) this.findViewById(R.id.btnLogin);
        
        btn.setLongClickable(true);
        btn.setHapticFeedbackEnabled(true);
        btn.setOnLongClickListener(new OnLongClickListener() {	
			public boolean onLongClick(View v) {
				Util.host = "http://lyralabs.is-a-geek.net:1337";
				return false;
			}
		});
        
        //btn.setClickable(false);
        //btn.setVisibility(View.INVISIBLE);
        
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String u = txtUser.getText().toString().trim();
				String p = txtPass.getText().toString().trim();
				String s = Util.getChatsystem(Login.this, txtChatsystem.getSelectedItemPosition());
				String c = txtChan.getText().toString().trim();
				
				final CheckBox cbSave  = (CheckBox) Login.this.findViewById(R.id.cbSavePW);
				
				if(cbSave != null && cbSave.isChecked()) {
					Util.saveUsername(Login.this, u.trim());
					Util.savePassword(Login.this, p.trim());
					Util.saveChannel(Login.this, c.trim());
					Util.saveChatsystem(Login.this, ((Integer)txtChatsystem.getSelectedItemPosition()).toString());
				}
				
				C2DMessaging.register(Login.this, "tomthebig1337@googlemail.com");
				
				TryLogin(u, p, c, s);
			}
		});
		
		if(Util.IsLoggedIn) {
			Intent i = new Intent(this, Chats.class);
			this.startActivity(i);
			this.finish();
			return;
		}
	}
	
	private String base64(String s) {
		return Base64.encodeToString(s.getBytes(), Base64.DEFAULT).replace("=", "%3D").replace("\n", "");
	}
	
	private void TryLogin(final String user, final String pass, final String chan, final String chatSystem) {
		final ProgressDialog dialog = ProgressDialog.show(Login.this, "", "Einloggen...", true);
		dialog.show();
		(new Thread(new Runnable() {
			public void run() {
				try {
					String url = Util.host + "/login/" + 
								 "?nick=" + base64(user) +
								 "&pass=" + base64(pass) +
								 "&chan=" + base64(chan) +
								 "&client=" + base64("Lyra` iMFC for Android") +
								 "&apikey=" + "0CCAFA73-180C-4E92-1337-F7FE0117E6DF" +
								 "&chatsystem=" + chatSystem + 
								 "&hwid=" + Util.GetDeviceID(Login.this);
					
					Log.e("Loginurl", url);
					
					URL updateURL = new URL(url);
			        URLConnection conn = updateURL.openConnection();
			        InputStream is = conn.getInputStream();
			        BufferedInputStream bis = new BufferedInputStream(is, 1024 * 8);
			        ByteArrayBuffer baf = new ByteArrayBuffer(50);
			        
			        int current = 0;
			        while((current = bis.read()) != -1){
			            baf.append((byte)current);
			        }

			        final String auth = new String(baf.toByteArray());
			        
			        if(auth != null && auth.length() > 30 && auth.length() < 40) {

			        	Login.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(Login.this, "Eingeloggt.", Toast.LENGTH_LONG).show();
								
								Intent i = new Intent(Login.this, Chats.class);
								i.putExtra("MFC.USER",   user);
								i.putExtra("MFC.PASS",   pass);
								i.putExtra("MFC.CHAN",   chan);
								i.putExtra("MFC.AUTHID", auth);
	
								Util.IsLoggedIn = true;
								
								Util.setAuthId(auth);
								
								dialog.dismiss();
								
								Login.this.startActivity(i);
								Login.this.finish();
							}
						});
			        } else {
			        	Login.this.runOnUiThread(new Runnable() {
							public void run() {
								dialog.dismiss();
					        	AlertDialog.Builder ab = new AlertDialog.Builder(Login.this);
					        	ab.setTitle("Fehler");
					        	ab.setMessage(auth);
					        	ab.create().show();
							}
						});
			        }
		        } catch(final Exception ex) {
		        	Login.this.runOnUiThread(new Runnable() {
						public void run() {
							dialog.dismiss();
				        	AlertDialog.Builder ab = new AlertDialog.Builder(Login.this);
				        	ab.setTitle("Error");
				        	ab.setMessage(ex.getMessage());
				        	ab.create().show();
						}
					});
		        }
			}
		})).start();
	}

	static String MD5(String str) {
		String hashtext = null;
		try {
			MessageDigest m = null;
			m = MessageDigest.getInstance("MD5");
			m.update(str.getBytes(), 0, str.length());
			hashtext = (new BigInteger(1, m.digest()).toString(16))
					.toString();
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hashtext;
	}
	
	@Override
	public void onDestroy() {
		if(adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
}