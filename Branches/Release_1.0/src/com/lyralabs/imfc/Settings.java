package com.lyralabs.imfc;

public class Settings {
	private static Integer updateInterval = 250;

	public static void setUpdateInterval(Integer updateInterval) {
		Settings.updateInterval = updateInterval;
	}

	public static Integer getUpdateInterval() {
		return updateInterval;
	}
}