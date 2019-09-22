/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.objects;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import ru.major.db.DataEng;
import ru.major.util.Cache;
import ru.major.util.Tools;
import ru.major.web.Action;

/**
 *
 * @author alex
 */
public class Settings extends DataEng implements Action {
    
    Writer     out       = null;
    JSONObject root      = new org.json.JSONObject();
    String     sid       = null;

    @Override
    public void perform(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer res = 0;
        out = response.getWriter();
        res = getSettings(request.getParameter("sett"));
                
        switch (res) {
            case 0:
                response.setStatus(404);
                root.put("err", "Данные не найдены");
                root.write(out);
                break;
            case 1:
                root.write(out);
                break;
        }
        Tools.close(out);
    }
    
    private int getSettings(String name) throws ServletException, IOException {
        Integer res  = 0;
        String  sett = null;
        Cache c = Cache.getInstance();
        sett = c.getSett(name);
        
        if (sett != null) {
            root.put("setting", sett);
            res++;
        }
        return res;
    }
}
