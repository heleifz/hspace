package com.helei.hspace.server;

/**
 * Web Server interface
 */
public interface WebServer {

    // start web server
    void run() throws Exception;
    void addProcessor(RequestProcessor processor);

}