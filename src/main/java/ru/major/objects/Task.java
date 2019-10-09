package ru.major.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.util.Tools;
import ru.major.web.Action;
import ru.major.web.Mailer;
import ru.major.web.SessionHandler;
import java.net.*;
import java.time.Duration;
import java.util.HashMap;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import ru.major.tbot.Bot;
import ru.major.util.NewCache;
import ru.major.web.AlertManager;
/**
 *
 * @author alex
 */
public class Task extends DataEng implements Action {


    public static final SessionHandler sh = new SessionHandler();
    
    protected static final CacheManager CACHE_MANAGER = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("jsCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, JSONObject.class,ResourcePoolsBuilder.heap(100))
                .withLoaderWriter(new NewCache())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(30)))
                .build()).build(true);

    @Override
    public void perform(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject root = new org.json.JSONObject();
        JSONArray items = new org.json.JSONArray();
        
        String Mode = request.getParameter("q");
        
        Integer res = 0;
        if ( Mode != null ) {
            switch ( Mode ) {
                case "reg":
                    items = workReg(request);
                    break;
                case "new":
                    items = workNew(request);
                    break;
                case "conf":
                    items = workMod(request);
                    break;
                case "getpay":
                    items = workTask(request, 40);
                    break;
                case "setpay":
                    items = workPay(request);
                    break;
                case "pub":
                    items = workPub(request);
                    break;
                case "top":
                    items = workTop(request);
                    break;
                case "stat":
                    items = workStat(request);
                    break;
                case "queue":
                    items = workTask(request, 90);
                    break;
                case "inst":
                    items = workInsta(request);
                    break;                    
                case "post":
                    items = workTask(request, 100);
                    break;
                case "resetpass":
                    items = workReset(request);
                    break;
                case "recall":
                    items = workRecall(request);
                    break;
                case "tab":
                    items = workTask(request, 110);
                    break;
            }
        }
        res = items.length();
        Writer out = response.getWriter();
        if (res == 0) {
            response.setStatus(404);
            root.put("err", "Данные не найдены");
            root.write(out);
        } else if (res > 0) {
            items.write(out);
        }
        Tools.close(out);
    }
    
    private JSONArray getPost(HttpServletRequest request) {
        return workTask(request, 16);
    }
    
    private JSONArray workReg(HttpServletRequest request) {
        JSONArray rs = new org.json.JSONArray();
        try {
            rs = getPost(request);
        } catch (Throwable tw) {}
        if ( rs.length() > 0 ) {
            AlertManager am = new AlertManager();
            am.send("reg", rs.getJSONObject(0));
        }
        return rs;
    }
    private JSONArray workNew(HttpServletRequest request) {
        JSONArray rs = new org.json.JSONArray();
        try {
            rs = workTask(request, 20);
            rs = getPost(request);
        } catch (Throwable tw){}
        if ( rs.length() > 0 ) {
            AlertManager am = new AlertManager();
            am.send("new", rs.getJSONObject(0));
            am.sendModeration(rs.getJSONObject(0));
        }
        return rs;        
    }

    private JSONArray workTask(HttpServletRequest request, int pMode) {
        JSONArray rs = null;
        Map<String, String[]> params = request.getParameterMap();
        try {
            rs = getData(pMode, params);
        } catch (Throwable ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    private JSONArray workMod(HttpServletRequest request) {
        JSONArray rs = null;
        rs = workTask(request, 30);
        if (rs.getJSONObject(0).has("codepay")) {
            Mailer m = new Mailer();
            m.mail(rs.getJSONObject(0));
        } else {
            Mailer m = new Mailer();
            m.mailFail(rs.getJSONObject(0));
        }
            
        return rs;
    }
    
    private JSONArray workTop(HttpServletRequest request) throws MalformedURLException, IOException {
        JSONArray rs = null;
        rs = workTask(request, 70);
        for (int i = 0 ; i < rs.length(); i++) {
            JSONObject instaUser = getJsDetails(rs.getJSONObject(i).getString("user_login"));
            rs.getJSONObject(i).put("img", instaUser.getJSONObject("graphql").getJSONObject("user").getString("profile_pic_url"));
        }
        return rs;        
    }
    
    private JSONArray workStat(HttpServletRequest request) {
        JSONArray rs = new org.json.JSONArray();
        try {
            rs = workTask(request, 80);
            if ( rs.length() > 0 ) {
                JSONArray inst = workTask(request, 1003);
                rs.getJSONObject(0).put("insta", inst);
            }
        } catch (Throwable tw){}
        return rs;
    }
    private JSONArray workPay(HttpServletRequest request) {
        JSONArray rs = null;
        try {
            rs = workTask(request, 50);
            rs = getPost(request);
        } catch (Throwable tw) {}
        
        if ( rs.length() > 0 ) {
            AlertManager am = new AlertManager();
            am.send("setpay", rs.getJSONObject(0));
//            am.sendModeration(rs.getJSONObject(0));
        }

        return rs;
    }

    private JSONArray workPub(HttpServletRequest request) {
        JSONArray rs = null;
        try {
            rs = workTask(request, 60);
            rs = getPost(request);
        } catch (Throwable tw) {}
        if (rs.length() > 0) {
            AlertManager am = new AlertManager();
            am.send("pub", rs.getJSONObject(0));
            am.sendModeration(rs.getJSONObject(0));
        }
            
        return rs;
    }
    
    private JSONArray workReset(HttpServletRequest request) {
        JSONArray rs = new org.json.JSONArray();
        JSONObject o = new org.json.JSONObject();
        for (Map.Entry entry : request.getParameterMap().entrySet()) {
            String key = (String) entry.getKey();
            String[] v = (String [])entry.getValue();
            for (int i = 0, n = v.length; i < n; i++) {
                o.put(key, v[i]);
            }
        }
        AlertManager am = new AlertManager();
        am.send("resetpass", o);
        rs.put(o);
        return rs;
    }
    
    private JSONArray workInsta(HttpServletRequest request) throws MalformedURLException, IOException {
        JSONArray  rs = new org.json.JSONArray();
        String     user = request.getParameter("u");
        JSONObject instaUser = getJsDetails(user);
        JSONObject res = new org.json.JSONObject();
        res.put("login", user);
        res.put("img", instaUser.getJSONObject("graphql").getJSONObject("user").getString("profile_pic_url"));
        rs.put(res);
        return rs;        
    }
    
    public JSONObject getJsDetails(String key){
        final Cache<String, JSONObject> jsCache = CACHE_MANAGER.getCache("jsCache", String.class, JSONObject.class);
        return jsCache.get(key);
    }
    
    private JSONArray workRecall(HttpServletRequest request) {
        JSONArray rs  = new org.json.JSONArray();
        JSONArray res = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        try {
            rs = workTask(request, 1005);
            if (rs.length() > 0) {
                JSONArray r = getData(1007, params);
                for (int i = 0; i < rs.length(); i++) {
                    params.clear();
                    params.put("id", new String[]{rs.getJSONObject(i).getString("id")});
                    params.put("postid", new String[]{rs.getJSONObject(i).getString("postid")});
                    params.put("regdt", new String[]{rs.getJSONObject(i).getString("regdt")});
                    params.put("opersum", new String[]{rs.getJSONObject(i).getString("opersum")});
                    params.put("dkflag", new String[]{rs.getJSONObject(i).getString("dkflag")});
                    params.put("note", new String[]{rs.getJSONObject(i).getString("note")});
                    params.put("linkid", new String[]{rs.getJSONObject(i).getString("linkid")});
                    r = getData(1006, params);
                    params.clear();
                    params.put("post", new String[]{rs.getJSONObject(i).getString("postid")});
                    r = getData(16, params);
                    Integer p = Integer.parseInt(r.getJSONObject(0).getString("ratesum")) + 1;
                    r.getJSONObject(0).put("nextpay", p.toString());
                    Bot b = Bot.getInstance();
                    b.sendRecallData(new Long(222217595), r.getJSONObject(0));
                    res.put(r);
                }
            }
        } catch (Throwable tw) {
            
        }
        return res;
    }
}
