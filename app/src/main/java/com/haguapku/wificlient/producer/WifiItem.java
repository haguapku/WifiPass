package com.haguapku.wificlient.producer;

import java.util.List;

/**
 * Created by MarkYoung on 15/10/21.
 */
public class WifiItem extends ItemBase {

    public String id;
    public String name;
    public String type;
    public boolean isNeedLogin;
    public boolean check;
    public List<KeyItem> keyList;
    public List<String> macList;


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "  name："+name+"  id："+id;
    }

}
