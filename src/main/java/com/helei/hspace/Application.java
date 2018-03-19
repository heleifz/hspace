package com.helei.hspace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.RequestProcessor;

public class Application {
    public static void main(String[] args) throws Exception {
        JettyServer s = new JettyServer(8080, 5);
        
        s.addProcessor(new RequestProcessor() {
            @Override
            public String handle(HttpServletRequest req, HttpServletResponse resp) {
                return "<h1>fuckyou</h1>";
            }
        });

        s.run();
    }
}