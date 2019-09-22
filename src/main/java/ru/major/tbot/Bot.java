package ru.major.tbot;

import java.io.InputStream;
import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.serial.SerialBlob;
import org.json.JSONArray;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.major.db.DataEng;

/**
 *
 * @author alex
 */
public final class Bot extends TelegramLongPollingCommandBot {
    private static final String BOT_NAME = "ekkerttestbot";
    private static final String BOT_TOKEN = "961149615:AAGPGgcnUlPKWT0Ktr5mZ9WzAh_nS_0mWSs";
    private static volatile Bot instance;
    
    public static Bot getInstance() {
        if (instance == null) {
            synchronized (Bot.class) {
                if (instance == null) {
                    ApiContextInitializer.init();
                    TelegramBotsApi telegram = new TelegramBotsApi();
                    DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

                    Bot bot = new Bot(botOptions);
                    try {
                        telegram.registerBot(bot);
                    } catch (TelegramApiRequestException e) {
                        e.printStackTrace();
                    }
                    instance = bot;
                }
            }
        }
        return instance;
    }
    
    public Bot(DefaultBotOptions options) {
        super(options, BOT_NAME);
        
        
        register(new StartCommand());
        
        registerDefaultAction(((absSender, message) -> {

            SendMessage text = new SendMessage();
            text.setChatId(message.getChatId());
            text.setText(message.getText() + " command not found!");

            try {
                absSender.execute(text);
            } catch (TelegramApiException e) {
            }

        }));
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (!update.hasMessage()) {
            System.out.println(update);
        } else {
            Message msg = update.getMessage();
            User    usr = msg.getFrom();
            Contact cnt = msg.getContact();
            if ( cnt != null ) {
                String pnum = cnt.getPhoneNumber();
                DataEng data = new DataEng();
                Map<String, String[]> params = new HashMap();
                params.put("pnum", new String[]{pnum});
                params.put("chat", new String[]{msg.getChatId().toString()});
                try {
                    JSONArray rs = data.getData(1000, params);
                    Boolean res = (rs.length() > 0);
                    if ( res ) {
                        replyToUser(msg.getChatId(), "Поздравляю " + usr.getUserName() + ". Нам удалось подвердить Ваши полномочия.");
                    } else {
                        replyToUser(msg.getChatId(), "Извините " + usr.getUserName() + ". Нам не удалось подвердить Ваши полномочия. Если это техническая ошибка свяжитесь с автором проекта 1@instamajor.com");                        
                    }
                } catch (Throwable ex) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void replyToUser(Long chat_id, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void sendPostData(Long chat_id, String postData, String postTime, String image) {
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Новый пост для публикации в " + postTime);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
        
        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(postData);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chat_id);
        try {
            Blob b = null;
            b = new SerialBlob(java.util.Base64.getDecoder().decode(image));
            InputStream is = b.getBinaryStream();
            photo.setPhoto("image.png", is);
        } catch (Throwable tw){
            tw.printStackTrace();
        }
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
