package ru.major.web;

import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

/**
 *
 * @author alex
 */

@ServerEndpoint("/notice")
public class WebSocketServer {
    private final SessionHandler sessionHandler = new SessionHandler();
    
    @OnOpen
    public void open(Session session) {
        
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
        Logger.getLogger(WebSocketServer.class.getName()).severe(error.getMessage());
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        if(!message.isEmpty()) {
            JSONObject msg = new JSONObject(message);
            if(msg.has("uid")) {
                sessionHandler.addSession(msg.getString("uid"), session);
            }
            
            if(msg.has("regId")) {
                sessionHandler.removeRegId(msg.getString("regId"));
                sessionHandler.addRegId(msg.getString("uid"), msg.getString("regId"));
            }
        }
    }
    
}