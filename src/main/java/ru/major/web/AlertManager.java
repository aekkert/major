/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.web;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.util.Cache;
import ru.major.util.Tools;
import ru.major.tbot.Bot;
/**
 *
 * @author alex
 */
public class AlertManager {
    private Properties properties = System.getProperties();

    public AlertManager() {
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.store.protocol", "pop3");
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.mime.charset", "utf-8");

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.ssl.enable", "true");

        final int timeout = (int) TimeUnit.SECONDS.toMillis(10);
        properties.put("mail.smtp.connectiontimeout", timeout);
        properties.put("mail.smtp.timeout", timeout);
    }
    
    private class EAuth extends Authenticator {

        private String user;
        private String password;

        EAuth(String user, String password) {
            this.user = user;
            this.password = password;
        }
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            String user = this.user;
            String password = this.password;
            return new PasswordAuthentication(user, password);
        }
    }
    
    private class Alert {

        private String alertMailFrom;
        private String alertMailFromName;
        private String alertMailTo;
        private String alertMailSubj;
        private String alertMailSmtp;
        private String alertMailPwd;
        private String alertMailTmpl;
        private String alertMailImg;
    }
    
    public void send(String what, JSONObject o) {
        String res = null;
        Cache c = Cache.getInstance();
        Alert a = new Alert();
        switch ( what ) {
            case "reg":
                a.alertMailFrom = c.getSett("mailModeration");
                a.alertMailSubj = "Регистрация";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert1");
                break;
            case "new":
                a.alertMailFrom = c.getSett("mailModeration");
                a.alertMailSubj = "Модерация";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert2");
                break;
            case "newerr":
                a.alertMailFrom = c.getSett("mailModeration");
                a.alertMailSubj = "Модерация";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert3");
                break;
            case "allow":
                a.alertMailFrom = c.getSett("mailCrowdfunding");
                a.alertMailSubj = "Краудфандинг";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert4");
                break;
            case "notes":
                a.alertMailFrom = c.getSett("mailModeration");
                a.alertMailSubj = "Модерация";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert5");
                break;
            case "deny":
                a.alertMailFrom = c.getSett("mailModeration");
                a.alertMailSubj = "Модерация";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert6");
                break;
            case "setpay":
                a.alertMailFrom = c.getSett("mailCrowdfunding");
                a.alertMailSubj = "Краудфандинг";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert7");
                break;
            case "pub":
                a.alertMailFrom = c.getSett("mailInfo");
                a.alertMailSubj = "Инфо";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert8");
                break;
            case "payerr":
                a.alertMailFrom = c.getSett("mailCrowdfunding");
                a.alertMailSubj = "Краудфандинг";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert10");
                break;
            case "payerr2":
                a.alertMailFrom = c.getSett("mailCrowdfunding");
                a.alertMailSubj = "Краудфандинг";
                a.alertMailSmtp = c.getSett("mailSmtp");
                a.alertMailTo = o.getString("user_email");
                a.alertMailPwd = c.getSett("mailPwd");
                a.alertMailTmpl = c.getSett("Alert9");
                break;
        }
        a.alertMailFromName = "Главный в интаграм";
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{REQUESTPAY}", c.getSett("requestPay"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{POSTID}", o.getString("postid"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{CODEPAYLONG}", c.getSett("codePayLong"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{SUPPORT}", c.getSett("mailSupport"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{CODEPAY}", o.getString("codepay"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{UUID}", UUID.randomUUID().toString());
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{MODERATIONNOTES}", o.getString("modenotes"));
        } catch (java.lang.Throwable tw) {}
        try {
            a.alertMailTmpl = a.alertMailTmpl.replace("{LOGIN}", o.getString("login"));
        } catch (java.lang.Throwable tw) {}
        res = sendMail(a);
        o.put("result", res);
        o.put("evt", what);
        putEvent(o);
    }
    
    public void sendModeration(JSONObject o) {
        DataEng data = new DataEng();
        JSONArray rs = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        try {
            rs = data.getData(15, params);
        } catch (Throwable tw) {
        }
        if ( o.length() > 0 ) {
            Cache c = Cache.getInstance();
            for (int j = 0 ; j < rs.length(); j++) {
                Alert a = new Alert();
                a.alertMailTmpl = c.getSett("AlertModeration");
                try {
                    a.alertMailTmpl = a.alertMailTmpl.replace("{LOGO}", c.getSett("logo"));
                } catch (java.lang.Throwable tw) {}
                try {
                    a.alertMailTmpl = a.alertMailTmpl.replace("{LOGIN}", o.getString("login"));
                } catch (java.lang.Throwable tw) {}
                try {
                    a.alertMailTmpl = a.alertMailTmpl.replace("{POSTURI}", o.getString("postid"));
                } catch (java.lang.Throwable tw) {}
                try {
                    BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("border.jpg"));
                    a.alertMailImg = Tools.makeImage(image, o.getString("imguri"));
                    a.alertMailTmpl = a.alertMailTmpl.replace("{POSTIMG}", a.alertMailImg);
                } catch (java.lang.Throwable tw) {}
                try {
                    a.alertMailTmpl = a.alertMailTmpl.replace("{POSTCONTENT}", o.getString("post_content"));
                } catch (java.lang.Throwable tw) {}
                try {
                    a.alertMailTmpl = a.alertMailTmpl.replace("{POSTID}", o.getString("postid"));
                } catch (java.lang.Throwable tw) {}
                if ( rs.length() > 0 ) {
                    a.alertMailFrom = c.getSett("mailModeration");
                    a.alertMailFromName = "Главный в интаграм";
                    a.alertMailSubj = "Модерация";
                    a.alertMailSmtp = c.getSett("mailSmtp");
                    a.alertMailPwd = c.getSett("mailPwd");

                    for (int i = 0 ; i < rs.length(); i++) {
                        a.alertMailTo = rs.getJSONObject(0).getString("email");
                        String res = sendMail(a);
                        o.put("user_email", a.alertMailTo);
                        o.put("result", res);
                        o.put("evt", "moderation");
                        putEvent(o);
                        Bot b = Bot.getInstance();
                        b.sendModeData(Long.parseLong(rs.getJSONObject(0).getString("chatid")), o, a.alertMailImg);
                    }
                }
            }
        }
    }
    
    private String sendMail(Alert alert) {
        String res     = null;
        try {
            if (properties.contains("mail.smtp.host")) {
                properties.remove("mail.smtp.host");
            }
            properties.setProperty("mail.smtp.host", alert.alertMailSmtp);
            
            EAuth auth = new EAuth(alert.alertMailFrom, alert.alertMailPwd);

            String s = alert.alertMailTmpl;
            Session session = Session.getInstance(properties, auth);
            MimeMessage message = new MimeMessage(session);
            message.addHeader("Content-type", "text/HTML;charset=UTF-8");
            message.setSentDate(new java.util.Date());
            message.setFrom(new InternetAddress(alert.alertMailFrom, alert.alertMailFromName));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(alert.alertMailTo));
            message.setSubject(alert.alertMailSubj, "UTF-8");
            Multipart mp = new MimeMultipart();
            BodyPart tbp = new MimeBodyPart();
            tbp.setContent(s, "text/html; charset=UTF-8");
            mp.addBodyPart(tbp);
            message.setContent(mp);
            message.saveChanges();
            Transport t;
            if ("true".equals(properties.getProperty("mail.smtp.ssl.enable", "false"))) {
                t = session.getTransport("smtps");
            } else {
                t = session.getTransport("smtp");
            }
            t.connect(properties.getProperty("mail.smtp.host"), auth.user, auth.password);
            t.send(message);
            t.close();
            res = "Successed";
        } catch (java.lang.Throwable tw) {
            res = tw.getMessage();
        }
        return res;
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
            Logger.getLogger(AlertManager.class.getName()).log(Level.SEVERE, tw.getMessage());
        }
    }
}
