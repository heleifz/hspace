package com.helei.hwiki;

import com.helei.hspace.*;
import com.helei.hspace.server.WebServer;
import com.helei.hspace.server.ThreadedServer;
import com.helei.hspace.ioc.Container;

class HWiki extends Application
{
     
    @Override
    public WebServer getServer(String port, int threadNum) {
        WebServer s = new ThreadedServer(Integer.valueOf(port), threadNum, Container.get());
        return s;
    }

    public static void main(String[] arg) {
        HWiki app = new HWiki();
        try {
            app.run("8080", 3);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } 
    }
}