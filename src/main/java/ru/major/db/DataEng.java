package ru.major.db;

import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.major.util.Tools;

/**
 * Родительский класс для организации jdbc-подключений к СУБД
 */
public class DataEng {

    public static final Logger log = Logger.getLogger(DataEng.class.getName());
    public static final int MODE_READ = 0;
    public static final int MODE_WRITE = 1;
    public static final int MODE_DELETE = 2;
    private static String DB_NAME = "";                     //one is KNOW_DB_xxx
    private boolean m_fConnStored = false;
    protected Connection m_cnn = null;
    protected String m_last_err = null;
    protected int m_mode = MODE_READ;
    protected CallableStatement m_stm = null;
    protected PreparedStatement p_stm = null;
    protected ResultSet m_rs = null;
    
    public DataEng() {
    }

    private synchronized void chkDbMeta() {
        if (DB_NAME.length() > 1) {
            return;
        }
        try {
            DatabaseMetaData dmd = m_cnn.getMetaData();
            DB_NAME = dmd.getDatabaseProductName().toUpperCase();
        } catch (java.lang.Throwable tw) {
            log.log(Level.SEVERE, "chkDbMeta", tw);
        }
    }

    protected boolean openDs() {
        boolean res = m_fConnStored || isOpen();
        if (!res) {
            try {
                m_cnn = connInstancer.openConnection();
                m_cnn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                m_cnn.setAutoCommit(false);
                chkDbMeta();
                res = true;
                CallableStatement stm = m_cnn.prepareCall(ru.major.db.SQL.data.SQL_INF, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                stm.registerOutParameter(1, Types.OTHER);
                stm.setInt(2, 1000);
                stm.setNull(3, java.sql.Types.VARCHAR);
                stm.execute();
                ResultSet rs = (ResultSet)stm.getObject(3);
                rollback();
            } catch (java.lang.Throwable tw) {
                m_last_err = tw.getMessage();
                Logger.getLogger(DataEng.class.getName()).severe(m_last_err);
            }
        }
        return res;
    }

    public boolean openDs(Connection cnn) {
        m_cnn = cnn;
        if (isOpen()) {
            m_fConnStored = true;
        }
        return openDs();
    }

    protected void setConnection(Connection cnn) {
        m_cnn = cnn;
        m_fConnStored = isOpen();
    }

    public Connection getConnection() {
        return m_cnn;
    }

    protected void closeDs() {
        Tools.close(m_rs);
        Tools.close(m_stm);
        if (m_fConnStored) {
            return;
        }
        if (m_cnn != null) {
            try {
                rollback();
                connInstancer.closeConnection(m_cnn);
            } catch (java.lang.Throwable tw) {
                log.log(Level.SEVERE, "closeDs", tw);
            } finally {
                m_cnn = null;
            }
        }
    }

    public boolean isOpen() {
        boolean res = (m_cnn != null);
        if (res) {
            try {
                res = !m_cnn.isClosed();
            } catch (java.lang.Throwable tw) {
                log.log(Level.SEVERE, "isOpen", tw);
                res = false;
            }
        }
        return res;
    }

    public int getMode() {
        return m_mode;
    }

    /**
     * Установка режима модификаций
     * @param newMode одно из значений MODE_xxx
     * @see DataEng.MODE_xxx
     *
     */
    public void setMode(int newMode) {
        m_mode = newMode;
    }

    public Statement getStatement() throws SQLException {
        if (isOpen()) {
            return m_cnn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            return null;
        }
    }

    public String readCLob(Clob c) {
        String s = "";
        if (c == null) {
            return null;
        }
        Reader in_stm = null;
        try {
            in_stm = c.getCharacterStream();
            int length = (int) c.length();
            char[] buff = new char[length];
            in_stm.read(buff);
            s = String.valueOf(buff);
        } catch (java.lang.Throwable ex) {
            m_last_err = ex.getMessage();
        } finally {
            Tools.close(in_stm);
        }
        return s;
    }          //readCLob

    public void Commit() throws SQLException {
        if (m_cnn != null) {
            m_cnn.commit();
        }
    }

    public void rollback() {
        rollback(false);
    }

    public void rollback(boolean fClose) {
        if (m_cnn != null) {
            try {
                m_cnn.rollback();
                if (fClose) {
                    closeDs();
                }
            } catch (java.lang.Throwable tw) {
                log.log(Level.SEVERE, "rollback", tw);
            }
        }
    }

    public String getLastErr() {
        return (m_last_err == null) ? "" : m_last_err.replaceAll("\"", "").replaceAll("\'", "");
    }

    @Override
    protected void finalize() throws Throwable {
        closeDs();        // close open connection
        super.finalize();
    }

    public static byte[] getBytes(final String s) {
        if ((s == null) || (s.length() < 1)) {
            return null;
        } else {
            int length = s.length() / 2;
            byte[] raw = new byte[length];
            for (int i = 0; i < length; ++i) {
                raw[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
            }
            return raw;
        }
    }

    public static boolean isId(final String s) {
        boolean res = (s != null);
        if (res) {
            res = (s.length() > 0) && (!"00".equals(s)) && (!"null".equalsIgnoreCase(s));
        }
        return res;
    }

    public void putParams(Map<String, String[]> p, String sid) throws Throwable {
        try {
            delParams(sid);
            p_stm = m_cnn.prepareStatement(ru.major.db.SQL.data.SQL_PUTP, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            for (Map.Entry entry : p.entrySet()) {
                String key = (String) entry.getKey();
                String[] v = (String [])entry.getValue();
                for (int i = 0, n = v.length; i < n; i++) {
                    p_stm.setString(1, sid);
                    p_stm.setString(2, key);
                    p_stm.setString(3, v[i]);
                    p_stm.executeUpdate();
                }
            }
            Commit();
        } catch (java.lang.Throwable tw) {
            Logger.getLogger(DataEng.class.getName()).severe(tw.getMessage());
        }
    }
    
    public void delParams(String sid) throws Throwable {
        try {
            p_stm = m_cnn.prepareStatement(ru.major.db.SQL.data.SQL_DELP, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            p_stm.setString(1, sid);
            p_stm.executeUpdate();
            Commit();
        } catch (java.lang.Throwable tw) {
        }
    }
            
    public JSONArray getData(int mode, Map<String, String[]> params) throws Throwable {
        String uuid = getNewId();
        String cmd = ru.major.db.SQL.data.SQL_INF;
        
        if (!openDs()) {
            throw new java.lang.Throwable("MAJOR:Can`t open database connection: " + m_last_err);
        }
        if (!params.isEmpty()) {
            putParams(params, uuid);
        }
        JSONArray items = new org.json.JSONArray();
        
        try {
            m_stm = m_cnn.prepareCall(cmd, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            m_stm.registerOutParameter(1, Types.OTHER);
            m_stm.setInt(2, mode);
            m_stm.setString(3, uuid);
            m_stm.execute();
            m_rs = (ResultSet)m_stm.getObject(1);
            ResultSetMetaData rsmd = m_rs.getMetaData();
            int c = rsmd.getColumnCount();
            while ( m_rs.next() ) {
                JSONObject item = new org.json.JSONObject();
                for (int i = 1 ; i < c + 1; i++) {
                    String  colName = rsmd.getColumnLabel(i);
                    if ( "BLOB".equals(m_rs.getMetaData().getColumnTypeName(i)) ) {
                        if ( m_rs.getBytes(colName) != null ) {
                            String file64 = new String(java.util.Base64.getEncoder().encode(m_rs.getBytes(colName)));
                            item.put(colName, file64);
                        }
                    } else {
                        item.put(colName, m_rs.getString(colName));
                    }
                }
                items.put(item);
            }
            Commit();
            delParams(uuid);
            return items;
        } catch(java.lang.Throwable tw){
            log.severe(tw.getMessage());
        } finally {
            delParams(uuid);
            Tools.close(m_rs);
            closeDs();
        }
        return items;
    }
    
    public String getNewId(){
        return UUID.randomUUID().toString();
    }
}

