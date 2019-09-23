package ru.major.tbot;

import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
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
        if (update.hasCallbackQuery()) {
            String res = update.getCallbackQuery().getData();
            String postID = res.substring(res.indexOf("#", 0) + 1, res.indexOf("#", 1));
            if ( res.contains("ALLOW") ) {
                SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                    .setText("Пост #" + postID + " подтвержден. Кандидату отправлено уведомление на email. Теперь необходимо установить пол кандидата.");
                InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
                InlineKeyboardButton                button      = null;
                List<InlineKeyboardButton>          buttonsRow  = null;
                List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();

                buttonsRow = new ArrayList<>();
                button = new InlineKeyboardButton();
                button.setText("Пол мужской");
                button.setCallbackData("#" + postID + "#SEXM");
                buttonsRow.add(button);

                button = new InlineKeyboardButton();
                button.setText("Пол женский");
                button.setCallbackData("#" + postID + "#SEXF");
                buttonsRow.add(button);
                rowList.add(buttonsRow);
                
                markup.setKeyboard(rowList);
                message.setReplyMarkup(markup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                }
            } else if ( res.contains("DENY") ) {
                SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                    .setText("Пост #" + postID + " отклонен. Кандидату отправлено уведомление на email.");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                }
            } else if ( res.contains("NOTES") ) {
                ForceReplyKeyboard key = new ForceReplyKeyboard();
                SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                    .setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setReplyMarkup(key)
                    .setText("Напишите в ответе на это сообщение ваши замечания и мы немедленно отправим их кандитату.#" + postID + "#")
                    .setParseMode("HTML");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                }                
            }
        } else if (update.hasMessage()) {
            Message msg = update.getMessage();
            User    usr = msg.getFrom();
            Contact cnt = msg.getContact();
            Message replMsg = msg.getReplyToMessage();
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
            } else if ( replMsg != null ) {
                String res = replMsg.getText();
                int start = res.indexOf("#", 0) + 1;
                int end = res.indexOf("#", start);
                String postID = res.substring(start, end);
                System.out.println(postID + " !!!! " + msg.getText());
            }
        }
    }

    private void replyToUser(Long chat_id, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(messageText);
        message.setReplyMarkup(new ReplyKeyboardRemove());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public void sendModeData(Long chat_id, String postID, String postData, String image) {
        InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
        InlineKeyboardButton                button      = null;
        List<InlineKeyboardButton>          buttonsRow  = null;
        List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Перейти в профиль кандидата @igor.lix");
        button.setUrl("https://instagram.com/igor.lix");
        buttonsRow.add(button);
        rowList.add(buttonsRow);
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Подвердить заявку кандидата @igor.lix");
        button.setCallbackData("#" + postID + "#ALLOW");
        buttonsRow.add(button);
        
        button = new InlineKeyboardButton();
        button.setText("Отклонить заявку кандидата @igor.lix");
        button.setCallbackData("#" + postID + "#DENY");
        buttonsRow.add(button);
        rowList.add(buttonsRow);
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Отправить замечания кандидату @igor.lix");
        button.setCallbackData("#" + postID + "#NOTES");
        buttonsRow.add(button);
        rowList.add(buttonsRow);

        markup.setKeyboard(rowList);
        
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Кандидат <a href=\"https://instagram.com/igor.lix\">igor.lix</a> оплатил модерацию поста");
        message.setParseMode("HTML");
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

        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(postData);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
        
        message.setChatId(chat_id);
        message.setText("Меню модерации:");
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public void sendPostData(Long chat_id, String postData, String postTime, String image, String note) {
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Новый пост для публикации в " + postTime);
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

        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(postData);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }

        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(note);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
