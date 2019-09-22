package ru.major.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
import org.json.JSONObject;

/**
 *
 * @author alex
 */
public class SessionHandler {
    private static final String FCM_ADDRESS = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_AUTHORIZATION = "key=AAAALwhQYUs:APA91bHVd-SiHpRAEjSHWuBsEMdqZgI96_vJkinEUxvehQzrdgw9DZj1vDlGe7ixVKdSG7GXCFzzQi2t07dH-pySiaqm29YifkLHcugmWnbyjfWEmKIgsQKBapHq5K1jhPdoWRNulbhHoMeywTD9n33DFDGoMdQ6ug";
    
    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> regIds = new ConcurrentHashMap<>();
    
    public void addSession(String uid, Session s) {
        if(!sessions.containsKey(uid)) {
            sessions.put(uid, new HashSet<Session>());
        }
        sessions.get(uid).add(s);
    }
    
    public void removeSession(Session s) {
        Set<Map.Entry<String, Set<Session>>> entrySet = sessions.entrySet();
        for(Map.Entry<String, Set<Session>> pair : entrySet) {
            Set<Session> ss = pair.getValue();
            if(ss.contains(s)) {
                sessions.get(pair.getKey()).remove(s);
                if(sessions.get(pair.getKey()).isEmpty()) {
                    sessions.remove(pair.getKey());
                }
                break;
            }
        }
    }
    
    public void sendMessageToUsers(String[] users, String msg) {
        for(String uid : users) {
            if(sessions.get(uid) != null) {
                for(Session s: sessions.get(uid)) {
                    sendMessageToSession(s, msg);
                }
            } else {
                //TODO добавить сообщение в очередь
            }
        }
    }
    
    public void sendMessageToAll(String msg) {
        for(Set<Session> ss: sessions.values()) {
            for(Session s: ss) {
                sendMessageToSession(s, msg);
            }
        }
    }
    
    public void sendMessageToSession(Session s, String msg) {
        try {
            s.getBasicRemote().sendText(msg);
        } catch (IOException ex) {
            removeSession(s);
        }
    }
    
    public void addRegId(String uid, String s) {
        if(!regIds.containsKey(uid)) {
            regIds.put(uid, new HashSet<String>());
        }
        regIds.get(uid).add(s);
    }
    
    public void removeRegId(String s) {
        Set<Map.Entry<String, Set<String>>> entrySet = regIds.entrySet();
        for(Map.Entry<String, Set<String>> pair : entrySet) {
            Set<String> ss = pair.getValue();
            if(ss.contains(s)) {
                regIds.get(pair.getKey()).remove(s);
                if(regIds.get(pair.getKey()).isEmpty()) {
                    regIds.remove(pair.getKey());
                }
                break;
            }
        }
    }
    
    public void sendPushNotificationToUsers(String[] users, String title, String msg) {
        for(String uid : users) {
            if(regIds.get(uid) != null) {
                for(String s: regIds.get(uid)) {
                    sendPushNotification(s, title, msg);
                }
            }
        }
    }
    
    public void sendPushNotificationToAll(String title, String msg) {
        for(Set<String> ss: regIds.values()) {
            for(String s: ss) {
                sendPushNotification(s, title, msg);
            }
        }
    }
    
    public void sendPushNotification(String regId, String title, String msg) {
        try {           
            URLConnection connection = new URL(FCM_ADDRESS).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", FCM_AUTHORIZATION);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            
            JSONObject obj = new JSONObject();
            JSONObject data = new JSONObject();
            
            data.put("title", title);
            data.put("body", msg);
            data.put("sound", "default");
            data.put("icon", "icon");
            data.put("color", "#efefef");
            
            obj.put("to", regId);
            obj.put("notification", data);
            obj.put("priority", "HIGH");
            out.write(obj.toString());
            out.flush();
            
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = in.readLine()) != null) {
                Logger.getLogger(SessionHandler.class.getName()).log(Level.WARNING, line);
//               TODO Добавить обработку ответа, если проблема с регистрацией, то удалять regId (варианты ответов ниже)
//                {"multicast_id":6167045854422111927,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"InvalidRegistration"}]}
//                {"multicast_id":5791892615128740428,"success":1,"failure":0,"canonical_ids":0,"results":[{"message_id":"0:1467793561981964%c5046a74c5046a74"}]}
            }
            out.close();
            in.close();
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
