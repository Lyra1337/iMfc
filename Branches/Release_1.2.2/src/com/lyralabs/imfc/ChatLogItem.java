package com.lyralabs.imfc;

public class ChatLogItem {
	private String sender;
	private String receiver;
	private String channel;
	private String message;
	private String timestamp;
	private Boolean read;
	private MessageType type;
	
	public ChatLogItem(String _sender, String _receiver, String _channel, String _message, String _timestamp, Boolean _read, MessageType _type) {
		this.sender = _sender;
		this.receiver = _receiver;
		this.message = _message;
		this.timestamp = _timestamp;
		this.read = _read;
		this.type = _type;
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
		return this.receiver;
	}
	
	public void setChannel(String _channel) {
		this.channel = _channel;
	}

	public String getChannel() {
		return this.channel;
	}

	public void setMessage(String _message) {
		this.message = _message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setSender(String _sender) {
		this.sender = _sender;
	}

	public String getSender() {
		return this.sender;
	}

	public void setTimestamp(String _timestamp) {
		this.timestamp = _timestamp;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public void setRead(Boolean _read) {
		this.read = _read;
	}

	public Boolean getRead() {
		return this.read;
	}
	
	public static MessageType GetMessageType(String type) throws Exception {
		if (type != null)
	    {
	        String val = type.toLowerCase();

	        if(val.equals("privatemessage") || val.equals("privatemsg") || val.equals("m") || val.equals("pm")) {
	        	return MessageType.PrivateMessage;
	        }
	        if(val.equals("p") || val.equals("pp") || val.equals("private")) {
	        	return MessageType.Private;
	        }
	        if(val.equals("pub") || val.equals("public") || val.equals("channel") || val.equals("chan")) {
	        	return MessageType.Public;
	        }
	    }
		
		throw new Exception("client: type is in the wrong Format: [" + type + "]");
	}
}