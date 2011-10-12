package com.lyralabs.imfc;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatLogItemAdapter extends ArrayAdapter<ChatLogItem> {
	private ArrayList<ChatLogItem> _items;
	private Context _context = null;
	private LayoutInflater vi;

	public ChatLogItemAdapter(Context context, int textViewResourceId, ArrayList<ChatLogItem> items) {
		super(context, textViewResourceId, items);
		this._items = items;
		this._context = context;
		this.vi = (LayoutInflater)this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = this.vi.inflate(R.layout.chatlogitem, null);
		}
		ChatLogItem current = this._items.get(position);
		if (current != null) {
			TextView sender = (TextView) v.findViewById(R.id.sender);
			TextView content = (TextView) v.findViewById(R.id.content);
			if (sender != null) {
				sender.setText(current.getSender());
				if(current.getSender().equalsIgnoreCase(Util.user)) {
					sender.setTextColor(Color.GREEN);
				} else {
					sender.setTextColor(Color.CYAN);
				}
			}
			if (content != null) {
				content.setText(current.getMessage());
				content.setTextColor(Color.WHITE);
			}
		}
		v.setClickable(false);
		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				return;
			}
		});
		return v;
	}
}