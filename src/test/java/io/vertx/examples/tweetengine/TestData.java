package io.vertx.examples.tweetengine;

import java.time.Instant;
import java.util.Date;

public enum TestData {

  JBOSSDEMO(702198386284433409L, "jbossdemo", 1113217015198703616L),
  VERTXDEMO(1113238476600893445L, "vertxdemo", 1113247355720089600L);

  public final long id;

  public final String screen_name;

  public final long reply_to_status_id;

  private TestData(long id, String screen_name, long reply_to_status_id) {
    this.id = id;
    this.screen_name = screen_name;
    this.reply_to_status_id = reply_to_status_id;
  }

  public static String generateReply(TestData testData){
    return new StringBuilder()
      .append("@")
      .append(testData.screen_name)
      .append(" Reply from Reactive Twitter MSA Demo at ")
      .append(Date.from(Instant.now())).toString();
  }
}
