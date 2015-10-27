package com.haguapku.wificlient.producer;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLContentHandler extends DefaultHandler {
	private List<WifiItem> wifiList = null;
	private List<BlackItem> blackList = null;

	private String tagName = null;
	private WifiItem currentWifiItem;
	private BlackItem currentBlackItem;
	private KeyItem currentKeyItem;

	public List<WifiItem> getWifiList() {
		return wifiList;
	}
	
	public List<BlackItem> getBlackList() {
		return blackList;
	}
	
	@Override
	public void startDocument() throws SAXException {
		wifiList = new ArrayList<WifiItem>();
		blackList = new ArrayList<BlackItem>();
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("wifi")) {
			currentWifiItem = new WifiItem();
			currentWifiItem.id = atts.getValue("id");
			currentWifiItem.name = atts.getValue("name");
			currentWifiItem.type = atts.getValue("type");
			currentWifiItem.check = Boolean.parseBoolean(atts.getValue("check"));
			currentWifiItem.isNeedLogin = Boolean.parseBoolean(atts.getValue("isNeedLogin"));
			currentWifiItem.macList = new ArrayList<String>();
			currentWifiItem.keyList = new ArrayList<KeyItem>();
		} else if (localName.equals("blackMac")) {
			currentBlackItem = new BlackItem();
			currentBlackItem.isFishing = Boolean.parseBoolean(atts.getValue("isFishing"));
			currentBlackItem.macList = new ArrayList<String>();
			currentBlackItem.typeList = new ArrayList<String>();
		} else if (localName.equals("keyList")) {
		} else if (localName.equals("key")) {
			currentKeyItem = new KeyItem();
			currentKeyItem.mark = Boolean.parseBoolean(atts.getValue("mark"));
			currentKeyItem.auth = atts.getValue("auth");
		} else if (localName.equals("macList")) {
		} else if (localName.equals("typeList")) {
		}
		this.tagName = localName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (tagName != null) {
			String data = new String(ch, start, length);
			if (tagName.equals("key")) {
				currentKeyItem.key = data;
			} else if (tagName.equals("mac")) {
				if (currentWifiItem != null) {
					currentWifiItem.macList.add(data);
				} else {
					currentBlackItem.macList.add(data);
				}
			} else if (tagName.equals("type")) {
				currentBlackItem.typeList.add(data);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if (localName.equals("wifi")) {
			wifiList.add(currentWifiItem);
			currentWifiItem = null;
		} else if (localName.equals("blackMac")) {
			blackList.add(currentBlackItem);
			currentBlackItem = null;
		} else if (localName.equals("key")) {
			currentWifiItem.keyList.add(currentKeyItem);
			currentKeyItem = null;
		}

		this.tagName = null;
	}

}
