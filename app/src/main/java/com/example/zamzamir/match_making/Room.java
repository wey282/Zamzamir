package com.example.zamzamir.match_making;

public class Room {
	private boolean full = true;
	private float remainingTime = 1000000;

	public Room() {}

	public Room(boolean full) {
		this.full = full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public boolean getFull() {
		return full;
	}

	public float getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(float remainingTime) {
		this.remainingTime = remainingTime;
	}
}
