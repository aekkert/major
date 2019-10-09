/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.tbot.Bot;
import ru.major.util.Tools;
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
        /*
        try {
            DataEng data = new DataEng();
            Map<String, String[]> params = new HashMap();
            params.put("post", new String[]{"734"});
            rs = data.getData(16, params);
            rs.getJSONObject(0).put("imguri", "http://instamajor.com/wp-content/uploads/2019/10/IMG_0109.jpg");
            InputStream image = Tools.makeImage(rs.getJSONObject(0).getString("imguri"));
            Bot b = Bot.getInstance();
            b.sendPostData(Long.parseLong("222217595"), rs.getJSONObject(0), image);
        } catch (Throwable tw){
            System.out.println(tw.getMessage());
        }
        */
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
