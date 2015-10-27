package com.haguapku.wificlient.producer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.haguapku.wificlient.activity.R;


public class ProducerUtils {
	
	private static List<WifiItem> wifiList = new ArrayList<WifiItem>();
	private static List<BlackItem> blackList = new ArrayList<BlackItem>();
	private static final String DEFAULT_FILTER_RULE = "[{\"type\":0,\"rules\":[\"^hawk$\"]},{\"type\":1,\"rules\":[\"^cmcc$|^cmcc-auto$|^chinanet$|^chinaunicom$|^cuawifi$\"]}]";

	
	public static void init(Context context) {
		
		// init production
		try {
			InputStream inStream = context.getResources().getAssets()
					.open("producer.xml");
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();

			XMLContentHandler handler = new XMLContentHandler();
			saxParser.parse(inStream, handler);
			inStream.close();
			wifiList = handler.getWifiList();
			blackList = handler.getBlackList();
		} catch (IOException e) {
			Log.e("dengcb", e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e("dengcb", e.getMessage());
		} catch (SAXException e) {
			Log.e("dengcb", e.getMessage());
		}
	}
	
	private static boolean isExistInMacList(List<String> macs, String mac) {
		if ((mac == null) || (mac.length() == 0) || (macs == null)) {
			return false;
		}
		
		for (String string : macs) {
			if (mac.equals(string)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isSimilarSSID(List<KeyItem> keys, String SSID) {
		if ((keys == null) || (SSID == null) || (SSID.length() == 0)) {
			return false;
		}
		
		for (KeyItem keyItem : keys) {
			if ((keyItem.key != null) && SSID.contains(keyItem.key)) {
				return true;
			}
		}
		return false;
	}
	
	public static ItemBase find(String mac, String SSID) {
		
		if (mac != null && mac.length() > 6) {
			mac = mac.replace(":", "").toUpperCase().substring(0, 6);
		}
		if (SSID != null) {
			SSID = SSID.replace("\"", "");
		}
		// find from black list
		for (BlackItem item : blackList) {
			if (isExistInMacList(item.macList, mac)) {
				return item;
			}
		}
		// find from wifi list
		for (WifiItem item : wifiList) {
			if (isExistInMacList(item.macList, mac)) {
				return item;
			}
			if (isSimilarSSID(item.keyList, SSID)) {
				return item;
			}
		}
		return null;
	}
	


	public static int getWiFiIcon(int id) {
		switch (id) {
		case 1:return R.drawable.logo_1;
		case 10:return R.drawable.logo_10;
		case 11:return R.drawable.logo_11;
		case 12:return R.drawable.logo_12;
		case 13:return R.drawable.logo_13;
		case 14:return R.drawable.logo_14;
		case 15:return R.drawable.logo_15;
		case 16:return R.drawable.logo_16;
		case 17:return R.drawable.logo_17;
		case 18:return R.drawable.logo_18;
		case 19:return R.drawable.logo_19;
		//--------------------------------//
		case 2:return R.drawable.logo_2;
		case 20:return R.drawable.logo_20;
		case 21:return R.drawable.logo_21;
		case 22:return R.drawable.logo_22;
		case 23:return R.drawable.logo_23;
		case 24:return R.drawable.logo_24;
		case 25:return R.drawable.logo_25;
		case 26:return R.drawable.logo_26;
		case 27:return R.drawable.logo_27;
		case 28:return R.drawable.logo_28;
		case 29:return R.drawable.logo_29;
		//--------------------------------//
		case 3:return R.drawable.logo_3;
		case 30:return R.drawable.logo_30;
		case 31:return R.drawable.logo_31;
		case 32:return R.drawable.logo_32;
		case 33:return R.drawable.logo_33;
		case 34:return R.drawable.logo_34;
		case 35:return R.drawable.logo_35;
		case 36:return R.drawable.logo_36;
		case 37:return R.drawable.logo_37;
		case 38:return R.drawable.logo_38;
		case 39:return R.drawable.logo_39;
		//--------------------------------//
		case 4:return R.drawable.logo_4;
		case 40:return R.drawable.logo_40;
		case 41:return R.drawable.logo_41;
		case 42:return R.drawable.logo_42;
		case 43:return R.drawable.logo_43;
		case 44:return R.drawable.logo_44;
		case 45:return R.drawable.logo_45;
		case 46:return R.drawable.logo_46;
		case 47:return R.drawable.logo_47;
		case 48:return R.drawable.logo_48;
		case 49:return R.drawable.logo_49;
		//--------------------------------//
		case 5:return R.drawable.logo_5;
		case 50:return R.drawable.logo_50;
		case 51:return R.drawable.logo_51;
		case 52:return R.drawable.logo_52;
		case 53:return R.drawable.logo_53;
		case 54:return R.drawable.logo_54;
		case 55:return R.drawable.logo_55;
		case 56:return R.drawable.logo_56;
		case 57:return R.drawable.logo_57;
		case 58:return R.drawable.logo_58;
		case 59:return R.drawable.logo_59;
		//--------------------------------//
		case 6:return R.drawable.logo_6;
		case 60:return R.drawable.logo_60;
		case 61:return R.drawable.logo_61;
		case 62:return R.drawable.logo_62;
		case 63:return R.drawable.logo_63;
		case 64:return R.drawable.logo_64;
		case 65:return R.drawable.logo_65;
		case 66:return R.drawable.logo_66;
		case 67:return R.drawable.logo_67;
		case 68:return R.drawable.logo_68;
		case 69:return R.drawable.logo_69;
		//--------------------------------//
		case 7:return R.drawable.logo_7;
		case 70:return R.drawable.logo_70;
		case 71:return R.drawable.logo_71;
		case 72:return R.drawable.logo_72;
		case 73:return R.drawable.logo_73;
		//--------------------------------//
		default:return R.drawable.wifi_default_logo;
		}
	}
	
	
	
	
}
