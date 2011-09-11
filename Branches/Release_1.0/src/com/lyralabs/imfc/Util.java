package com.lyralabs.imfc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Process;
import android.os.IBinder;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class Util {
	public static boolean IsLoggedIn = false;
	private static Notification notification = null;
	private static NotificationManager mNotificationManager = null;
	private static String lastTimestamp = "0";
	private static String pushId;
	public static ArrayList<ChatItem> ChatItems = new ArrayList<ChatItem>();
	public static String authId;
	public static String user;
	public static String pass;
	public static String chan;
	
//	public final static String host = "http://mfc-dev.net:1337";
//	public final static String host = "http://lyralabs.is-a-geek.net:1337";
	public static String host = "http://imfc.mfc-dev.net:1337";
	
	public static Thread updateThread = null;
	public static boolean Notifications = false;
	public static boolean Updated = false;
	private static Integer updateFailCount = 0;
	private static ServiceConnection mConnection = null;
	public static RefreshService mBoundService = null;

	public static void killAll(Context context) {
		if (updateThread != null && updateThread.isAlive()) {
			try {
				updateThread.interrupt();
			} catch (SecurityException ex) {
				ex.printStackTrace();
			}
		}

		if(mBoundService != null) {
			mBoundService.stopSelf();
		}
	}
	
	public static String GetDeviceID(Context context) {
	    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    final WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    
	    String deviceMac, tmDevice, tmSerial, androidId;
	    try {
	    	deviceMac = "" + wm.getConnectionInfo().getMacAddress();
	    } catch(Throwable t) {
	    	deviceMac = "";
	    }
	    try {
	    	tmDevice = "" + tm.getDeviceId();
	    } catch(Throwable t) {
	    	tmDevice = "";
	    }
	    try {
	    	tmSerial = "" + tm.getSimSerialNumber();
	    } catch(Throwable t) {
	    	tmSerial = "";
	    }
	    try {
	    	androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    } catch(Throwable t) {
	    	androidId = "";
	    }

	    UUID deviceUuid = new UUID((androidId.hashCode() | deviceMac.hashCode()), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    String deviceId = deviceUuid.toString();
	    
	    Log.e("DeviceID", deviceId);
	    
	    return deviceId;
	}

	public static void killMe() {
		Process.killProcess(Process.myPid());
	}

	public static String UpdateChatItems(final Activity _context,
			String _timestamp, String _authId) {
		IsLoggedIn = true;
		String timestamp = _timestamp;
		try {
			String url = host + "/ajax/?timestamp=" + _timestamp + "&instance="
					+ _authId;
			
			Log.v("DL-JSON", url);

			String htmlContent = httpGet(url, false);
			
			if (htmlContent.equals("instance not found")) {
				Toast.makeText(_context, "Du bist nicht eingeloggt!", Toast.LENGTH_LONG).show();
			}
			
			Log.v("DL-JSON", htmlContent);

			JSONObject object = new JSONObject(htmlContent);

			timestamp = object.getString("timeStamp");
			final JSONArray array = object.getJSONArray("updates");

			_context.runOnUiThread(new Runnable() {
				public void run() {
					for (int i = 0; i < array.length(); i++) {
						try {
							JSONObject entry = array.getJSONObject(i);
							ChatItem chat = GetChat(entry.getString("sender"),
													entry.getString("receiver"),
													entry.getString("channel"),
													ChatLogItem.GetMessageType(entry.getString("type")));
							
							if(chat == null) {
								Toast.makeText(_context, "Nick nicht eingeloggt oder existiert nicht.", Toast.LENGTH_LONG).show();
								continue;
							}
							
							ChatLogItem cli = new ChatLogItem(
									entry.getString("sender"),
									entry.getString("receiver"),
									entry.getString("channel"),
									entry.getString("message"),
									entry.getString("timestamp"),
									false,
									ChatLogItem.GetMessageType(entry.getString("type"))
							);

							chat.getChatlog().add(cli);
							Util.Updated = true;

							if (Util.Notifications) {
								Util.nofify(_context, cli);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							Log.w("lyra-json-parser", "failed");
						}
					}
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
			if (updateFailCount++ > 4) {
				updateFailCount = 0;
				Log.w("lyra-mfc-updater", "Failed 5 times");
				Toast.makeText(_context, "Connection problems?", Toast.LENGTH_LONG);
				//return lastTimestamp;
			}
			Log.i("lyra-mfc-updater", "retrying update");
			return UpdateChatItems(_context, _timestamp, _authId);
		}
		lastTimestamp = timestamp;
		updateFailCount = 0;
		return timestamp;
	}

	public static void unNotify(Context context) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mNotificationManager.cancel(1);
	}

	public static void bindService(final Context context) {
		if (mConnection == null) {
			mConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					mBoundService = ((RefreshService.RefreshBinder) service)
							.getService();

					Toast.makeText(context, R.string.service_desc,
							Toast.LENGTH_LONG).show();
				}

				public void onServiceDisconnected(ComponentName className) {
					mBoundService = null;
					Toast.makeText(context, "service disconnected.",
							Toast.LENGTH_LONG).show();
				}
			};

			context.bindService(new Intent(context, RefreshService.class),
					mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	public static void nofify(Context context, ChatLogItem cli) {
		if (cli.getSender().equalsIgnoreCase("Charles")
				|| cli.getReceiver().equalsIgnoreCase("Charles")
				|| cli.getSender().equalsIgnoreCase(Util.user)
				|| cli.getType() == MessageType.Public) {
			Log.w("notify", "ignoring nitification");
			return;
		}

		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}

		int icon = R.drawable.android_icon_mfc; // android.R.drawable.stat_notify_chat;
		CharSequence tickerText = "Neue MFC Nachricht";
		long when = System.currentTimeMillis();
		CharSequence contentTitle = cli.getSender();
		CharSequence contentText = cli.getMessage();

		Intent notificationIntent = new Intent(context, Chats.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		if (notification == null) {
			notification = new Notification(icon, tickerText, when);
		}

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notification.defaults |= Notification.DEFAULT_LIGHTS;
		long[] vibrate = { 100, 200, 100, 200, 100, 200 };
		notification.vibrate = vibrate;

		mNotificationManager.notify(1, notification);

		Util.playNotificationSound(context);
	}

	public static String GetLastTimestamp() {
		return lastTimestamp;
	}

	public static ChatItem GetChat(String sender, String receiver, String channel, MessageType type) {
		if(sender == null || sender.length() < 1) {
			return null;
		}
		
		if(type == MessageType.Public && (channel == null || channel.length() < 1)) {
			return null;
		}
		
		if((type == MessageType.Private || type == MessageType.PrivateMessage) && (receiver == null || receiver.length() < 1)) {
			return null;
		}
		
		
		
		if(type == MessageType.Public) {
			for (int i = 0; i < Util.ChatItems.size(); i++) {
				ChatItem current = Util.ChatItems.get(i);
				if (current.getType() == MessageType.Public && current.getChannel().equalsIgnoreCase(channel)) {
					return Util.ChatItems.get(i);
				}
			}
		}
		
		if(type == MessageType.Private) {
			for (int i = 0; i < Util.ChatItems.size(); i++) {
				ChatItem current = Util.ChatItems.get(i);
				String otherChatter = getOtherNick(current.getReceiver(), current.getSender());
				Log.e("otherChatter", "otherChatter: " + otherChatter + " | receiver: " + receiver + " | sender: " + sender);
				if(otherChatter == null) {
					continue;
				}
				if (current.getType() == MessageType.Private && otherChatter.equals("") == false && (otherChatter.equalsIgnoreCase(receiver) || otherChatter.equalsIgnoreCase(sender))) {
					return Util.ChatItems.get(i);
				}
			}
		}
		
		Log.e("GetChat", "Creating new Chat...");
		
		Log.i("GetChat", "Sender [" + sender + "]  Receiver [" + receiver + "]  Channel [" + channel + "]  Type [" + type.name() + "]");
		
		/*for (int i = 0; i < Util.ChatItems.size(); i++) {
			if ((Util.ChatItems.get(i).getSender().equalsIgnoreCase(sender)   && Util.ChatItems.get(i).getReceiver().equalsIgnoreCase(receiver))
		     || (Util.ChatItems.get(i).getSender().equalsIgnoreCase(receiver) && Util.ChatItems.get(i).getReceiver().equalsIgnoreCase(sender))) {
				return Util.ChatItems.get(i);
			}
		}*/

		ChatItem i = new ChatItem();
		i.setChatlog(new ArrayList<ChatLogItem>());

		i.setSender(sender);
		i.setReceiver(receiver);
		i.setChannel(channel);
		i.setType(type);

		Util.ChatItems.add(i);

		return i;
	}

	public static void setAuthId(String authId) {
		Util.authId = authId;
	}

	public static String getAuthId() {
		return authId;
	}

	public static void setPushId(String pushId) {
		Util.pushId = pushId;
	}

	public static String getPushId() {
		return pushId;
	}

	public static String getHost() {
		return host;
	}
	
	public static String getOtherNick(String nick1, String nick2) {
		if(nick1.equalsIgnoreCase(Util.user)) {
			return nick2;
		} else if(nick2.equalsIgnoreCase(Util.user)) {
			return nick1;
		} else {
			return null;
		}
	}

	public static void sendMessage(final String receiver, final String text, final String channel, final MessageType type) {
		(new Thread(new Runnable() {
			public void run() {
				String _channel = channel;
				String _receiver= receiver;
				if(channel == null)
					_channel = "";
				if(receiver== null)
					_receiver = "";
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = host + "/ajax/?action=sendpp&instance="
						+ authId + "&receiver=" + URLEncoder.encode(_receiver)
						+ "&message=" + URLEncoder.encode(text)
						+ "&type=" + type.name().toLowerCase()
						+ "&channel=" + URLEncoder.encode(_channel);
				Log.e("Util", urlString);
				HttpGet get = new HttpGet(urlString);
				try {
					httpClient.execute(get);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		})).start();
	}

	public static void playNotificationSound(Context context) {
		Uri uri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (uri != null) {
			Ringtone rt = RingtoneManager.getRingtone(context, uri);
			if (rt != null) {
				rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
				rt.play();
			}
		}
	}

	public static void savePassword(Context context, String text) {
		Util.writeFile(context, "passfile", text);
	}

	public static void saveUsername(Context context, String text) {
		Util.writeFile(context, "userfile", text);
	}

	public static void saveChannel(Context context, String text) {
		Util.writeFile(context, "chanfile", text);
	}

	public static void saveChatsystem(Context context, String text) {
		Util.writeFile(context, "chatfile", text);
	}

	public static String readPassword(Context context) {
		return Util.readFile(context, "passfile");
	}

	public static String readUsername(Context context) {
		return Util.readFile(context, "userfile");
	}

	public static String readChannel(Context context) {
		return Util.readFile(context, "chanfile");
	}

	public static String readChatsystem(Context context) {
		return Util.readFile(context, "chatfile");
	}
	
	public static String getChatsystem(Context context, int pos) {
		Resources res = context.getResources(); 
		CharSequence[] items = res.getTextArray(R.array.remoteendpoints);
		if(items != null && pos >= 0 && items.length > pos) {
			return (String) items[pos];
		} else {
			return "";
		}
	}

	private static String readFile(Context context, String file) {
		try {
			FileInputStream fis = context.openFileInput(file);
			byte[] buffer = new byte[1024];
			fis.read(buffer);
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < buffer.length; i++) {
				if (buffer[i] != 0)
					sb.append(new String(new byte[] { buffer[i] }));
				else
					break;
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static void writeFile(Context context, String file, String text) {
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(file, Context.MODE_PRIVATE);
			fos.write(text.getBytes());
			fos.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void enablePush() {
		Util.Notifications = true;
		action("enablePush");
	}

	public static void disablePush() {
		Util.Notifications = false;
		action("disablePush");
	}
	
	public static void action(final String action) {
		Util.action(action, "", true);
	}
	
	public static String action(final String action, final Boolean async) {
		return Util.action(action, "", async);
	}
	
	public static void action(final String action, final String args) {
		Util.action(action, args, true);
	}

	public static String action(final String action, final String args, final Boolean async) {
		if(async) {
			(new Thread(new Runnable() {
				public void run() {
					httpGet(host + "/ajax/?instance=" + authId + "&action=" + action + args, true);
				}
			})).start();
			return null;
		} else {
			return httpGet(host + "/ajax/?instance=" + authId + "&action=" + action + args, true);
		}
	}

	public static void registerWithServer(final String regId) {
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				Util.httpGet(Util.getHost() + "/ajax/?action=registerPush"
						+ "&pushid=" + regId + "&instance=" + authId, false);
			}
		});
		t1.start();
	}

	public static String httpGet(String url, boolean newLine) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response;

		try {
			response = httpClient.execute(get);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(instream), 1024 * 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null) {
					if (newLine) {
						sb.append(line + "\n");
					} else {
						sb.append(line);
					}
				}

				return sb.toString();
			}
		} catch (Exception ex) {
			return null;
		}
		return null;
	}

	private static boolean stayonlineEnabled = false;

	public static boolean toggleStayonline() {
		if (stayonlineEnabled) {
			action("disableStayonline");
		} else {
			action("enableStayonline");
		}
		stayonlineEnabled = !stayonlineEnabled;
		return stayonlineEnabled;
	}

	public static void logout() {
		Log.i("iMFC", "logout");
		Util.IsLoggedIn = false;
		Util.action("logout", false);
	}

	public static void getHash(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo( "com.lyralabs.imfc", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.e("Hash", new String(Base64.encode(md.digest(), Base64.DEFAULT)));
			}
		} catch (Exception e) {
			Log.e("Hash", "ERROR");
			e.printStackTrace();
		}
	}
	
	public static void runCommand(String command, String args) {
		Util.action("command", "&command=" + URLEncoder.encode(command) + "&args=" + URLEncoder.encode(args));
	}
}