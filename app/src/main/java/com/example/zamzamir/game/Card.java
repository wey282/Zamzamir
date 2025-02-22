package com.example.zamzamir.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Card extends Point {
	public static final List<Card> deck = new ArrayList<>();
	public static final List<Card> discardPile = new ArrayList<>();
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
	public static final int DISCARD_PILE = -2;

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

	/** Checks whether x,y is inside a card */
	public static Card checkTouchForAllCards(float x, float y) {
		for (Card card: cards) {
			if (x > card.x && card.x + WIDTH  > x
		  	 && y > card.y && card.y + HEIGHT > y)
				return card;
		}
		return null;
	}

	/** Checks whether point x,y is inside a card known to given player. */
	public static Card checkTouchForKnownCards(float x, float y, int player) {
		for (Card card: cards) {
			if (x > card.x && card.x + WIDTH  > x
					&& y > card.y && card.y + HEIGHT > y
					&& ((card.revealed && (card.owner != DISCARD_PILE || discardPile.get(0).equals(card))) || player == card.owner))
				return card;
		}
		return null;
	}

	/** Puts back all cards in the deck in order. */
	public static void resetDeck() {
		deck.clear();
		deck.addAll(cards);
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

	/** Shuffles deck according to given shuffle template. */
	public static void shuffle(List<Integer> shuffle) {
		discardPile.clear();
		deck.clear();
		for (int i : shuffle) {
			deck.add(cards.get(i));
		}
	}

	/** Adds*/
	public static void discard(Card card) {
		card.owner = DISCARD_PILE;
		discardPile.add(0, card);
	}

	@NonNull
	@Override
	public String toString() {
		return "id: " + cards.indexOf(this) + " Value: " + defenceValue + "/" + attackValue + " Position: " +  super.toString();
	}
}
