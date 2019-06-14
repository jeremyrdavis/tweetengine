package io.vertx.examples.tweetengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * {
 *     "tweet": {
 *         "status_id" : 123456789,
 *         "reply_message" : "@twitteruser Thanks for the tweet!"
 *     }
 * }
 */
public class MainVerticle extends AbstractVerticle {

  Twitter twitter;

  WebClient webClient;

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
    apiRouter.post("/reply").handler(this::replyHandler);
    apiRouter.get("/dm").handler(this::twitter4jDirectMessageHandler);

    vertx.deployVerticle(new TwitterStreamVerticle(), ar -> {
      if (ar.succeeded()) {
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
      }else{
        startFuture.fail(ar.cause());
      }
    });

  }

  private void replyHandler(RoutingContext routingContext) {

    String replyText = new StringBuilder()
      .append("@")
      .append(routingContext.getBodyAsJson().getJsonObject("user").getString("screen_name"))
      .append(" Thanks for the tweet!")
      .append(" Sent from Reactive Twitter MSA at ")
      .append(Date.from(Instant.now()).getTime()).toString();

    vertx.executeBlocking((Future<Object> future) -> {
      try {
        Status status = twitter.updateStatus(replyText);
        System.out.println(status.toString());
        future.complete();
      } catch (TwitterException e) {
        e.printStackTrace();
        future.complete(e.getMessage());
      }
    }, res -> {
      if (res.succeeded()) {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(Json.encodePrettily(new JsonObject().put("outcome", "success").put("reply", replyText)));
      } else {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("error", res.cause().getMessage()).toBuffer());
      }
    });
  }

  private void defaultResultHandler(final AsyncResult result, final RoutingContext routingContext, final JsonObject successMessage) {
    if (result.succeeded()) {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("Content-Type", "application/json")
        .end(Json.encodePrettily(successMessage));
    } else {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("error", result.cause().getMessage()).toBuffer());
    }

  }

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello, Tweetengine!");
  }

  private void twitter4jDirectMessageHandler(RoutingContext routingContext) {

    vertx.executeBlocking((Future<Object> future) -> {
      try {
        twitter.sendDirectMessage(1113238476600893445L, "Hi");
        System.out.println("Sending direct message to @vertxdemo");
        future.complete(new JsonObject().put("result", "message sent"));
      } catch (Exception e) {
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
}
