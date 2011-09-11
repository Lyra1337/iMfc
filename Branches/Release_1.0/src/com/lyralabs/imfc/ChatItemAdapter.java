package com.lyralabs.imfc;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatItemAdapter extends ArrayAdapter<ChatItem> {
	private ArrayList<ChatItem> _items;
	private Activity _context = null;

	public ChatItemAdapter(Activity context, int textViewResourceId, ArrayList<ChatItem> items) {
		super(context, textViewResourceId, items);
		this._items = items;
		this._context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.i("ChatItemAdapter", "updating View..");
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.chatitem, null);
			//Log.i("ChatItemAdapter", "convertView is null");
		}
		final ChatItem current = this._items.get(position);
		if (current != null) {
			TextView chatterName = (TextView) v.findViewById(R.id.chatterName);
			TextView counter = (TextView) v.findViewById(R.id.unreadItemsCount);
			if (chatterName != null) {
				if(current.getType() == MessageType.Public) {
					chatterName.setText("#" + current.getChannel());
				} else if(current.getType() == MessageType.Private) {
					chatterName.setText(Util.getOtherNick(current.getSender(), current.getReceiver()));
				}
			}
			if (counter != null) {
				counter.setText("(" + current.getUnreadCount() + ")");
			}
		} else {
			Log.i("ChatItemAdapter", "current ChatItem is null");
		}
		
		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ChatItemAdapter.this._context, ChatLog.class);
				i.putExtra("MFC.SENDER", current.getSender());
				i.putExtra("MFC.CHANNEL", current.getChannel());
				i.putExtra("MFC.RECEIVER", current.getReceiver());
				i.putExtra("MFC.TYPE", current.getType().ordinal());
				ChatItemAdapter.this._context.startActivity(i);
				ChatItemAdapter.this._context.finish();
			}
		});
		
		return v;
	}
}