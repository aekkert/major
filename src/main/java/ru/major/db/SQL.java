/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.major.db;

/**
 *
 * @author alex
 */
public interface SQL {
    public interface data {
        public static final String SQL_INF = "{? = call mj_GetData(? /*that*/, ? /*prms*/)}";
        public static final String SQL_DELP = "delete from mj_reqParams where ID = ?::uuid /*ID*/";
        public static final String SQL_PUTP = "insert into mj_reqParams(ID, pID, pValue) values(?::uuid /*ID*/, ? /*pID*/, ? /*pValue*/)";
    }
}
