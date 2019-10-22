/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author alex
 */
public class connInstancer {
    private static final String JNDI_DS_NAME = "jdbc/major";    //see server.xml in Tomcat
    private static final String ENG_ENV = "java:comp/env";
    static final Logger m_logger = Logger.getLogger(connInstancer.class.getName());
    private static connInstancer m_ci = null;
    private DataSource m_ds = null;

    private connInstancer() {
        try {
            Locale.setDefault(Locale.UK);
            Context env = (Context) new InitialContext().lookup(ENG_ENV);
            m_ds = (DataSource) env.lookup(JNDI_DS_NAME);
        } catch (NamingException ex) {
            m_logger.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized static Connection openConnection() {
        try {
            return getInstance().m_ds.getConnection();
        } catch (SQLException e) {
            m_logger.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {con.close();} catch (java.lang.Throwable tw) {}
        }
    }

    public static boolean testConn() {
        boolean isConnected = false;
        try {
            connInstancer ci = getInstance();
            Connection cn = null;
            cn = ci.m_ds.getConnection();
            if(cn != null) {
                isConnected = true;
            }
            cn.close();
        }
        catch (SQLException e) {
            m_logger.log(Level.SEVERE, null, e);
        }
        return isConnected;
    }

    private static connInstancer getInstance() {
        if (m_ci == null) {
            m_ci = new connInstancer();
        }
        return m_ci;
    }
}
