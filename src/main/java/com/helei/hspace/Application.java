package com.helei.hspace;

import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.RequestDispatcher;

import com.helei.hspace.Dependency;

public class Application {

    public static void main(String[] args) throws Exception {
        JettyServer s = new JettyServer(8080, 5);
        // wiring Object net
        Dependency.wire();
        RequestDispatcher dispatcher = new RequestDispatcher();
        dispatcher.registerProcessor("/hello/([0-9]+)(?:/.*)?", "GET", "com.helei.hspace.article.ArticleService", "hello");
        s.addProcessor(dispatcher);
        s.run();
    }
}