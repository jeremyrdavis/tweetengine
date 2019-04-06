package io.vertx.examples.tweetengine;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class TestReplyToTweet {

  private Vertx vertx;

  @Test
  @Timeout(120000)
  @DisplayName("Test Reply Functionality")
  public void testReplyingToATweet(Vertx vertx, VertxTestContext tc) {

    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    JsonObject requestJson = new JsonObject()
      .put("id", TestData.VERTXDEMO.reply_to_status_id)
      .put("user", new JsonObject("user").put("screen_name", TestData.VERTXDEMO.screen_name));

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      webClient.post(8080, "localhost", "/api/reply")
        .as(BodyCodec.string())
        .sendJsonObject(requestJson,
          tc.succeeding(resp -> {
            tc.verify(() -> {
              assertThat(resp.statusCode()).isEqualTo(200);
              assertThat(resp.body()).isNotEmpty();
              assertThat(resp.body()).contains("success");
              requestCheckpoint.flag();
              tc.completeNow();
            });
          }));
    }));
  }
}
