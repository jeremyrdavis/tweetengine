package io.vertx.examples.tweetengine;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class TestReplyToTweet {

  private Vertx vertx;

  @Test
  @DisplayName("Test Reply Functionality")
  public void testReplyingToATweet(Vertx vertx, VertxTestContext tc) {

    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      webClient.post(8081, "localhost", "/api/censor")
        .as(BodyCodec.string())
        .sendJsonObject(new JsonObject()
            .put("text", "Hi, there!")
            .put("id", 1113217015198703616L)
            .put("screen_name", "jbossdemo"),
          tc.succeeding(resp -> {
            tc.verify(() -> {
              assertThat(resp.statusCode()).isEqualTo(200);
              assertThat(resp.body()).isNotEmpty();
              assertThat(resp.body()).contains("Success");
              requestCheckpoint.flag();
            });
          }));
    }));
  }
}
