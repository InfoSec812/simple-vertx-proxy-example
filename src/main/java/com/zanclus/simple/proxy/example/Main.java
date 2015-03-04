package com.zanclus.simple.proxy.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.streams.Pump;
import io.vertx.ext.apex.Router;

/**
 *
 * @author <a href="https://github.com/InfoSec812">Deven Phillips</a>
 */
public class Main extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOG.debug("Deploying Main verticle.");
        Vertx.vertx().deployVerticle("com.zanclus.simple.proxy.example.Main");
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        HttpClient client = vertx.createHttpClient();
        router.route().handler(ctx -> {
            LOG.error("Sending proxied request.");
            HttpClientRequest clientReq = client.request(ctx.request().method(), 80, "www.reddit.com", ctx.request().uri());
            clientReq.headers().addAll(ctx.request().headers().remove("Host"));
            clientReq.putHeader("Host", "www.reddit.com");
            if (ctx.request().method().equals(HttpMethod.POST) || ctx.request().method().equals(HttpMethod.PUT)) {
                if (ctx.request().headers().get("Content-Length")==null) {
                    clientReq.setChunked(true);
                }
            }
            clientReq.handler(pResponse -> {
              LOG.error("Getting response from target");
              ctx.response().headers().addAll(pResponse.headers());
              if (pResponse.headers().get("Content-Length") == null) {
                ctx.response().setChunked(true);
              }
              ctx.response().setStatusCode(pResponse.statusCode());
              ctx.response().setStatusMessage(pResponse.statusMessage());
              Pump targetToProxy = Pump.pump(pResponse, ctx.response());
              targetToProxy.start();
              pResponse.endHandler(v -> ctx.response().end());
            });
            Pump proxyToTarget = Pump.pump(ctx.request(), clientReq);
            proxyToTarget.start();
            ctx.request().endHandler(v -> clientReq.end());
        });
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
