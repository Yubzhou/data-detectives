package com.yubzhou.common;


public class SystemInfo {

	public static final int AVAILABLE_PROCESSORS;

	static {
		AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	}
}