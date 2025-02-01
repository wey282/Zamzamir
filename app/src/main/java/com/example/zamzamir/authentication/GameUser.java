package com.example.zamzamir.authentication;

public class GameUser {
	private String id;
	private String username;
	private int ELO = 1500;

	public GameUser() {}

	public GameUser(String id, String username) {
		this.id = id;
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public int getELO() {
		return ELO;
	}
}
