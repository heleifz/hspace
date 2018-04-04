package com.helei.hspace;

import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.WebServer;
import com.helei.hspace.server.RequestDispatcher;

import com.helei.hspace.Dependency;

// TODO extract core and application
public class Application {

    public static void main(String[] args) throws Exception {
        WebServer s = new JettyServer(8080, 5);
        // wiring Object net
        RequestDispatcher dispatcher = new RequestDispatcher();
        Dependency.wire();
        dispatcher.registerProcessor("/hello/([0-9]+)(?:/.*)?", "GET", "com.helei.hspace.article.ArticleService", "hello");
        s.addProcessor(dispatcher);
        s.run();
    }
}