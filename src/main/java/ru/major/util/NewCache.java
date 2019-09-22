/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.json.JSONObject;

/**
 *
 * @author alex
 */
public class NewCache implements CacheLoaderWriter<String, JSONObject> {

    public NewCache(){
    }
    
    private JSONObject getInstaPub(String login) {
        String url = "https://instagram.com/" + login + "/?__a=1";
        JSONObject res = new org.json.JSONObject();
        
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String readAPIResponse = " ";
            StringBuilder jsonString = new StringBuilder();
            while((readAPIResponse = in.readLine()) != null){
                jsonString.append(readAPIResponse);
            }
            res = new JSONObject(jsonString.toString());
        } catch(java.lang.Throwable tw){
        }
        return res;
    }
    
    @Override
    public JSONObject load(String key) throws Exception {
        return getInstaPub(key);
    }

    @Override
    public void write(String k, JSONObject v) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String k) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
