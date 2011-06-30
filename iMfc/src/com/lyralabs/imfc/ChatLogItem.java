package com.lyralabs.imfc;

public class ChatLogItem {
	private String sender;
	private String receiver;
	private String message;
	private String timestamp;
	private Boolean read;
	
	public ChatLogItem(String _sender, String _receiver, String _message, String _timestamp, Boolean _read) {
		this.sender = _sender;
		this.receiver = _receiver;
		this.message = _message;
		this.timestamp = _timestamp;
		this.read = _read;
	}
	
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSender() {
		return sender;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getRead() {
		return read;
	}
}