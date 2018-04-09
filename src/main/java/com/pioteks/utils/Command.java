package com.pioteks.utils;

import java.util.List;
import java.util.Map;

public class Command {

	
	
	private List<RequestResponse> commandListMode1;
	private List<RequestResponse> commandListMode2;
	
	private int mode;

	private int delayTime = 100;
	
	

	public List<RequestResponse> getCommandListMode1() {
		return commandListMode1;
	}

	public void setCommandListMode1(List<RequestResponse> commandListMode1) {
		this.commandListMode1 = commandListMode1;
	}

	public List<RequestResponse> getCommandListMode2() {
		return commandListMode2;
	}

	public void setCommandListMode2(List<RequestResponse> commandListMode2) {
		this.commandListMode2 = commandListMode2;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}
	
	
	
	
}
