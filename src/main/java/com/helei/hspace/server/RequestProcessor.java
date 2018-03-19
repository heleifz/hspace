package com.helei.hspace.server;

import javax.servlet.http.*;

/**
 * handle http request
 */
public interface RequestProcessor {
  
    /**
     * @param req servlet request
     * @param resp servlet response
     * @return HTTP body
     */
    public String handle(HttpServletRequest req, HttpServletResponse resp);
    
}