package com.example.app.domain.common.logging;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LogPrefix {

	APPLICATION("###"),
	COMMAND("<c>"),
	EVENT("<e>"),
	PROCESSING("==="),
	INCOMING(">>>"),
	OUTGOING("<<<");

	private final String tag;

	public String withText(String text) {
		return tag + " " + text;
	}

	@Override
	public String toString() {
		return tag;
	}
	
}
