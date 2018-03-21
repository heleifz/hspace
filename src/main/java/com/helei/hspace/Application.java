package com.helei.hspace;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.RequestDispatcher;
import com.helei.hspace.server.RequestProcessor;

public class Application {

    public String gogo(String... s) {
        return Arrays.toString(s);
    }

    public static void main(String[] args) throws Exception {
        JettyServer s = new JettyServer(8080, 5);
        RequestDispatcher dispatcher = new RequestDispatcher();
        dispatcher.registerProcessor("/hello/(.*)", "GET", "com.helei.hspace.Application", "gogo");
        s.addProcessor(dispatcher);
        s.run();
    }
}