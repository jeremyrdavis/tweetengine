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

  Map<Integer, JsonObject> processedTweets = new HashMap<>();

  WebClient webClient;

  JsonObject config;

  @Override
  public void start(Future<Void> startFuture) {

    webClient = WebClient.create(vertx);

    Router baseRouter = Router.router(vertx);
    baseRouter.route("/").handler(this::indexHandler);

    Router apiRouter = Router.router(vertx);
    apiRouter.route("/*").handler(BodyHandler.create());

    apiRouter.post("/reply").handler(this::replyHandler);

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx.deployVerticle(new TwitterVerticle(), ar -> {
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

  private void directMessageHandler(RoutingContext routingContext) {

  }

  private void replyHandler(RoutingContext routingContext) {

    System.out.println("replyHandler");

    JsonObject requestJson = routingContext.getBodyAsJson();

    System.out.println("request payload:\n" + requestJson);

    String replyText = new StringBuilder()
      .append("@")
      .append(requestJson.getJsonObject("user").getString("screen_name"))
      .append(" Thanks for the tweet!")
      .append(" Sent from Reactive Twitter MSA at ")
      .append(Date.from(Instant.now()).getTime()).toString();

    System.out.println("reply:\n" + replyText);

    JsonObject message = new JsonObject()
      .put(EventBusConstants.MESSAGE_KEY, new JsonObject()
        .put(EventBusConstants.ACTION, EventBusConstants.ACTIONS_REPLY)
        .put(EventBusConstants.PARAMETERS_REPLY_TO_STATUS_ID, requestJson.getInteger("id"))
        .put(EventBusConstants.PARAMETERS_REPLY_STATUS, replyText));

    System.out.println("message:\n" + message);

    vertx.<JsonObject>eventBus().send(EventBusConstants.ADDRESS, message, ar -> {
      if (ar.succeeded()) {
        processedTweets.put(requestJson.getInteger("id"), message);
        processedTweets.forEach((k,v) -> System.out.println("key: " + k + "value: " + v));
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(Json.encodePrettily(new JsonObject().put("outcome", "success").put("reply", replyText)));
      } else {
        HttpServerResponse response = routingContext.response();
        response
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("error", ar.cause().getMessage()).toBuffer());
      }
    });
  }

/*
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
      defaultResultHandler(res, routingContext, new JsonObject().put("outcome", "success"));
    });

  }
*/

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Hello, Tweetengine!");
  }

}
