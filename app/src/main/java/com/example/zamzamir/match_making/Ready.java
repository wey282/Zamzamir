package com.example.zamzamir.match_making;

/** Represents whether a player is ready. */
public class Ready {
	private boolean ready;

	public Ready() {
		ready = false;
	}

	public Ready(boolean ready) {
		this.ready = ready;
	}

	public boolean isReady() {
		return ready;
	}

	public void flip() {
		ready = !ready;
	}
}
