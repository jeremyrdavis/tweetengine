package io.vertx.examples.tweetengine;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class TwitterVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture){

    EventBus eventBus = vertx.eventBus();
    MessageConsumer<JsonObject> consumer = eventBus.consumer(EventBusConstants.ADDRESS);

    consumer.handler(message -> {

      String action = message.body().getString("action");

      switch (action) {
        case EventBusConstants.ACTIONS_REPLY:
          reply(message);
          break;
        default:

          message.fail(EventBusConstants.EventBusErrors.UNKNOWN_ADDRESS.errorCode, EventBusConstants.EventBusErrors.UNKNOWN_ADDRESS.errorMessage);
      }
    });

    startFuture.complete();

  }

  private void reply(Message<JsonObject> message) {
    message.reply(new JsonObject().put("outcome", "success"));
  }
}
