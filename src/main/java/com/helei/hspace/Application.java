package com.helei.hspace;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.RequestDispatcher;
import com.helei.hspace.server.RequestProcessor;

public class Application {

    public static void main(String[] args) throws Exception {
        JettyServer s = new JettyServer(8080, 5);
        RequestDispatcher dispatcher = new RequestDispatcher();
        dispatcher.registerProcessor("/hello/([0-9]+)(?:/.*)?", "GET", "com.helei.hspace.article.ArticleService", "hello");
        s.addProcessor(dispatcher);
        s.run();
    }
}