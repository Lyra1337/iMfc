package com.lyralabs.imfc;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Chats extends ListActivity {
	private AdView adView;
	
	private String timestamp = "0";

	private ProgressDialog progressDialog = null;
	public static ChatItemAdapter adapter;
	private Boolean updateRunning = false;

	private Runnable updateChats = new Runnable() {
		public void run() {
			if (updateRunning) {
				return;
			}
			while (true) {
				updateRunning = true;
				getChatItems();
				try {
					Thread.sleep(Settings.getUpdateInterval());
				} catch (InterruptedException e) {
					Chats.this.updateRunning = false;
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.chats);
		
		LinearLayout outterLayout = (LinearLayout)this.findViewById(R.id.outterLayout);
		
		this.adView = new AdView(this, AdSize.BANNER, "a14e574f8901e4c");
		outterLayout.addView(adView);       
		this.adView.loadAd(new AdRequest());

		this.setTitle("Lyra` MobileKnuddels [/w br1ght]");
		
		if(adapter != null)
			adapter.notifyDataSetChanged();
		
        this.getListView().setClickable(true);
        this.getListView().setItemsCanFocus(true);
        
		Util.unNotify(this);
		Util.disablePush();
		
		if(Util.IsLoggedIn == false) {
			Intent i = new Intent(this, Login.class);
			this.startActivity(i);
			this.finish();
		}
		
		Bundle extras = this.getIntent().getExtras();

		if (extras != null) {
			Log.d("Extras", "Extras are not null!");
			if (Util.user == null) {
				Util.user = extras.getString("MFC.USER");
				Log.e("Extras", "User: " + Util.user);
			}
			if (Util.pass == null)
				Util.pass = extras.getString("MFC.PASS");
			if (Util.chan == null)
				Util.chan = extras.getString("MFC.CHAN");
			if (Util.authId == null)
				Util.authId = extras.getString("MFC.AUTHID");
		} else {
			Log.e("Extras", "Extras are null!");
		}
		if(this.progressDialog == null) {
			this.progressDialog = ProgressDialog.show(Chats.this, "Please wait...", "Retrieving data ...", true);
		}
		this.updateAdapter();
	}
	
	@Override
	public void onDestroy() {
		if(adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	private void updateAdapter() {
		Log.i("Adapterupdate", "updating adapter...");
		
		if (Util.updateThread == null || !Util.updateThread.isAlive()) {
			Log.i("UpdateThread", "Starting Updatethread");
			Util.updateThread = new Thread(this.updateChats);
			Util.updateThread.start();
		}
		
		adapter = new ChatItemAdapter(this, R.layout.chatitem, Util.ChatItems);
		this.getListView().setAdapter(adapter);
		if(adapter != null)
			adapter.setNotifyOnChange(false);
	}

	@Override
	protected void onResume() {
		if(adapter != null)
			adapter.notifyDataSetChanged();
		Log.e("Chats", "onResume()");
		this.progressDialog.dismiss();
		Util.unNotify(this);
		Util.disablePush();
		super.onResume();
	}
	
	private void getChatItems() {
		try {
			this.timestamp = Util.UpdateChatItems(this, this.timestamp, Util.authId);
			Log.d("BACKGROUND_PROC", "updating chatitems...");
			Thread.sleep(500);
		} catch (Exception e) {
			if(e != null && e.getMessage() != null && e.getMessage().length() > 0) {
				Log.e("BACKGROUND_PROC", "Error Message: " + e.getMessage());
			}
		}
		this.runOnUiThread(returnRes);
	}

	private Runnable returnRes = new Runnable() {
		public void run() {
			try {
				Chats.adapter.notifyDataSetChanged();
				
				Log.i("updater", "notificated datachanged");
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.newchat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*case R.id.menu_togglebot:
			if(Util.toggleStayonline()) {
				Toast.makeText(this, "Stayonline aktiviert", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Stayonline deaktiviert", Toast.LENGTH_LONG).show();
			}
			return true;/**/
			
		case R.id.menu_newchat:
			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = layoutInflater.inflate(R.layout.newchat, (ViewGroup) findViewById(R.id.layout_root));

			final EditText newReceiver = (EditText) layout.findViewById(R.id.text);

			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			alertDialog = builder.create();
			alertDialog.setTitle("Nickname eingeben");
			alertDialog.setButton("Neuer Chat", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ChatItem item = Util.GetChat(newReceiver.getText().toString(), Util.user, null, MessageType.Private);
					if(item == null) {
						Toast.makeText(Chats.this, "Nick nicht eingeloggt oder existiert nicht.", Toast.LENGTH_SHORT).show();
					}
				}
			});
			alertDialog.show();
			return true;
			
		case R.id.menu_cCommand:
			AlertDialog.Builder cbuilder;
			AlertDialog calertDialog;

			LayoutInflater cinflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			View clayout = cinflater.inflate(R.layout.customcommand, (ViewGroup) findViewById(R.id.ccRoot));

			final EditText command = (EditText) clayout.findViewById(R.id.cCommand);
			final EditText args = (EditText) clayout.findViewById(R.id.cArgs);

			cbuilder = new AlertDialog.Builder(this);
			cbuilder.setView(clayout);
			calertDialog = cbuilder.create();
			calertDialog.setTitle("Command ausführen");
			calertDialog.setButton("ausführen", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Util.runCommand(command.getText().toString(), args.getText().toString());
				}
			});
			calertDialog.show();
			return true;
		case R.id.menu_friendList:
			final ProgressDialog friendListWaiter = ProgressDialog.show(Chats.this, "", "Friendlist abrufen...", true);
			
			(new Thread(new Runnable() {
				public void run() {
					String friendListJson = Util.action("friendlist", false);
					Chats.this.runOnUiThread(new Runnable() {
						public void run() {
							friendListWaiter.hide();
						}
					});
					final FriendlistData friendList = new FriendlistData(friendListJson);
					final CharSequence[] items = friendList.getAll();
					
					Chats.this.runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder friendListDialog = new AlertDialog.Builder(Chats.this);
							friendListDialog.setTitle("Friendlist");
							friendListDialog.setItems(items, new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int item) {
							        
							    }
							});
							AlertDialog alert = friendListDialog.create();
							alert.show();
						}
					});
				}
			})).start();
			
			friendListWaiter.show();
			return true;
		case R.id.menu_logout:
			try {
				final ProgressDialog dialog = ProgressDialog.show(Chats.this, "", "Ausloggen...", true);
				dialog.show();
				(new Thread(new Runnable() {
					public void run() {
						try {
							Util.logout();
							Util.IsLoggedIn = false;
							Util.killAll(Chats.this);
							if (Util.updateThread != null) {
								Util.updateThread.interrupt();
								Util.updateThread = null;
							}
							dialog.dismiss();
							Util.killMe();
							Chats.this.finish();
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				})).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			Util.enablePush();
			//Intent i = new Intent(this, Chats.class);
			//this.startActivity(i);
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}