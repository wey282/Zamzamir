package com.example.zamzamir.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Card extends Point {
	public static final List<Card> deck = new ArrayList<>();
	private static final List<Card> cards = new ArrayList<>();

	private static Paint borderPaint;

	public static final int WIDTH = 156, HEIGHT = 220;

	public static Bitmap back;

	private final Bitmap fullSprite;
	private final Bitmap sprite;
	private boolean revealed;

	private int attackValue;
	private int defenceValue;

	private int owner = -1;
	/** The value of @owner for which the card is in the deck*/
	public static final int DECK = -1;

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
		canvas.drawRect(x, y, x + WIDTH, y + HEIGHT, borderPaint);
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

	/** Creates a paint use to draw the border of a card */
	public static void createBorderPaint(int borderColor) {
		borderPaint = new Paint();
		borderPaint.setColor(borderColor);
		borderPaint.setStrokeWidth(20);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeJoin(Paint.Join.ROUND);
	}
}
