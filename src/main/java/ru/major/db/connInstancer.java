/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import oracle.jdbc.OracleDriver;
import org.apache.commons.dbcp2.BasicDataSource;

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
            BasicDataSource ds = (BasicDataSource)ci.m_ds;
            String cnn_url = ds.getUrl();
            String cnn_user = ds.getUsername();
            String cnn_pass = ds.getPassword();
            Connection cn = null;
            DriverManager.registerDriver(new OracleDriver());
            DriverManager.setLoginTimeout(5);
            cn = DriverManager.getConnection(cnn_url, cnn_user, cnn_pass);
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
