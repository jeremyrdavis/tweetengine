package io.vertx.examples.tweetengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MainVerticle extends AbstractVerticle {

  WebClient webClient;

  Twitter twitter;

  JsonObject config;
  @Override
  public void start(Future<Void> startFuture) {

    webClient = WebClient.create(vertx);
    twitter = TwitterFactory.getSingleton();

    Router baseRouter = Router.router(vertx);
    baseRouter.route("/").handler(this::indexHandler);

    Router apiRouter = Router.router(vertx);
    apiRouter.route("/*").handler(BodyHandler.create());

    baseRouter.mountSubRouter("/api", apiRouter);
    apiRouter.post("/directmessage").handler(this::directMessageHandler);

    vertx
      .createHttpServer()
      .requestHandler(baseRouter::accept)
      .listen(8080, result -> {
        if (result.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }

  private void directMessageHandler(RoutingContext routingContext) {

    JsonObject directMessageJson = routingContext.getBodyAsJson();

    vertx.executeBlocking((Future<Object> future) -> {
      try {
        twitter.sendDirectMessage(directMessageJson.getLong("id"), directMessageJson.getString("message"));
        System.out.println("Sending direct message to " + directMessageJson.getString("screen_name"));
        future.complete(new JsonObject().put("result", "Success!"));
      } catch (TwitterException e) {
        future.fail(e.getMessage());
      }
    }, res -> {
      if (res.succeeded()) {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(res.result().toString());
      } else {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("error", res.cause().getMessage()).toBuffer());
      }
    });

  }

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello, Tweetengine!");
  }

}
