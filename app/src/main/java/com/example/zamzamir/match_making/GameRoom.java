package com.example.zamzamir.match_making;

import com.example.zamzamir.StaticUtils;

import java.util.List;

public class GameRoom {
	private int playerCount;
	private List<Integer> shuffle;
	private List<Integer> order;

	public GameRoom() {}

	public GameRoom(int playerCount) {
		this.playerCount = playerCount;
		this.shuffle = StaticUtils.createShuffle();
		this.order = StaticUtils.createPlayerOrder(playerCount);
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public List<Integer> getShuffle() {
		return shuffle;
	}

	public List<Integer> getOrder() {
		return order;
	}
}
