/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.tbot;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.json.JSONArray;
import ru.major.db.DataEng;
import ru.major.util.Tools;
/**
 *
 * @author alex
 */
public class BotInitializer implements ServletContextListener {
    
    private  ScheduledExecutorService service = null;
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        SimpleDateFormat ff = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Bot b = Bot.getInstance();
                
        service = Executors.newSingleThreadScheduledExecutor();
        
        service.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                
                DataEng data = new DataEng();
                try {
                    JSONArray rs = data.getData(1002, new HashMap());
                    if ( rs.length() > 0 ) {
                        Date fd = ff.parse(rs.getJSONObject(0).getString("firstdt"));
                        long diff = getDateDiff(fd, TimeUnit.SECONDS);
                        if ( diff < 30 ) {
                            Map<String, String[]> params = new HashMap();
                            params.put("post", new String[]{rs.getJSONObject(0).getString("postid")});
                            rs = data.getData(16, params);
                            JSONArray mngrs = data.getData(14, params);
                            for (int i = 0; i < mngrs.length(); i++) {
                                InputStream is = Tools.makeImage(rs.getJSONObject(0).getString("imguri"));
                                b.sendPostData(Long.parseLong(mngrs.getJSONObject(i).getString("chatid")), rs.getJSONObject(0), is);
                                is.close();
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Logger.getLogger(BotInitializer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);

        /*
        ApiContextInitializer.init();
        TelegramBotsApi telegram = new TelegramBotsApi();
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        Bot bot = new Bot(botOptions);
        try {
            telegram.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
        */

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (service != null){
            service.shutdownNow();
        }
    }
    
    private static long getDateDiff(Date date1, TimeUnit timeUnit) {
        long diffInMillies = date1.getTime() - new Date().getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
   
}
