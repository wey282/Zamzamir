package com.example.zamzamir.match_making;

import com.example.zamzamir.StaticUtils;

import java.util.List;

public class GameRoom {
	private int playerCount;
	private List<Integer> shuffle;

	public GameRoom() {}


	public GameRoom(int playerCount) {
		this.playerCount = playerCount;
		this.shuffle = StaticUtils.createShuffle();
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public List<Integer> getShuffle() {
		return shuffle;
	}
}
