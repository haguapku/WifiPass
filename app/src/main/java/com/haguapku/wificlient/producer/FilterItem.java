package com.haguapku.wificlient.producer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MarkYoung on 15/11/6.
 */
public class FilterItem {

    public static final int TYPE_UNKNOW = -1;
    public static final int TYPE_HIDE = 0;
    public static final int TYPE_MOVE_TO_KEY = 1;
    public static final int TYPE_MOVE_TO_FREE = 2;

    public int type;
    public List<Pattern> rules;

    public FilterItem(int type) {
        this.type = type;
        this.rules = new ArrayList<Pattern>();
    }

    public void addRule(String rule) {
        Pattern pattern = Pattern.compile(rule);
        rules.add(pattern);
    }

    public boolean isMatched(String ssid) {
        for (Pattern pattern : rules) {
            Matcher m = pattern.matcher(ssid);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }
}
