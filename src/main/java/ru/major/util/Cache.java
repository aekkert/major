/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import ru.major.db.DataEng;

/**
 *
 * @author alex
 */
public final class Cache extends DataEng {
    private static volatile Cache instance;
    private static JSONArray Settings;
    
    public Cache() {
        Settings = new org.json.JSONArray();
    }
    
    public static Cache getInstance() {
        if (instance == null) {
            synchronized (Cache.class) {
                if (instance == null) {
                    instance = new Cache();
                }
            }
        }
        return instance;
    }
    
    public String getSett(String param) {
        String res = null;
        
        if ( Settings.length() == 0 )
            getSettings();
        
        for (int i = 0; i < Settings.length(); i++) {
            if ( param.equals(Settings.getJSONObject(i).getString("settcode")) ) {
                res = Settings.getJSONObject(i).getString("settvalue");
                break;
            }
        }
        return res;
    }
    
    public void refresh(String obj) {
        if (obj==null) {
            getSettings();
        } else if ("Settings".equals(obj)) {
            getSettings();
        }
    }
    
    private void getSettings() {
        JSONArray             rs     = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        try {
            rs = getData(10, params);
        } catch (Throwable ex) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
        }
        Settings = rs;
    }

}
