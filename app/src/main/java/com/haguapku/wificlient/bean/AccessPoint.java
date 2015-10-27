/**
 * 
 */
package com.haguapku.wificlient.bean;

import java.io.Serializable;

import com.haguapku.wificlient.util.SSIDTranslator.SsidInfo;
import com.haguapku.wificlient.util.SSIDTranslator;

/**
 * @author whoze
 * 
 */
public class AccessPoint implements Serializable {

	private static final long serialVersionUID = 6442997438711791415L;

	private double latitude;
	private double longitude;
	private int type;
	private int score;
	private String address;
	private String city;
	private String phone;
	private String capabilities;
	private int describeContents;
	private int level;
	private int frequency;
	private String id;
	private String owner;
	private String ssid;
	private String bssid;
	private String distance;
	private String addrType;
	private int placeType;
	private int safeLevel;
	private int icon;
	private String luyouType;
	private boolean isLock;
	private boolean isCrack;
	private int levelPercent;
	private int signalIcon;

	private boolean isNeedLogin;
	private boolean isCheck;
	private String desc;

	private int networkId;

	private int successNum;
	private int failNum;
	private int speed;
	private int isAvailable;

	private boolean isZapyaHot;

	public AccessPoint(String ssid, String bssid, int type) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.type = type;
	}

	public AccessPoint() {
	}
	
	/**
	 * @return the getSuccessNum
	 */
	public int getSuccessNum() {
		return successNum;
	}

	/**
	 * @param csuccessNum
	 *            the setSuccessNum to set
	 */
	public void setSuccessNum(int csuccessNum) {
		this.successNum = csuccessNum;
	}

	/**
	 * @return the cfailNum
	 */
	public int getFailNum() {
		return failNum;
	}

	/**
	 * @param cfailNum
	 *            the cfailNum to set
	 */
	public void setFailNum(int cfailNum) {
		this.failNum = cfailNum;
	}

	/**
	 * @return the speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @param speed
	 *            the speed to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * @return the isavailable
	 */
	public int isAvailable() {
		return isAvailable;
	}

	/**
	 * @param isavailable
	 *            the isavailable to set
	 */
	public void setAvailable(int isavailable) {
		this.isAvailable = isavailable;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone
	 *            the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the ssid
	 */
	public String getSsid() {
		return ssid;
	}

	public String getDisplayName() {
		if (isZapyaHot) {
			SsidInfo ssidInfo = SSIDTranslator.generateSsidInfo(ssid);
			return ssidInfo.displayName;
		} else {
			return ssid;
		}
	}

	/**
	 * @param ssid
	 *            the ssid to set
	 */
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	/**
	 * @return the distance
	 */
	public String getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(String distance) {
		this.distance = distance;
	}

	/**
	 * @return the safeLevel
	 */
	public int getSafeLevel() {
		return safeLevel;
	}

	/**
	 * @param safeLevel
	 *            the safeLevel to set
	 */
	public void setSafeLevel(int safeLevel) {
		this.safeLevel = safeLevel;
	}

	public String getAddrType() {
		return addrType;
	}

	public void setAddrType(String addrType) {
		this.addrType = addrType;
	}

	public int getPlaceType() {
		return placeType;
	}

	public void setPlaceType(int placeType) {
		this.placeType = placeType;
	}

	public String getBssid() {
		return bssid;
	}

	public void setBssid(String bssid) {
		this.bssid = bssid;
	}

	public String getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getDescribeContents() {
		return describeContents;
	}

	public void setDescribeContents(int describeContents) {
		this.describeContents = describeContents;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getLuyouType() {
		return luyouType;
	}

	public void setLuyouType(String luyouType) {
		this.luyouType = luyouType;
	}

	public int getLevelPercent() {
		return levelPercent;
	}

	public void setLevelPercent(int levelPercent) {
		this.levelPercent = levelPercent;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	public int getSignalIcon() {
		return signalIcon;
	}

	public void setSignalIcon(int signalIcon) {
		this.signalIcon = signalIcon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "ssid:" + ssid + "---bssid=" + bssid;
	}

	public boolean isNeedLogin() {
		return isNeedLogin;
	}

	public void setNeedLogin(boolean isNeedLogin) {
		this.isNeedLogin = isNeedLogin;
	}

	public boolean isCheck() {
		return isCheck;
	}

	public void setCheck(boolean isCheck) {
		this.isCheck = isCheck;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isCrack() {
		return isCrack;
	}

	public void setCrack(boolean isCrack) {
		this.isCrack = isCrack;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public void setZapyaHot(boolean zapyaHot) {
		this.isZapyaHot = zapyaHot;
	}

	public boolean isZapyaHot() {
		return isZapyaHot;
	}
}
