    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.web;

/**
 *
 * @author alex
 */
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ru.major.objects.Ping;
import ru.major.objects.Task;
import ru.major.objects.Settings;

public class ActionFactory {

    protected static volatile  ActionFactory singleton;
    protected Map<String, Class> map;

    public static  ActionFactory getSingleton() {
        if (singleton == null) {
            synchronized (ActionFactory.class) {
                if (singleton == null) {
                    singleton = new ActionFactory();
                }
            }
        }
        return singleton;
    }

    public ActionFactory() {
        map = Collections.unmodifiableMap( defaultMap() );
    }


    public Action create(String actionName) {
        Class klass = (Class) map.get(actionName);
        if (klass == null) {
            throw new RuntimeException(getClass() + " was unable to find an action named '" + actionName + "'.");
        }

        Action actionInstance = null;
        try {
            actionInstance = (Action) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionInstance;
    }

    protected Map defaultMap() {
        Map<String, Class> mapActions = new HashMap();
        mapActions.put("ping", Ping.class);
        mapActions.put("task", Task.class);
        mapActions.put("settings", Settings.class);
        return mapActions;
    }
}

