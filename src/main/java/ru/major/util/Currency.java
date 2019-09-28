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
import java.util.Date;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.json.JSONObject;

/**
 *
 * @author alex
 */
public class Currency implements CacheLoaderWriter<Date, JSONObject> {
    public Currency() {
    }

    private JSONObject getCurrency(Date date) {
        String url = "https://www.cbr-xml-daily.ru/daily_json.js";
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
        } catch(java.lang.Throwable tw){}
        return res;
    }

    @Override
    public JSONObject load(Date k) throws Exception {
        return getCurrency(k);
    }

    @Override
    public void write(Date k, JSONObject v) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Date k) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
