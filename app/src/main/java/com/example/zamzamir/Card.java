package com.example.zamzamir;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Card extends Point {
	public static final List<Card> deck = new ArrayList<>();
	private static final List<Card> cards = new ArrayList<>();

	public static final int WIDTH = 156, HEIGHT = 220;

	public static Bitmap back;

	private final Bitmap fullSprite;
	private final Bitmap sprite;
	private boolean revealed;

	private int attackValue;
	private int defenceValue;

	private int owner = -1;

	public Card(Bitmap sprite, int attackValue, int defenceValue) {
		this.fullSprite = sprite;
		this.sprite = Bitmap.createScaledBitmap(sprite, WIDTH, HEIGHT, false);
		this.attackValue = attackValue;
		this.defenceValue = defenceValue;
		deck.add(this);
		cards.add(this);
	}

	/** Draws the card on the screen.*/
	public void draw(Canvas canvas) {
		canvas.drawBitmap(revealed ? sprite : back, x, y, null);
	}

	public void setRevealed(boolean revealed) {
		this.revealed = revealed;
	}

	public int getAttackValue() {
		return attackValue;
	}

	public int getDefenceValue() {
		return defenceValue;
	}

	public Bitmap getFullSprite() {
		return fullSprite;
	}

	public void setOwner(int player) {
		owner = player;
	}


	public static Card checkTouchForAllCards(float x, float y) {
		for (Card card: cards) {
			if (x > card.x && card.x + WIDTH  > x
		  	 && y > card.y && card.y + HEIGHT > y)
				return card;
		}
		return null;
	}

	public static Card checkTouchForKnownCards(float x, float y, int player) {
		for (Card card: cards) {
			if (x > card.x && card.x + WIDTH  > x
					&& y > card.y && card.y + HEIGHT > y
					&& (card.revealed || player == card.owner))
				return card;
		}
		return null;
	}

	public int getOwner() {
		return owner;
	}
}
