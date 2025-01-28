package com.example.zamzamir;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Card extends Point {
	public static final List<Card> deck = new ArrayList<>();

	public static final int WIDTH = 189, HEIGHT = 264;

	public static Bitmap back;

	private final Bitmap sprite;
	private final Bitmap smallBitmap;
	private boolean revealed;

	private int attackValue;
	private int defenceValue;

	public Card(Bitmap sprite, int attackValue, int defenceValue) {
		this.sprite = sprite;
		this.smallBitmap = Bitmap.createScaledBitmap(sprite, WIDTH, HEIGHT, false);
		this.attackValue = attackValue;
		this.defenceValue = defenceValue;
		x = (int)(Math.random()*1000);
		deck.add(this);
	}

	/** Draws the card on the screen.*/
	public void draw(Canvas canvas) {
		canvas.drawBitmap(revealed ? smallBitmap : back, x, y, null);
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
}
