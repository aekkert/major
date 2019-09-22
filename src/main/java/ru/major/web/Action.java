/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.major.web;

/**
 *
 * @author alex
 */
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {
    public static final String MODE_PARAM = "q";
    public static final String IMODE_PARAM = "i";
    public static final String IUSER_PARAM = "u";
    
    public void perform(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException;
    
}

