package com.lyralabs.imfc;

import java.util.ArrayList;

public class ChatItem {
	private String sender;
	private String receiver;
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
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSender() {
		return sender;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
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