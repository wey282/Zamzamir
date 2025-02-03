package com.example.zamzamir.game;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zamzamir.R;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
	private List<Card> discardPile;
	private List<List<Card>> players;

	private int currentPlayer = 0;
	private int player;

	private Card selectedCard;
	private Card confirmedCard;
	private Card targetCard;

	private Paint highlightPaint;
	private Paint confirmedPaint;
	private Paint targetedPaint;

	private Button skipButton;
	private Button attackButton;

	private boolean started = false;

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

		setOnTouchListener(this::onTouch);

		highlightPaint = new Paint();
		highlightPaint.setStyle(Paint.Style.STROKE);
		highlightPaint.setStrokeJoin(Paint.Join.ROUND);
		highlightPaint.setStrokeWidth(40);
		highlightPaint.setColor(getColor(R.color.highlight));

		confirmedPaint = new Paint(highlightPaint);
		confirmedPaint.setStrokeWidth(50);
		confirmedPaint.setColor(getColor(R.color.confirmed));

		targetedPaint = new Paint(confirmedPaint);
		targetedPaint.setColor(getColor(R.color.targeted));
	}

	private boolean onTouch(View view, MotionEvent event) {

		if (currentPlayer == player) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Card card = Card.checkTouchForAllCards(event.getX(), event.getY());
				if (card != null && card.getOwner() != Card.DECK) {
					if (card.getOwner() == player) {
						if (card == selectedCard)
							confirmedCard = card;
					} else
						targetCard = card;
				}
			}
		}
		else {

		}
		selectedCard = Card.checkTouchForKnownCards(event.getX(), event.getY(), player);
		invalidate();
		return true;
	}

	public void start(int playerCount, int player, Button skipButton, Button attackButton)
	{
		players = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) {
			players.add(new ArrayList<>());
		}
		this.player = player;

		this.skipButton = skipButton;
		this.attackButton = attackButton;

		dealCards();

		started = true;

		invalidate();
	}

	/** Deals cards to all players, 2 hidden, one revealed */
	private void dealCards() {
		Card lowest = null;
		for (int i = 0; i < players.size(); i++) {
			List<Card> player = players.get(i);
			// deal 1 hidden card
			Card card = Card.deck.remove(0);
			card.setOwner(i);
			player.add(card);
			// deal 1 revealed card
			card = Card.deck.remove(0);
			card.setRevealed(true);
			card.setOwner(i);
			player.add(card);
			// check if current card is the lowest defense value, and if so make that player start
			if (lowest == null || lowest.getDefenceValue() > card.getDefenceValue()) {
				lowest = card;
				currentPlayer = i;
			}

			// deal 1 hidden card
			card = Card.deck.remove(0);
			card.setOwner(i);
			player.add(card);
		}
		if (currentPlayer == player)
			enableButtons();
		else
			disableButtons();
	}

	/** Disables the buttons and colors them accordingly*/
	private void disableButtons() {
		ColorStateList disabledColor = ColorStateList.valueOf(getColor(R.color.disabled_button));
		skipButton.setEnabled(false);
		skipButton.setBackgroundTintList(disabledColor);
		attackButton.setEnabled(false);
		attackButton.setBackgroundTintList(disabledColor);
	}

	/** Enables the buttons and colors them accordingly*/
	private void enableButtons() {
		ColorStateList skipColor = ColorStateList.valueOf(getColor(R.color.skip_button));
		skipButton.setEnabled(true);
		skipButton.setBackgroundTintList(skipColor);
		ColorStateList attackColor = ColorStateList.valueOf(getColor(R.color.attack_button));
		attackButton.setEnabled(true);
		attackButton.setBackgroundTintList(attackColor);
	}


	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		if (!started)
			return;
		int cx = getWidth()/2 - Card.WIDTH/2, cy = getHeight()/2 - Card.HEIGHT/2;
		for (Card card: Card.deck) {
			card.x = cx;
			card.y = cy;
		}
		if (!Card.deck.isEmpty())
			Card.deck.get(0).draw(canvas);
		if (selectedCard != null)
			canvas.drawRect(selectedCard.x, selectedCard.y, selectedCard.x + Card.WIDTH, selectedCard.y + Card.HEIGHT, highlightPaint);
		if (confirmedCard != null)
			canvas.drawRect(confirmedCard.x, confirmedCard.y, confirmedCard.x + Card.WIDTH, confirmedCard.y + Card.HEIGHT, confirmedPaint);
		if (targetCard != null)
			canvas.drawRect(targetCard.x, targetCard.y, targetCard.x + Card.WIDTH, targetCard.y + Card.HEIGHT, targetedPaint);
		for (int i = 0; i < players.size(); i++) {
			List<Card> player = players.get(i);
			double angle = Math.PI*2*i/players.size()+Math.PI/2;
			for (int j = 0; j < player.size(); j++) {
				Card card = player.get(j);
				card.x = cx + (int)(Math.cos(angle)*Card.HEIGHT*1.6) + (int)((j - (double)player.size() /2 + 0.5) * Card.WIDTH * 1.5);
				card.y = cy + (int)(Math.sin(angle)*Card.HEIGHT*1.6);
				card.draw(canvas);
			}
		}
		if (selectedCard != null) {
			canvas.drawBitmap(selectedCard.getFullSprite(), 0, 0, null);
		}
	}

	/** Returns the static color with given id. */
	public int getColor(int id) {
		return ContextCompat.getColor(getContext(), id);
	}
}
