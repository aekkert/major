package ru.major.tbot;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
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
import ru.major.util.Currency;
import ru.major.util.Translator;
import ru.major.web.AlertManager;

/**
 *
 * @author alex
 */
public final class Bot extends TelegramLongPollingCommandBot {
//    private static final String BOT_NAME = "alextestbot";
    private static final String BOT_NAME = "Главный в инстаграм";
//    private static final String BOT_TOKEN = "955335597:AAFC3m5RZ65qCQMGVNr7e-huVFSP7Vg6lV0" /*ekkerttestbot*/;
    private static final String BOT_TOKEN = "978242904:AAEcLWZnu1gw2IrMGepTrxuvrmGm3qeJblU"/*InstaMajor_bot*/;
    private static final String PROXY_HOST = "103.72.153.226"; /* proxy host */
    private static final Integer PROXY_PORT = 8000;            /* proxy port */
    private static final String PROXY_USER = "QPnDHG";          /* proxy user */
    private static final String PROXY_PASSWORD = "EAu7GD";      /* proxy password */
    
    private static volatile Bot instance;
    
    public static Bot getInstance() {
        if (instance == null) {
            synchronized (Bot.class) {
                if (instance == null) {
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray());
                        }
                    });
                    ApiContextInitializer.init();
                    TelegramBotsApi telegram = new TelegramBotsApi();
                    DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
                    botOptions.setProxyHost(PROXY_HOST);
                    botOptions.setProxyPort(PROXY_PORT);
                    botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
                    
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
        register(new StatCommand());
        
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

    protected static final CacheManager CACHE_MANAGER = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("currencyCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(Date.class, JSONObject.class,ResourcePoolsBuilder.heap(100))
                .withLoaderWriter(new Currency())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(180)))
                .build()).build(true);
    
    protected static final CacheManager CACHE_MANAGER1 = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("translateCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(JSONObject.class, String.class, ResourcePoolsBuilder.heap(100))
                .withLoaderWriter(new Translator())
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(180)))
                .build()).build(true);
    
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            String res = update.getCallbackQuery().getData();
            String postID = null;
            try {
                postID = res.substring(res.indexOf("#", 0) + 1, res.indexOf("#", 1));
            } catch (Exception e) {}
            if ( res.contains("ALLOW") ) {
                try {
                    DataEng data = new DataEng();
                    Map<String, String[]> params = new HashMap();
                    params.put("post", new String[]{postID});
                    params.put("res", new String[]{"t"});
                    JSONArray rs = data.getData(30, params);
                    rs = data.getData(16, params);
                    AlertManager am = new AlertManager();
                    am.send("allow", rs.getJSONObject(0));
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
                } catch (Throwable tw) {}
            } else if ( res.contains("DENY") ) {
                try {
                    DataEng data = new DataEng();
                    Map<String, String[]> params = new HashMap();
                    params.put("post", new String[]{postID});
                    params.put("res", new String[]{"f"});
                    JSONArray rs = data.getData(30, params);
                    rs = data.getData(16, params);
                    AlertManager am = new AlertManager();
                    am.send("deny", rs.getJSONObject(0));
                    SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setText("Пост #" + postID + " отклонен. Кандидату отправлено уведомление на email.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                    }
                } catch (Throwable tw) {}
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
            } else if ( res.contains("SEXM") || res.contains("SEXF") ) {
                try {
                    String sex = "f";
                    if (res.contains("SEXM"))
                        sex = "m";
                    DataEng data = new DataEng();
                    Map<String, String[]> params = new HashMap();
                    params.put("post", new String[]{postID});
                    params.put("sex", new String[]{sex});
                    JSONArray rs = data.getData(35, params);
                    SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setText("Спасибо, модерация поста #" + postID + " завершена.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                    }                
                } catch (Throwable tw) {}
            } else if ( res.contains("PUBLISH") ) {
                try {
                    DataEng data = new DataEng();
                    Map<String, String[]> params = new HashMap();
                    params.put("post", new String[]{postID});
                    JSONArray rs = data.getData(60, params);
                    rs = data.getData(16, params);
                    AlertManager am = new AlertManager();
                    am.send("pub", rs.getJSONObject(0));
                    SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                        .setText("Пост #" + postID + " опубликован. Кандидату отправлено уведомление на email.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                    }
                } catch (Throwable tw) {}
            } else if ( res.contains("DELAY") ) {
                ForceReplyKeyboard key = new ForceReplyKeyboard();
                SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                    .setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setReplyMarkup(key)
                    .setText("На сколько часов отложить публикацию.");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                }                
            } else if ( res.contains("BACKSTAT") ) {
                statMenu(update.getCallbackQuery().getMessage().getChatId());
            } else {
                ForceReplyKeyboard key = new ForceReplyKeyboard();
                SendMessage message = new SendMessage().setChatId(update.getCallbackQuery().getMessage().getChatId())
                    .setReplyToMessageId(update.getCallbackQuery().getMessage().getMessageId())
                    .setReplyMarkup(key);

                InlineKeyboardMarkup markup = update.getCallbackQuery().getMessage().getReplyMarkup();
                for (List<InlineKeyboardButton> buttons : markup.getKeyboard()) {
                    for (InlineKeyboardButton button : buttons) {
                        if (res.equals(button.getCallbackData())) {
                            message.setText(button.getText());
                        }
                    }
                }
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
                try {
                    String res = replMsg.getText();
                    int start = res.indexOf("#", 0) + 1;
                    if ( start > 0 ) {
                        int end = res.indexOf("#", start);
                        String postID = res.substring(start, end);
                        DataEng data = new DataEng();
                        Map<String, String[]> params = new HashMap();
                        params.put("post", new String[]{postID});
                        JSONArray rs = data.getData(30, params);
                        rs = data.getData(16, params);
                        rs.getJSONObject(0).put("modenotes", msg.getText());
                        AlertManager am = new AlertManager();
                        am.send("notes", rs.getJSONObject(0));
                        SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId())
                            .setText("Ваши замечания отправлены кандидату на email. Спасибо, модерация поста #" + postID + " завершена.");
                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                        }
                    } else if (res.contains("отложить")) {
                        DataEng data = new DataEng();
                        Map<String, String[]> params = new HashMap();
                        params.put("delay", new String[]{msg.getText()});
                        JSONArray rs = data.getData(120, params);
                        String result = rs.getJSONObject(0).getString("firstdt");
                        SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId())
                                .setText("Success".equals(result) ? "Данные \"" + replMsg.getText() + "\" успешно сохранены" : "Ошибка сохренения \"" + replMsg.getText() + "\"");
                        
                    } else {
                        DataEng data = new DataEng();
                        Map<String, String[]> params = new HashMap();
                        params.put("value", new String[]{msg.getText()});
                        params.put("name", new String[]{replMsg.getText()});
                        JSONArray rs = data.getData(1004, params);
                        String result = rs.getJSONObject(0).getString("result");
                        SendMessage message = new SendMessage().setChatId(update.getMessage().getChatId())
                                .setText("Success".equals(result) ? "Данные \"" + replMsg.getText() + "\" успешно сохранены" : "Ошибка сохренения \"" + replMsg.getText() + "\"");
                        InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
                        InlineKeyboardButton                button      = null;
                        List<InlineKeyboardButton>          buttonsRow  = null;
                        List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();
                        buttonsRow = new ArrayList<>();
                        button = new InlineKeyboardButton();
                        button.setText("<-- Назад в меню статистики");
                        button.setCallbackData("BACKSTAT");
                        buttonsRow.add(button);
                        rowList.add(buttonsRow);
                        markup.setKeyboard(rowList);
                        message.setReplyMarkup(markup);
                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, e);
                        }                        
                    }
                    
                } catch (Throwable tw) {}
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
    
    public void statMenu(Long chat_id) {
        InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
        InlineKeyboardButton                button      = null;
        List<InlineKeyboardButton>          buttonsRow  = null;
        List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();

        String msg = "Ввод статистических данных трафикообразующего аккаунта @insta_major";
        try {
            DataEng data = new DataEng();
            JSONArray rs = data.getData(1003, new HashMap());
            if ( rs.length() > 0 ) {
                for ( int i = 0; i < rs.length(); i++ ) {
                    buttonsRow = new ArrayList<>();
                    button = new InlineKeyboardButton();
                    button.setText(rs.getJSONObject(i).getString("markname"));
                    button.setCallbackData(rs.getJSONObject(i).getString("markcode"));
                    buttonsRow.add(button);
                    rowList.add(buttonsRow);
                    msg = msg + "\n " + rs.getJSONObject(i).getString("markname") + ": " + rs.getJSONObject(i).getString("regvalue");
                 }
            }
            markup.setKeyboard(rowList);
        } catch (Throwable tw) {
        }

        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(msg);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendModeData(Long chat_id, JSONObject o, InputStream image) {
        String                              res         = "";
        InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
        InlineKeyboardButton                button      = null;
        List<InlineKeyboardButton>          buttonsRow  = null;
        List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Перейти в профиль кандидата @" + o.getString("login"));
        button.setUrl("https://instagram.com/" + o.getString("login"));
        buttonsRow.add(button);
        rowList.add(buttonsRow);
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Подвердить заявку кандидата @" + o.getString("login"));
        button.setCallbackData("#" + o.getString("postid") + "#ALLOW");
        buttonsRow.add(button);
        
        button = new InlineKeyboardButton();
        button.setText("Отклонить заявку кандидата @" + o.getString("login"));
        button.setCallbackData("#" + o.getString("postid") + "#DENY");
        buttonsRow.add(button);
        rowList.add(buttonsRow);
        
        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Отправить замечания кандидату @" + o.getString("login"));
        button.setCallbackData("#" + o.getString("postid") + "#NOTES");
        buttonsRow.add(button);
        rowList.add(buttonsRow);

        markup.setKeyboard(rowList);
        
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Кандидат @"  + o.getString("login") + " оплатил модерацию поста");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            res = e.getMessage();
        }
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chat_id);
        photo.setPhoto("image.png", image);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            res = res + ";" + e.getMessage();
        }
        message.setChatId(chat_id);
        message.setText("Меню модерации:");
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            res = res + ";" + e.getMessage();
        }
        if ( res == "")
            res = "Successed";
        o.put("user_email", chat_id.toString());
        o.put("result", res);
        o.put("evt", "moderation");
        putEvent(o);
    }
    
    public void sendPostData(Long chat_id, JSONObject o, InputStream image) {
        String                              res         = "";
        InlineKeyboardMarkup                markup      = new InlineKeyboardMarkup();
        InlineKeyboardButton                button      = null;
        List<InlineKeyboardButton>          buttonsRow  = null;
        List<List<InlineKeyboardButton>>    rowList     = new ArrayList<>();

        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Новый пост для публикации в " + o.getString("postdate"));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            res = e.getMessage();
        }
        
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chat_id);
        photo.setPhoto("image.png", image);
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            res = res + ";" + e.getMessage();
        }
        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(getNote(o));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            res = res + ";" + e.getMessage();
        }

        buttonsRow = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("ДА");
        button.setCallbackData("#" + o.getString("postid") + "#PUBLISH");
        buttonsRow.add(button);
        rowList.add(buttonsRow);

        button = new InlineKeyboardButton();
        button.setText("Отложить публикацию");
        button.setCallbackData("#" + o.getString("postid") + "#DELAY");
        buttonsRow.add(button);
        rowList.add(buttonsRow);

        markup.setKeyboard(rowList);
        
        message = new SendMessage();
        message.setChatId(chat_id);
        message.setText("Пост кандидата @" + o.getString("login") + " опубликован?");
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            res = res + ";" + e.getMessage();
        }
        if ( res == "")
            res = "Successed";
        o.put("user_email", chat_id.toString());
        o.put("result", res);
        o.put("evt", "manager");
        putEvent(o);
    }
    
    public void sendRecallData(Long chat_id, JSONObject o) {
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(getNote(o));
        try {
            execute(message);
        } catch (TelegramApiException e) {
        }
    }
    
    private void putEvent(JSONObject o) {
        Map<String, String[]> params = new HashMap();
        for (String key : o.keySet()) {
            params.put(key, new String[]{o.getString(key)});
        }
        try {
            DataEng data = new DataEng();
            JSONArray rs = data.getData(1001, params);
        } catch (Throwable tw) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, tw.getMessage());
        }
    }
    
    private String getNote(JSONObject o) {
        String s = null;
        if ( o.has("user_sex") ) {
            s = ("m".equals(o.getString("user_sex")))? maleNote() : femaleNote();
        } else {
            s = maleNote();
        }
        JSONObject stat = new org.json.JSONObject();
        try {
            DataEng data = new DataEng();
            Map<String, String[]> p = new HashMap();
            p.put("regdt", new String[]{o.getString("postdate")});  /// TODO. Delete
            JSONArray rs = data.getData(80, p);
            stat = rs.getJSONObject(0);
        } catch (Throwable tw) {}
        try {
            SimpleDateFormat ff = new SimpleDateFormat("dd.MM.yyyy");
            Date dt = ff.parse(ff.format(new Date()));
            Double curr = (Double) getJsDetails(dt).getJSONObject("Valute").getJSONObject("USD").get("Value");
            stat.put("treasury$", Double.toString(round(Double.parseDouble(stat.getString("treasury")) / curr, 2)));
            stat.put("storage$", Double.toString(round(Double.parseDouble(stat.getString("storage")) / curr, 2)));
            o.put("ratepay$", Double.toString(round(Double.parseDouble(o.getString("ratepay")) / curr, 2)));
            o.put("ratesum$", Double.toString(round(Double.parseDouble(o.getString("ratesum")) / curr, 2)));
            o.put("nextpay$", Double.toString(round(Double.parseDouble(o.getString("nextpay")) / curr, 2)));
        } catch (Exception e){}
        String translatedTown = getTranslate(o.getString("user_town"), "ru", "en");
        try {
            s = s.replace("{LOGIN}", o.getString("login"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{RATESUM}", o.getString("ratesum"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{RATESUM$}", o.getString("ratesum$"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{RATEPAY}", o.getString("ratepay"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{RATEPAY$}", o.getString("ratepay$"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{TREASURY}", stat.getString("treasury"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{TREASURY$}", stat.getString("treasury$"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{STORAGE}", stat.getString("storage"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{STORAGE$}", stat.getString("storage$"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{NEXTPAY}", o.getString("nextpay"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{NEXTPAY$}", o.getString("nextpay$"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{TOWN}", o.getString("user_town"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{TRANSTOWN}", translatedTown);
        } catch (java.lang.Throwable tw) {}
        return s;
    }
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private String maleNote() {
        String note = "👑 Его Величество ЦАРЬ @{LOGIN} 🤴 приветствуем 👏❤️ его вклад в Казну проекта составил\n" +
                      "= {RATESUM} руб. 💳\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "🌐 КОЛЛЕКТИВНЫЙ БЮДЖЕТ БУДУЩИХ РЕКЛАМНЫХ КАМПАНИЙ АККАУНТА (Казна проекта)\n" +
                      "= {TREASURY} руб. 💰\n" +
                      "⠀\n" +
                      "🏆 Призовой фонд победителей\n" +
                      "= {STORAGE} руб. 🗄\n" +
                      "⠀\n" +
                      "⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀☝️\n" +
                      "Хотите, чтобы эти деньги работали и для вас? Возглавьте наш трафикообразующий аккаунт, пополнив коллективный рекламный бюджет\n" +
                      "🔝 {NEXTPAY} руб.\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "👉❓ @{LOGIN} ❓👈\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👑 His Majesty the King @{LOGIN} 🤴 salute 👏❤️ his contribution to the treasury of the project amounted to\n" +
                      "= {RATESUM$} $ 💳\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "🌐 COLLECTIVE BUDGET FOR FUTURE ACCOUNT ADVERTISING CAMPAIGNS (treasury of the project)\n" +
                      "= {TREASURY$} $ 💰\n" +
                      "⠀\n" +
                      "🏆 Winners' prize fund\n" +
                      "= {STORAGE$} $ 🗄\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀☝️\n" +
                      "Do you want that money to work for you too?\n" +
                      "Head up our traffic-forming account by supplementing the collective advertising budget\n" +
                      "🔝 {NEXTPAY$} $\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "👉❓ @{LOGIN} ❓👈\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "➖➖➖➖➖➖➖➖➖➖➖\n" +
                      "#🇷🇺 #россия #russia #{TOWN} #{TRANSTOWN} #instamajor #главныйвинстаграм #царьгоры #корольтрафика #trafficking #kingoftraffic #mountainking #kingofthehill";
        return note;
    }

    private String femaleNote() {
        String note = "👑 Её Величество ЦАРИЦА @{LOGIN} 👸 приветствуем 👏❤️ её вклад в Казну проекта составил\n" +
                      "= {RATESUM} руб. 💳\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "🌐 КОЛЛЕКТИВНЫЙ БЮДЖЕТ БУДУЩИХ РЕКЛАМНЫХ КАМПАНИЙ АККАУНТА (Казна проекта)\n" +
                      "= {TREASURY} руб. 💰\n" +
                      "⠀\n" +
                      "🏆 Призовой фонд победителей\n" +
                      "= {STORAGE} руб. 🗄\n" +
                      "⠀\n" +
                      "⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀☝️\n" +
                      "Хотите, чтобы эти деньги работали и для вас? Возглавьте наш трафикообразующий аккаунт, пополнив коллективный рекламный бюджет\n" +
                      "🔝 {NEXTPAY} руб.\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "👉❓ @{LOGIN} ❓👈\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👑 Her Majesty the Queen @{LOGIN} 👸 salute 👏🏻❤️ her contribution to the treasury of the project amounted to\n" +
                      "= {RATESUM$} $ 💳\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "🌐 COLLECTIVE BUDGET FOR FUTURE ACCOUNT ADVERTISING CAMPAIGNS (treasury of the project)\n" +
                      "= {TREASURY$} $ 💰\n" +
                      "⠀\n" +
                      "🏆 Winners' prize fund\n" +
                      "= {STORAGE$} $ 🗄\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀ ⠀ ⠀ ⠀ ⠀ ⠀ ⠀☝️\n" +
                      "Do you want that money to work for you too?\n" +
                      "Head up our traffic-forming account by supplementing the collective advertising budget\n" +
                      "🔝 {NEXTPAY$} $\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "👉❓ @{LOGIN} ❓👈\n" +
                      "👉👑 @{LOGIN} 👑👈\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "⠀\n" +
                      "➖➖➖➖➖➖➖➖➖➖➖\n" +
                      "#🇷🇺 #россия #russia #{TOWN} #{TRANSTOWN} #instamajor #главныйвинстаграм #царьгоры #корольтрафика #trafficking #kingoftraffic #mountainking #kingofthehill";
        return note;
    }
    
    public JSONObject getJsDetails(Date key){
        final Cache<Date, JSONObject> currencyCache = CACHE_MANAGER.getCache("currencyCache", Date.class, JSONObject.class);
        return currencyCache.get(key);
    }

    public String getTranslate(String text, String src, String trg){
        final Cache<JSONObject, String> translateCache = CACHE_MANAGER1.getCache("translateCache", JSONObject.class, String.class);
        JSONObject key = new org.json.JSONObject();
        key.put("text", text);
        key.put("src", src);
        key.put("trg", trg);
        return translateCache.get(key);
    }

}
