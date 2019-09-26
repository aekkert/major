/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.web.Action;

/**
 *
 * @author alex
 */
public class Ping extends DataEng implements Action{

    @Override
    public void perform(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        JSONArray rs = null;
        Writer out = response.getWriter();
        org.json.JSONObject o = new JSONObject();
        o.put("q", request.getParameter(MODE_PARAM));
        HttpSession ss = request.getSession();
        o.put("s", (ss!=null)?ss.getId().replaceAll("-", ""):"00");
        
        try {
            rs = getData(0, new HashMap());
            if ( rs.length() > 0 ){
                o.put("val", rs.getJSONObject(0).get("version"));
            } else {
                throw new java.lang.Throwable("No data found");
            }
            o.write(out);
            
        } catch(java.lang.Throwable tw) {
            o.put("err", 1);
            o.put("msg", tw.getMessage());
            o.write(out);
            Logger.getLogger(Ping.class.getName()).severe(tw.getMessage());
        } finally {
            closeDs();
        }
    }
    
}
