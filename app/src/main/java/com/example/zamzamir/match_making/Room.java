package com.example.zamzamir.match_making;

public class Room {
	boolean full = true;
	int ready = 0;

	public Room() {}

	public Room(boolean full) {
		this.full = full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public boolean isFull() {
		return full;
	}


}
