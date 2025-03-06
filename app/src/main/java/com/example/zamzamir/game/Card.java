package com.example.zamzamir.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

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
	public static Bitmap fullBackSprite;
	private final Bitmap fullSprite;
	private final Bitmap sprite;
	private Bitmap activeSprite;

	private PointF attackPosition;

	private boolean revealed;

	private final int attackValue;
	private final int defenceValue;
	private final int id;

	private int owner = -1;
	/** The value of @owner for which the card is in the deck.*/
	public static final int DECK = -1;
	/** The value of @owner for which the card is in the discard pile.*/
	public static final int DISCARD_PILE = -2;

	public static int player;

	private int xOffset = 0;

	public Card(Bitmap sprite, int attackValue, int defenceValue) {
		this.fullSprite = sprite;
		this.sprite = Bitmap.createScaledBitmap(sprite, WIDTH, HEIGHT, false);
		this.attackValue = attackValue;
		this.defenceValue = defenceValue;
		deck.add(this);
		id = cards.size();
		cards.add(this);

		activeSprite = back;
	}

	/** Draws the card on the screen.*/
	public void draw(Canvas canvas) {
		canvas.drawRect(x + xOffset, y, x + activeSprite.getWidth() + xOffset, y + HEIGHT, borderPaint);
		canvas.drawBitmap(activeSprite, x + xOffset, y, null);
	}

	public boolean isRevealed() {
		return revealed;
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

	public Bitmap getSprite() {
		return sprite;
	}

	public void setActiveSprite(Bitmap activeSprite) {
		this.activeSprite = activeSprite;
	}

	public void setxOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	public PointF getAttackPosition() {
		return attackPosition;
	}

	public void setAttackPosition(PointF attackPosition) {
		this.attackPosition = attackPosition;
	}

	/** Checks whether x,y is inside a card */
	public static Card checkTouchForAllCards(float x, float y) {
		for (Card card: cards) {
			if (x > card.x && card.x + WIDTH  > x
		  	 && y > card.y && card.y + HEIGHT > y
			 && (card.owner != DISCARD_PILE || discardPile.get(0).equals(card)))
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
		deck.forEach(card -> card.setRevealed(false));
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
			Card card = cards.get(i);
			deck.add(card);
			card.setRevealed(false);
			card.setOwner(DECK);
			card.setActiveSprite(back);
		}
	}

	/** Adds a card to the discard pile. */
	public static void discard(GameView gameView, Animation.AnimationManager animationManager, Card card) {
		discard(0, gameView, animationManager, card);
	}

	/** Adds a card to the discard pile. */
	public static void discard(int delay, GameView gameView, Animation.AnimationManager animationManager, Card card) {
		card.owner = DISCARD_PILE;
		discardPile.add(0, card);

		if (!card.isRevealed())
			new Animation.CardFlipAnimation(animationManager, delay, card);
		new Animation.CardMoveAnimation(animationManager, delay, card, new PointF(card), new PointF(gameView.getDiscardPilePosition()));
	}

	@NonNull
	@Override
	public String toString() {
		return "id: " + id + " Value: " + defenceValue + "/" + attackValue + " Position: " +  super.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Card))
			return false;
		Card oCard = (Card)o;
		return oCard.id == this.id;
	}

	/** Returns whether the player knows this card. */
	private boolean knownToPlayer() {
		return revealed || owner == player;
	}
}