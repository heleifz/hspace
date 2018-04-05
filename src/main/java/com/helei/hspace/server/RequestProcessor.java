package com.helei.hspace.server;

import javax.servlet.http.*;

import com.helei.hspace.ioc.Container;

/**
 * handle http request
 */
public interface RequestProcessor {
  
    /**
     * @param req servlet request
     * @param resp servlet response
     * @param container IOC container
     * @return HTTP body
     */
    public String handle(HttpServletRequest req, HttpServletResponse resp, Container container) throws Exception;
    
}