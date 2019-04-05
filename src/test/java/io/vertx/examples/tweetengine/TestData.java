package io.vertx.examples.tweetengine;

public enum TestData {

  JBOSSDEMO(702198386284433409L, "jbossdemo", 1113217015198703616L),
  VERTXDEMO(1113238476600893445L, "vertxdemo", 1113247322274770944L);

  public final long id;

  public final String screen_name;

  public final long reply_to_status_id;

  private TestData(long id, String screen_name, long reply_to_status_id) {
    this.id = id;
    this.screen_name = screen_name;
    this.reply_to_status_id = reply_to_status_id;
  }
}
