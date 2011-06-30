package com.lyralabs.imfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChatLog extends Activity {
	private ListView listView = null;
	private ChatLogItemAdapter adapter;
	private String nick = null;
	private String source = null;
	private Integer lastCount = 0;
	private EditText sendText = null;
	
	private Thread update = new Thread(new Runnable() {
		public void run() {
			while(true) {
				ChatLog.this.runOnUiThread(new Runnable() {
					public void run() {
						ChatLog.this.adapter.notifyDataSetChanged();
						if(ChatLog.this.listView.getScrollY() == 0 && ChatLog.this.lastCount != ChatLog.this.adapter.getCount()) {
							ChatLog.this.listView.setSelection(ChatLog.this.adapter.getCount());
							ChatLog.this.lastCount = ChatLog.this.adapter.getCount();
						}
					}
				});
				try { Thread.sleep(500); } catch (InterruptedException e) {}
			}
		}
	});
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.chatlog);
        
		Util.unNotify(this);
		Util.disablePush();
        
        Bundle extras = this.getIntent().getExtras();
        this.nick = extras.getString("MFC.DEST");
        this.source = extras.getString("MFC.SRC");
        
        if(this.source.startsWith("#")) {
        	this.setTitle("Channel " + this.source);
        } else {
        	this.setTitle("Privates Gespräch mit " + this.nick);
        }
        
        this.listView = (ListView)this.findViewById(R.id.history);
		ChatItem item = Util.GetChat(this.nick, this.source);
        if(item == null) {
			Toast.makeText(ChatLog.this, "Nick nicht eingeloggt oder existiert nicht.", Toast.LENGTH_SHORT).show();
			this.onKeyUp(0, null);
		} else {
			this.adapter = new ChatLogItemAdapter(this, R.layout.chatlogitem, item.getChatlog());
		}
        this.listView.setAdapter(this.adapter);
        this.listView.setClickable(false);
        this.listView.setItemsCanFocus(false);
        
        this.sendText = (EditText) this.findViewById(R.id.sendText);
        this.sendText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && ChatLog.this.sendText.getText().length() > 0) {
					String text = ChatLog.this.sendText.getText().toString();
					if(ChatLog.this.source.startsWith("#")) {
						Util.sendPrivateMessage(ChatLog.this.source, text);
					} else {
						Util.sendPrivateMessage(ChatLog.this.nick, text);
					}
					ChatLog.this.sendText.setText("");
					return true;
				}
				return false;
			}
		});
        
		update.start();
    }
	
	@Override
	protected void onResume() {
		Util.unNotify(this);
		Util.disablePush();
		super.onResume();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if((event == null && keyCode == 0) || event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent(this, Chats.class);
			startActivity(i);
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chatlogmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ChatItem current = Util.GetChat(this.nick, this.source);
		if(current == null) {
			Toast.makeText(ChatLog.this, "Nick nicht eingeloggt oder existiert nicht.", Toast.LENGTH_SHORT).show();
			return false;
		}
		switch (item.getItemId()) {
		case R.id.menu_clearchat:
			current.clearChatLog();
			return true;
			
		case R.id.menu_closechat:
			current.close();
			for(int i = 0; i < Util.ChatItems.size(); i++) {
				if(Util.ChatItems.get(i) == current) {
					Util.ChatItems.remove(i);
					if(Chats.adapter != null) {
						Chats.adapter.notifyDataSetChanged();
					}
				}
			}
			this.finish();
			return true;
			
		default:
			return false;
		}
	}
}