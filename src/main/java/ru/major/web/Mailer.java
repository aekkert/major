package ru.major.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.util.Cache;

/**
 * @author alex
 */
public class Mailer extends DataEng  {
    private Properties properties = System.getProperties();

    public Mailer() {
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
    
    public void mail(JSONObject p) {
        Cache c = Cache.getInstance();
        String from = c.getSett("fromMail");
        String pwd  = c.getSett("fromPwd");
        EAuth auth = null;
        try {
            auth = new EAuth(from, pwd);
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, "Can`t connect " + from + ": {0}", tw.getMessage());
            return;
        }
        if (p.getString("user_email") == null) {
            return;
        }
        if (properties.contains("mail.smtp.host")) {
            properties.remove("mail.smtp.host");
        }
        properties.setProperty("mail.smtp.host", c.getSett("fromSmtp"));
        //Загрузка и подстановка данных в шаблон уведомления
        String s = c.getSett("codePayTmpl");
        try {
            s = s.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{USER}", p.getString("display_name"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{CODEPAY}", p.getString("codepay"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{CODEPAYEND}", p.getString("codepayenddt"));
        } catch (java.lang.Throwable tw) {}
        
        sendTo(auth, p.getString("user_email"), from, s, "Царский сбор");

    }
    
    public void mailFail(JSONObject p) {
        Cache c = Cache.getInstance();
        String from = c.getSett("fromMail");
        String pwd  = c.getSett("fromPwd");
        EAuth auth = null;
        try {
            auth = new EAuth(from, pwd);
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, "Can`t connect " + from + ": {0}", tw.getMessage());
            return;
        }
        if (p.getString("user_email") == null) {
            return;
        }
        if (properties.contains("mail.smtp.host")) {
            properties.remove("mail.smtp.host");
        }
        properties.setProperty("mail.smtp.host", c.getSett("fromSmtp"));
        //Загрузка и подстановка данных в шаблон уведомления
        String s = c.getSett("failureTmpl");
        try {
            s = s.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{USER}", p.getString("display_name"));
        } catch (java.lang.Throwable tw) {}
       
        sendTo(auth, p.getString("user_email"), from, s, "Отказ регистрации");

    }
    
    public void mailMod(JSONObject p) throws Throwable {
        JSONArray rs = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        rs = getData(15, params);
        
        Cache c = Cache.getInstance();
        String from = c.getSett("fromMail");
        String pwd  = c.getSett("fromPwd");
        EAuth auth = null;
        try {
            auth = new EAuth(from, pwd);
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, "Can`t connect " + from + ": {0}", tw.getMessage());
            return;
        }
        if (properties.contains("mail.smtp.host")) {
            properties.remove("mail.smtp.host");
        }
        properties.setProperty("mail.smtp.host", c.getSett("fromSmtp"));
        //Загрузка и подстановка данных в шаблон уведомления
        String s = c.getSett("rquestPayTmpl");
        try {
            s = s.replace("{POSTID}", p.getString("postid"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGIN}", p.getString("login"));
        } catch (java.lang.Throwable tw) {}
        for (int i = 0 ; i < rs.length(); i++) {
          sendTo(auth, rs.getJSONObject(i).getString("email"), from, s, "Заявка на участие");
        }
    }
    
    public void mailPay(JSONObject p) throws Throwable {
        JSONArray rs = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        rs = getData(15, params);
        
        Cache c = Cache.getInstance();
        String from = c.getSett("fromMail");
        String pwd  = c.getSett("fromPwd");
        EAuth auth = null;
        try {
            auth = new EAuth(from, pwd);
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, "Can`t connect " + from + ": {0}", tw.getMessage());
            return;
        }
        if (properties.contains("mail.smtp.host")) {
            properties.remove("mail.smtp.host");
        }
        properties.setProperty("mail.smtp.host", c.getSett("fromSmtp"));
        //Загрузка и подстановка данных в шаблон уведомления
        String s = c.getSett("payTmpl");
        try {
            s = s.replace("{LASTDT}", p.getString("firstdt"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGIN}", p.getString("login"));
        } catch (java.lang.Throwable tw) {}
        for (int i = 0 ; i < rs.length(); i++) {
          sendTo(auth, rs.getJSONObject(i).getString("email"), from, s, "Оплата царского сбора");
        }
    }

    public void mailNext(JSONObject p) throws Throwable {
        JSONArray rs = new org.json.JSONArray();
        Map<String, String[]> params = new HashMap();
        rs = getData(15, params);
        
        Cache c = Cache.getInstance();
        String from = c.getSett("fromMail");
        String pwd  = c.getSett("fromPwd");
        EAuth auth = null;
        try {
            auth = new EAuth(from, pwd);
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, "Can`t connect " + from + ": {0}", tw.getMessage());
            return;
        }
        if (properties.contains("mail.smtp.host")) {
            properties.remove("mail.smtp.host");
        }
        properties.setProperty("mail.smtp.host", c.getSett("fromSmtp"));
        //Загрузка и подстановка данных в шаблон уведомления
        String s = c.getSett("nextTmpl");
        try {
            s = s.replace("{LASTDT}", p.getString("firstdt"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGO}", c.getSett("logo"));
        } catch (java.lang.Throwable tw) {}
        try {
            s = s.replace("{LOGIN}", p.getString("login"));
        } catch (java.lang.Throwable tw) {}
        for (int i = 0 ; i < rs.length(); i++) {
          sendTo(auth, rs.getJSONObject(i).getString("email"), from, s, "Следующий в очереди публикации");
        }
    }

    private boolean sendTo(EAuth auth, String to, String from, String msg, String subj) {
        boolean res = false;
        Session session = null;
        try {
            String s = msg;
            session = Session.getDefaultInstance(properties, auth);
            MimeMessage message = new MimeMessage(session);
            message.addHeader("Content-type", "text/HTML;charset=UTF-8");
            message.setSentDate(new java.util.Date());
            message.setFrom(new InternetAddress(from, "Главный в инстаграм"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subj, "UTF-8");
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
            res = true;
        } catch (java.lang.Throwable tw) {
            res = false;
            Logger.getLogger(Mailer.class.getName()).log(Level.WARNING, "Can`t send e-mail: {0}", tw.getMessage());
        }
        return res;
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
}