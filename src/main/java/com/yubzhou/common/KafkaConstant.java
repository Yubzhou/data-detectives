package com.yubzhou.common;

public class KafkaConstant {
	public static final String REQUEST_TOPIC = "requests";
	public static final String REQUEST_GROUP_ID = "request-processor";
	public static final int REQUEST_PARTITIONS = 3;
	public static final short REQUEST_REPLICATION_FACTOR = 1;


	public static final String RESULT_TOPIC = "results";
	public static final String RESULT_GROUP_ID = "sse-group";
	public static final int RESULT_PARTITIONS = 3;
	public static final short RESULT_REPLICATION_FACTOR = 1;


	public static final String USER_ACTION_TOPIC = "user_actions";
	public static final String USER_ACTION_GROUP_ID = "user_actions_group";
	public static final int USER_ACTION_PARTITIONS = 3;
	public static final short USER_ACTION_REPLICATION_FACTOR = 1;
}
