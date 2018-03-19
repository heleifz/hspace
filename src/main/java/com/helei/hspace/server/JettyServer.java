package com.helei.hspace.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helei.hspace.server.RequestProcessor;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class JettyServer implements com.helei.hspace.server.WebServer
{

    private Server server;
    private ArrayList<RequestProcessor> processors = new ArrayList<RequestProcessor>();

    /**
     * @param port http port
     * @param numThread number of threads
     */
    public JettyServer(int port, int numThread) {
        server = new Server(port);
        server.setHandler(new AbstractHandler() {
        
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                String content = null;
                for (RequestProcessor p : processors) {
                    content = p.handle(request, response);
                }
                response.getWriter().write(content);
                baseRequest.setHandled(true);
            }

        });
    }
   
    @Override
    public void run() throws Exception {
        server.start();
    }

    @Override
    public void addProcessor(RequestProcessor processor) {
        processors.add(processor);
    }

}