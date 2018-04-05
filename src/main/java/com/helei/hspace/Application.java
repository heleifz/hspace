package com.helei.hspace;

import com.helei.hspace.ioc.Container;
import com.helei.hspace.router.Router;
import com.helei.hspace.server.JettyServer;
import com.helei.hspace.server.WebServer;

public class Application {

    /**
     * before application hook
     */
    public void beforeRun(WebServer s) {
    }

    public WebServer getServer(String port, int threadNum) {
        WebServer s = new JettyServer(Integer.valueOf(port), threadNum, Container.get());
        return s;
    }

    public void run(String port, int threadNum) throws Exception {
        WebServer s = getServer(port, threadNum);
        // auto wiring Object net
        String packageName = getClass().getPackage().getName();
        System.out.println("autoregister:" + packageName);
        Container.get().autoRegister(packageName);
        // register routers
        Router router = Router.get();
        router.autoRegister(packageName, Container.get());
        s.addProcessor(router);
        beforeRun(s);
        s.run();
    }
}