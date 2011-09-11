package com.lyralabs.imfc;

import java.util.ArrayList;

public class ChatItem {
	private String sender;
	private String receiver;
	private String channel;
	private MessageType type;
	private ArrayList<ChatLogItem> chatlog;
	public void setChatlog(ArrayList<ChatLogItem> chatlog) {
		this.chatlog = chatlog;
	}
	public ArrayList<ChatLogItem> getChatlog() {
		return chatlog;
	}
	public Integer getUnreadCount() {
		int unread = 0;
		for(int i = 0; i < this.chatlog.size(); i++) {
			if(!this.chatlog.get(i).getRead()) {
				unread++;
			}
		}
		return unread;
	}
	public void setSender(String _sender) {
		this.sender = _sender;
	}
	public String getSender() {
		return this.sender;
	}
	public void setChannel(String _channel) {
		this.channel = _channel;
	}
	public String getChannel() {
		return this.channel;
	}
	public void setType(MessageType _type) {
		this.type = _type;
	}
	public MessageType getType() {
		return this.type;
	}
	public void setReceiver(String _receiver) {
		this.receiver = _receiver;
	}
	public String getReceiver() {
		return receiver;
	}
	public void clearChatLog() {
		if(this.chatlog != null && this.chatlog.size() > 0)
			this.chatlog.clear();
	}
	public void close() {
		this.chatlog.clear();
		this.receiver = null;
		this.sender = null;
		this.chatlog = null;
	}
}