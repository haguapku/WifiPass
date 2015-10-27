package com.haguapku.wificlient.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSIDTranslator {

	// 2.0 SSID format
	/*
	 * | 1 | 1 | 1 | 4 | 1 | ~~~ | |ANY|CHKSUM| MAC | STATIC IP & PORT | FLAG(
	 * extra size 3bits + reserve 2bits + clear 1bit| ~~~ | STATIC IP = 8bits,
	 * PORT = 16bits, SO there are 24 bits, can indicate 24 bits use 4
	 * base64-codes extra size <= 7
	 */

	public static final int EXTRA_SIZE_2X = 0;
	public static final int DM_SSID_MIN_LENGHT_2X = 8;
	public static final int DM_SSID_USERNAME_LENGTH_MAX_2X = 24;
	public static final int DM_SSID_FLAG_POSITION_2X = 7;
	public static final int DM_SSID_CHECKSUM_POSITION_2X = 1;
	public static final int DM_SSID_IPPORT_POSITION_2X = 3;
	public static final int DM_SSID_MAC_POSITION_2X = 2;
	public static final int DM_SSID_EXTRA_MASK_2X = 0x7;
	public static final int DM_SSID_HEADER_SIZE_2X = 8;

	// 1.x
	public static final String DM_HOST_SSID_PREFIX_STANDARD_0 = "D";
	public static final String DM_HOST_SSID_PREFIX_STANDARD_1 = "d";
	public static final String DM_HOST_SSID_PREFIX_FOR_IPHONE_0 = "I";
	public static final String DM_HOST_SSID_PREFIX_FOR_IPHONE_1 = "i";
	public static final String DM_HOST_SSID_SPLITER_FOR_IPHONE = " - ";
	public static final String DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_WINDOWS = "-W-";
	public static final String DM_HOST_SSID_LABEL_USER_PASSWORD_WINDOWS = "-w-";
	public static final String DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_MAC = "-M-";
	public static final String DM_HOST_SSID_LABEL_USER_PASSWORD_MAC = "-m-";
	public static final String DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_LINUX = "-L-";
	public static final String DM_HOST_SSID_LABEL_USER_PASSWORD_LINUX = "-l-";
	public static final int DM_SSID_LABLE_LENGTH_MAX = 3; // Max label bytes
															// (label is part of
															// user name in
															// SSID)
	public static final int DM_SSID_USERNAME_LENGTH_MAX = 27;
	public static final int DM_SSID_HEAD_LENGHT_1X = 5;
	public static final int DM_SSID_MIN_LENGHT_1X = 5;

	public static final String DM_HOST_SSID_PREFIX_STANDARD_2X = "a";
	public static final int DM_USERNAME_LENGTH_MAX_2X = 18;

	public static final int ANDROID = 0;
	public static final int WINDOWS = 1;
	public static final int MACOS = 2;
	public static final int LINUX = 3;
	public static final int UNKNOWN = -1;

	public static final String[] SUPPORTED_2X = { "W", "A", "M", "L", "B", "O",
			"N", "X", "Y", "Z" };
	public static final int[] SUPPORTED_OS_2X = { WINDOWS, ANDROID, MACOS,
			LINUX, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN };

	private static char[] encodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-"
			.toCharArray();
	private static byte[] decodes = new byte[128];
	static {
		for (int i = 0; i < encodes.length; i++) {
			decodes[encodes[i]] = (byte) i;
		}
	}

	private static char toBase64Char(int v) {
		return encodes[v & 0x3f];
	}

	private static char calcChecksum(String s) {
		byte[] b = s.getBytes();
		byte checkSum = 0;
		for (byte tmp : b) {
			checkSum += tmp;
		}

		return toBase64Char(checkSum & 0x3F);
	}

	public static boolean isValidMacRange(String mac) {
		if (mac == null)
			return false;
		if (mac.length() == 0)
			return false;

		Pattern p = Pattern.compile("[0-9A-F]*");
		Matcher m = p.matcher(mac);

		return m.matches(); // TRUE
	}

	public static boolean isSsidAcceptable(String ssid) {
		if (ssid == null || ssid.length() == 0)
			return false;

		return (isSsidPrefixMatched(ssid) != null);
	}

	public static SsidInfo isSsidPrefixMatched(String ssid) {
		if (ssid == null)
			return null;
		if (ssid.length() == 0)
			return null;

		// Ignore quote if having it
		if (ssid.startsWith("\"")) {
			ssid = ssid.substring(1, ssid.length() - 1);
		}

		if (ssid.length() < DM_SSID_MIN_LENGHT_1X) {
			return null;
		}
		String string = ssid.substring(0, 1);
		if (string.equalsIgnoreCase(DM_HOST_SSID_PREFIX_STANDARD_0)
				|| string.equalsIgnoreCase(DM_HOST_SSID_PREFIX_FOR_IPHONE_0)) {
			// check for MAC
			try {
				String mac = ssid.substring(1, 4);
				if (isValidMacRange(mac)) {
					SsidInfo info = new SsidInfo();
					info.version = 1;
					info.leadText = string;
					return info;
				}
			} catch (Exception e) {
			}
			return null;
		}

		if (ssid.length() < DM_SSID_MIN_LENGHT_2X) {
			return null;
		}
     try{
		for (int i = 0; i < SUPPORTED_2X.length; i++) {
			if (SUPPORTED_2X[i].equalsIgnoreCase(string)) {
				char checksum = calcChecksum(ssid.substring(2));
				if (checksum == ssid.charAt(DM_SSID_CHECKSUM_POSITION_2X)) {
					int flag = decodes[ssid.charAt(DM_SSID_FLAG_POSITION_2X)];

					SsidInfo info = new SsidInfo();
					info.version = 2;
					info.osType = SUPPORTED_OS_2X[i];
					info.leadText = string;
					info.extraSize = flag & DM_SSID_EXTRA_MASK_2X;
					info.clear = ((flag & 0x20) == 0x20);
					if (info.extraSize + DM_SSID_HEADER_SIZE_2X > ssid.length()) {
						return null;
					} else {
						return info;
					}
				} else {
					return null;
				}
			}
		}
     }catch(ArrayIndexOutOfBoundsException e){
    	 e.printStackTrace();
     }

		return null;
	}
	
	public static SsidInfo generateSsidInfo(String ssid) {
		try{
		SsidInfo ssidInfo = isSsidPrefixMatched(ssid);
		if (ssidInfo == null) {
			return null;
		}
		if (ssidInfo.version == 1) {
			return generateSsidInfo1X(ssid, ssidInfo);
		}
		return generateSsidInfo2X(ssid, ssidInfo);
		}catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static SsidInfo generateSsidInfo1X(String ssid, SsidInfo ssidInfo) {
		String encodedUserId = "";

		// Ignore quote if having it
		if (ssid.startsWith("\"")) {
			ssid = ssid.substring(1, ssid.length() - 1);
		}

		String decodedUserId = ssid;
		if (DM_HOST_SSID_PREFIX_STANDARD_0.equals(ssidInfo.leadText)
				|| DM_HOST_SSID_PREFIX_STANDARD_1.equals(ssidInfo.leadText)) {
			encodedUserId = ssid.substring(DM_SSID_HEAD_LENGHT_1X); 
			decodedUserId = new String(WiFiUtil.decodeB62(encodedUserId
					.toCharArray()));
			ssidInfo.displayName = encodedUserId;
		} else if (DM_HOST_SSID_PREFIX_FOR_IPHONE_0.equals(ssidInfo.leadText)
				|| DM_HOST_SSID_PREFIX_FOR_IPHONE_1.equals(ssidInfo.leadText)) {
			decodedUserId = ssid.substring(DM_SSID_HEAD_LENGHT_1X); 
			if (decodedUserId.startsWith(DM_HOST_SSID_SPLITER_FOR_IPHONE)
					&& decodedUserId.length() > DM_HOST_SSID_SPLITER_FOR_IPHONE
							.length()) {
				decodedUserId = decodedUserId
						.substring(DM_HOST_SSID_SPLITER_FOR_IPHONE.length());
			}
		}
		
		if (DM_HOST_SSID_PREFIX_FOR_IPHONE_0.equals(ssidInfo.leadText) 
				|| DM_HOST_SSID_PREFIX_STANDARD_0.equals(ssidInfo.leadText)) {
			ssidInfo.shortMac = ssid.substring(1,5);
			ssidInfo.staticIpSubnet = -1;
		} else {
			ssidInfo.shortMac = ssid.substring(3,5);
			ssidInfo.staticIpSubnet = Integer.parseInt(ssid.substring(1, 3), 16);
		}

		
		if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_WINDOWS)) {
			ssidInfo.autoPwd = true;
			ssidInfo.osType = WINDOWS;
		} else if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_USER_PASSWORD_WINDOWS)) {
			ssidInfo.autoPwd = false;
			ssidInfo.osType = WINDOWS;
		} else if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_MAC)) {
			ssidInfo.autoPwd = true;
			ssidInfo.osType = MACOS;
		} else if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_USER_PASSWORD_MAC)) {
			ssidInfo.autoPwd = false;
			ssidInfo.osType = MACOS;
		} else if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_DEFAULT_PASSWORD_LINUX)) {
			ssidInfo.autoPwd = true;
			ssidInfo.osType = LINUX;
		} else if (decodedUserId.startsWith(DM_HOST_SSID_LABEL_USER_PASSWORD_LINUX)) {
			ssidInfo.autoPwd = false;
			ssidInfo.osType = LINUX;
		} else {
			ssidInfo.displayName = decodedUserId;
			ssidInfo.osType = ANDROID;
		}
		
		if (ssidInfo.osType != ANDROID) {
			ssidInfo.displayName = decodedUserId.substring(DM_SSID_LABLE_LENGTH_MAX);
		}
		
		return ssidInfo;
	}
	
	private static SsidInfo generateSsidInfo2X(String ssid, SsidInfo ssidInfo) {
		char c = ssidInfo.leadText.charAt(0);
		
		if (c >= 'A' && c <= 'Z') {
			ssidInfo.autoPwd = true;
		}
		if (ssid.startsWith("\"")) {
			ssid = ssid.substring(1, ssid.length() - 1);
		}
		
		if (!ssidInfo.clear) {
			String encodedUserId = ssid.substring(ssidInfo.extraSize + DM_SSID_HEADER_SIZE_2X); 
			ssidInfo.displayName = new String(WiFiUtil.decodeB62(encodedUserId
					.toCharArray()));
		} else {
			String encodedUserId = ssid.substring(ssidInfo.extraSize + DM_SSID_HEADER_SIZE_2X); 
			if (encodedUserId.startsWith(DM_HOST_SSID_SPLITER_FOR_IPHONE)
					&& encodedUserId.length() > DM_HOST_SSID_SPLITER_FOR_IPHONE
							.length()) {
				encodedUserId = encodedUserId
						.substring(DM_HOST_SSID_SPLITER_FOR_IPHONE.length());
			}
			ssidInfo.displayName = encodedUserId;
		}
		
		int portAndSubnet = decodes[(ssid.charAt(DM_SSID_IPPORT_POSITION_2X + 3))];
		portAndSubnet = ((portAndSubnet << 6)| decodes[(ssid.charAt(DM_SSID_IPPORT_POSITION_2X + 2))]);
		portAndSubnet = ((portAndSubnet << 6)| decodes[(ssid.charAt(DM_SSID_IPPORT_POSITION_2X + 1))]);
		portAndSubnet = ((portAndSubnet << 6)| decodes[(ssid.charAt(DM_SSID_IPPORT_POSITION_2X))]);
		
		ssidInfo.staticIpSubnet = portAndSubnet & 0xFF;
		ssidInfo.port = portAndSubnet >> 8;
		ssidInfo.shortMac = Integer.toHexString(decodes[ssid.charAt(DM_SSID_MAC_POSITION_2X)]);
		if (ssidInfo.shortMac.length() % 2 != 0) {
			ssidInfo.shortMac = "0" + ssidInfo.shortMac;
		}
		
		return ssidInfo;
	}

	public static class SsidInfo {
		public int version;
		public String shortMac;
		public String displayName;
		public int staticIpSubnet;
		public int port;
		public boolean autoPwd;
		public int osType;

		protected String leadText;
		protected boolean clear;
		protected int extraSize;
	}
}
