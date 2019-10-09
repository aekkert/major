/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.util;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.json.JSONObject;

/**
 *
 * @author alex
 */
public class Translator implements CacheLoaderWriter<JSONObject, String> {
    
    public Translator(){
    }
    
    private String Translate(JSONObject k) {
        try {
            Map<String, String> env = System.getenv();
            Field field;
            field = env.getClass().getDeclaredField("m");
            field.setAccessible(true);
            ((Map<String, String>) field.get(env)).put("GOOGLE_APPLICATION_CREDENTIALS", System.getProperty("java.io.tmpdir") + File.separator + "service-account-file.json");
        } catch (Exception ex) {
            Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Instantiates a client
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        // The text to translate
        String text = k.getString("text");

        // Translates some text into Russian
        Translation translation =
            translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(k.getString("src")),
                Translate.TranslateOption.targetLanguage(k.getString("trg")));
        return translation.getTranslatedText();
    }
    
    @Override
    public String load(JSONObject k) throws Exception {
        return Translate(k);
    }

    @Override
    public void write(JSONObject k, String v) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(JSONObject k) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
