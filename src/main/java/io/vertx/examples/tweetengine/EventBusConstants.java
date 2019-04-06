package io.vertx.examples.tweetengine;

import oracle.jrockit.jfr.StringConstantPool;

public class EventBusConstants {

  public static final String ADDRESS = "tweetbus";
  public static final String ACTION = "action";
  public static final String ACTIONS_REPLY = "reply";
  public static final String PARAMETERS_REPLY_TO_STATUS_ID = "reply_to_status_id";
  public static final String PARAMETERS_REPLY_TO_SCREEN_NAME = "screen_name";
  public static final String RESULT_FAILURE = "failure";
  public static final String RESULT_FAILURE_MESSAGE = "error_message";
  public static final String RESULT_SUCCESS = "success";
  public static final String PARAMETERS_REPLY_STATUS = "reply_message";
  public static final String MESSAGE_KEY = "message";

  public enum EventBusErrors {

    UNKNOWN_ADDRESS(1, "unrecognized address");

    public final int errorCode;

    public final String errorMessage;

    private EventBusErrors(int errorCodeToSet, String errorMessageToSet){
      this.errorCode = errorCodeToSet;
      this.errorMessage = errorMessageToSet;
    }
  }
}
