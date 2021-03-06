package org.kevoree.library.javase.http.webbit;

import org.kevoree.annotation.ComponentType;
import org.kevoree.library.javase.http.api.commons.HTTPOperationTuple;
import org.kevoree.library.javase.http.api.server.AbstractHTTPServer;
import org.kevoree.log.Log;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/04/13
 * Time: 11:01
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType (description = "Webbit server to server HTTP request. Thhis implementations is based on servlet API. However webbit doesn't provide a way to do chunked response for binary content. That's why this implementation is not able to stream binary content like media.")
public class WebbitHTTPServer extends AbstractHTTPServer {
    WebServer server;
    private WebbitHTTPHandler handler;

    @Override
    public void start() throws ExecutionException, InterruptedException {

        server = WebServers.createWebServer(port);
//        server.staleConnectionTimeout(Integer.parseInt(getDictionary().get("timeout").toString()));
        handler = new WebbitHTTPHandler(this);
        server.add(handler);
        server.start().get();
        Log.info("{} is starting on {}", context.getInstanceName(), port);
    }

    @Override
    public void stop() throws InterruptedException, ExecutionException {
        if (server != null) {
        Future future = server.stop();
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
            Log.info("{} is stopped on {}", context.getInstanceName(), port);
        } catch (TimeoutException e) {
            Log.warn("Time out when waiting the stop of the server. Maybe it is blocked!");
        }
        }
    }

    @Override
    // TODO replace Object with a specific type and rename the parameter
    public void response(/*HTTPOperationTuple*/Object param) {
        if (param != null && param instanceof HTTPOperationTuple) {
            handler.response((HTTPOperationTuple) param);
        }
    }
}
