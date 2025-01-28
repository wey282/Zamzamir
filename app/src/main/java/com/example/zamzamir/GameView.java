package com.example.zamzamir;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
	private List<Card> discardPile;
	private List<List<Card>> players;
	private int currentPlayer;
	private int player;

	public GameView(Context context) {
		super(context);
		init();
	}

	public GameView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		discardPile = new ArrayList<>();
	}

	public void start(int playerCount, int player)
	{
		players = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) {
			players.add(new ArrayList<>());
		}
		this.player = player;
		dealCards();
	}

	/** Deals cards to all players, 2 hidden, one revealed*/
	private void dealCards() {
		for (List<Card> player: players) {
			Card card = Card.deck.remove(0);
			player.add(card);
			card = Card.deck.remove(0);
			card.setRevealed(true);
			player.add(card);
			card = Card.deck.remove(0);
			player.add(card);
		}
	}


	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		int cx = getWidth()/2 - Card.WIDTH/2, cy = getHeight()/2 - Card.HEIGHT/2;
		for (Card card: Card.deck) {
			card.x = cx;
			card.y = cy;
		}
		Card.deck.get(0).draw(canvas);
		for (int i = 0; i < players.size(); i++) {
			List<Card> player = players.get(i);
			double angle = Math.PI*2*i/players.size()+Math.PI/2;
			for (int j = 0; j < player.size(); j++) {
				Card card = player.get(j);
				card.x = cx + (int)(Math.cos(angle)*Card.HEIGHT*1.2) + (int)((j - (double)player.size() /2 + 0.5) * Card.WIDTH * 1.5);
				card.y = cy + (int)(Math.sin(angle)*Card.HEIGHT*1.2);
				card.draw(canvas);
			}
		}
	}

	/** Kills current update thread if already running and start a thread called every 1000/TARGET_FPS milliseconds*/
	private void startUpdate() {

	}
}
