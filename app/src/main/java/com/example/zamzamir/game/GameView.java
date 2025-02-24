package com.example.zamzamir.game;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zamzamir.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Consumer;

public class GameView extends View {
	private DocumentReference lastTurnReference;

	private List<List<Card>> players;

	private int currentPlayer = 0;
	private int player;

	private Card selectedCard;
	private Card confirmedCard;
	private Card targetCard;
	private Card drawnCard;

	private Paint highlightPaint;
	private Paint confirmedPaint;
	private Paint targetedPaint;
	private Paint discardPillePaint;

	private Button skipButton;
	private Button attackButton;

	private boolean deckSelected;
	private boolean discardPileSelected;

	private boolean started = false;
	private boolean skipped = false;

	private boolean receivingCard = false;

	private int rank = 4;
	private Consumer<Integer> showGameEndScreen;

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

		discardPillePaint = new Paint(targetedPaint);
		discardPillePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		discardPillePaint.setStrokeWidth(30);
	}

	private boolean onTouch(View view, MotionEvent event) {

		if (currentPlayer == player) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Card card = Card.checkTouchForAllCards(event.getX(), event.getY());
				if (card != null && card.getOwner() != Card.DECK && card.getOwner() != Card.DISCARD_PILE) {
					if (card.getOwner() == player) {
						if (card == selectedCard) {
							confirmedCard = card;
							if (drawnCard != null)
								switchCard(drawnCard.getOwner());
							else if (targetCard != null)
								enableAttackButton();
						}
					} else {
						targetCard = card;
						if (confirmedCard != null && drawnCard == null)
							enableAttackButton();
					}
					deckSelected = false;
					discardPileSelected = false;
				}
				else if (card != null && card.getOwner() == Card.DECK) {
					if (deckSelected) {
						if (receivingCard)
							receiveCard(Turn.FROM_DECK);
						else if (drawnCard == null)
							drawCard(Turn.FROM_DECK);

						confirmedCard = null;
					}
					deckSelected = true;
					discardPileSelected = false;
				}
				else if (card != null && card.getOwner() == Card.DISCARD_PILE) {

					if (discardPileSelected) {
						if (drawnCard != null && drawnCard.getOwner() == Card.DECK)
							discard();
						else if (receivingCard)
							receiveCard(Turn.FROM_DISCARD_PILE);
						else if (drawnCard == null)
							drawCard(Turn.FROM_DISCARD_PILE);
						confirmedCard = null;
					}
					discardPileSelected = true;
					deckSelected = false;
				}
				else {
					if (inDiscardPile(event.getX(), event.getY()) && drawnCard != null && drawnCard.getOwner() == Card.DECK)
						discard();
					deckSelected = false;
					discardPileSelected = false;
				}
			}
		}
		else {
			deckSelected = false;
			discardPileSelected = false;
		}
		selectedCard = Card.checkTouchForKnownCards(event.getX(), event.getY(), player);
		invalidate();
		return true;
	}

	public void start(int playerCount, int player, Button skipButton, Button attackButton, DocumentReference lastTurnReference, Consumer<Integer> showGameEndScreen)
	{
		players = new ArrayList<>();
		for (int i = 0; i < playerCount; i++) {
			players.add(new ArrayList<>());
		}
		rank = playerCount;

		this.player = player;

		this.skipButton = skipButton;
		this.attackButton = attackButton;

		this.skipButton.setOnClickListener(v -> skip());
		this.attackButton.setOnClickListener(v -> attack());

		this.lastTurnReference = lastTurnReference;

		this.showGameEndScreen = showGameEndScreen;

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
			enableSkipButton();
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

	/** Enables the skip button and colors it accordingly*/
	private void enableSkipButton() {
		ColorStateList skipColor = ColorStateList.valueOf(getColor(R.color.skip_button));
		skipButton.setEnabled(true);
		skipButton.setBackgroundTintList(skipColor);
	}

	/** Enables the attack button and colors it accordingly*/
	private void enableAttackButton() {
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

		centerCards(cx, cy);

		// draw borders
		canvas.drawRect(cx + (int)(Card.WIDTH*0.6), cy, cx + (int)(Card.WIDTH*0.6)+Card.WIDTH, cy+Card.HEIGHT, discardPillePaint);
		if (selectedCard != null)
			canvas.drawRect(selectedCard.x, selectedCard.y, selectedCard.x + Card.WIDTH, selectedCard.y + Card.HEIGHT, highlightPaint);
		if (confirmedCard != null)
			canvas.drawRect(confirmedCard.x, confirmedCard.y, confirmedCard.x + Card.WIDTH, confirmedCard.y + Card.HEIGHT, confirmedPaint);
		if (targetCard != null)
			canvas.drawRect(targetCard.x, targetCard.y, targetCard.x + Card.WIDTH, targetCard.y + Card.HEIGHT, targetedPaint);

		// draw deck
		if (!Card.deck.isEmpty())
			Card.deck.get(0).draw(canvas);

		// draw discard pile
		if (!Card.discardPile.isEmpty())
			Card.discardPile.get(0).draw(canvas);


		// draw player's cards
		for (int i = 0; i < players.size(); i++) {
			List<Card> player = players.get(i);
			double angle = Math.PI*2*(i-this.player)/players.size()+Math.PI/2;
			for (int j = 0; j < player.size(); j++) {
				Card card = player.get(j);
				card.x = cx + (int)(Math.cos(angle)*Card.HEIGHT*1.6) + (int)((j - (double)player.size() /2 + 0.5) * Card.WIDTH * 1.5);
				card.y = cy + (int)(Math.sin(angle)*Card.HEIGHT*1.6);
				card.draw(canvas);
			}
		}
		// show resized selected card
		if (selectedCard != null) {
			canvas.drawBitmap(selectedCard.getFullSprite(), 0, getHeight()/2f-selectedCard.getFullSprite().getHeight()/2f, null);
		}
		// show resized drawn card
		if (drawnCard != null) {
			canvas.drawBitmap(drawnCard.getFullSprite(), getWidth()-drawnCard.getFullSprite().getWidth(), getHeight()/2f-drawnCard.getFullSprite().getHeight()/2f, null);
		}
	}

	private static void centerCards(int cx, int cy) {
		for (Card card: Card.deck) {
			card.x = cx -(int)(Card.WIDTH*0.6);
			card.y = cy;
		}
		for (Card card: Card.discardPile) {
			card.x = cx +(int)(Card.WIDTH*0.6);
			card.y = cy;
			card.setRevealed(true);
		}
	}

	/** Plays a skip turn. */
	private void skip() {
		skipped = true;
		Turn turn = Turn.skipTurn(player);
		lastTurnReference.set(turn);
	}

	/** Plays an attack turn. */
	private void attack() {
		Turn turn = Turn.attackTurn(player, players.get(player).indexOf(confirmedCard), targetCard.getOwner(), players.get(targetCard.getOwner()).indexOf(targetCard));
		lastTurnReference.set(turn);
	}

	/** Reveals a card drawn by the player.*/
	private void drawCard(int deck) {
		if (deck == Turn.FROM_DECK) {
			drawnCard = Card.deck.get(0);
		}
		else {
			drawnCard = Card.discardPile.get(0);
		}
		hideButtons();
		invalidate();
	}

	/** Switch drawn card with one of player's cards. */
	private void switchCard(int owner) {
		Turn turn = Turn.drawTurn(player, players.get(player).indexOf(confirmedCard), owner == Card.DECK ? Turn.FROM_DECK : Turn.FROM_DISCARD_PILE);
		drawnCard = null;
		lastTurnReference.set(turn);
		showButtons();
	}

	/** Receive card from given pile. */
	private void receiveCard(int from) {
		Turn turn = Turn.receiveTurn(player, from);
		lastTurnReference.set(turn);
	}

	/** Discard drawn card. */
	private void discard() {
		Turn turn = Turn.discardTurn(player);
		drawnCard = null;
		lastTurnReference.set(turn);
		showButtons();
	}

	/** Receives turn from other player. */
	public void onTurn(DocumentSnapshot documentSnapshot) {
		Turn turn = documentSnapshot.toObject(Turn.class);
		if (turn != null) {
			playTurn(turn);
		}
	}

	private void playTurn(Turn turn) {
		List<Card> player = players.get(turn.getPlayer());

		Card card;

		switch (turn.getType()) {
			case Turn.DRAW:
				Card.discard(player.get(turn.getSelectedCard()));
				card = turn.getFrom() == Turn.FROM_DECK ? Card.deck.remove(0) : Card.discardPile.remove(1);
				player.set(turn.getSelectedCard(), card);
				card.setOwner(turn.getPlayer());
				card.setRevealed(false);
				break;
			case Turn.RECEIVE:
				card = turn.getFrom() == Turn.FROM_DECK ? Card.deck.remove(0) : Card.discardPile.remove(0);
				player.add(card);
				card.setOwner(turn.getPlayer());
				card.setRevealed(true);
				break;
			case Turn.ATTACK:
				Card attacker = players.get(turn.getPlayer()).get(turn.getSelectedCard());
				Card defender = players.get(turn.getTargetPlayer()).get(turn.getTargetCard());
				if (attacker.getAttackValue() + turn.getAttackerRoll() >= defender.getDefenceValue() + turn.getDefenderRoll())
					Card.discard(players.get(turn.getTargetPlayer()).remove(turn.getTargetCard()));
				if (attacker.getAttackValue() + turn.getAttackerRoll() <= defender.getDefenceValue() + turn.getDefenderRoll())
					Card.discard(players.get(turn.getPlayer()).remove(turn.getSelectedCard()));
				Toast.makeText(getContext(), "Atk: " + turn.getAttackerRoll() + ", Def: " + turn.getDefenderRoll(), Toast.LENGTH_LONG).show();
				break;
			case Turn.DISCARD:
				Card.discard(Card.deck.remove(0));
				break;
		}

		turnEnd();
	}

	/** Signals the turn has ended. */
	private void turnEnd() {
		if (hasGameEnded()){
			gameEnd();
		}
		currentPlayer++;
		if (currentPlayer >= players.size())
			currentPlayer = 0;
		confirmedCard = null;
		targetCard = null;
		deckSelected = false;
		discardPileSelected = false;
		if (player == currentPlayer) {
			if (players.get(player).isEmpty()) {
				noTurn();
			}
			else if (skipped) {
				skipped = false;
				receiveCard();	
			}
			else {
				enableSkipButton();
			}
		}
		else {
			disableButtons();
		}
		invalidate();
	}

	private void noTurn() {
		Turn turn = Turn.emptyTurn(player);
		lastTurnReference.set(turn);
	}

	private void receiveCard() {
		receivingCard = true;
	}

	/** Returns the static color with given id. */
	public int getColor(int id) {
		return ContextCompat.getColor(getContext(), id);
	}

	public void hideButtons() {
		skipButton.setVisibility(GONE);
		attackButton.setVisibility(GONE);
	}

	public void showButtons() {
		skipButton.setVisibility(VISIBLE);
		attackButton.setVisibility(VISIBLE);
	}

	/** Returns whether the given point is inside the discard pile. */
	private boolean inDiscardPile(float x, float y) {
		return x < getWidth()/2f + Card.WIDTH*1.1
			&& x > getWidth()/2f + Card.WIDTH*0.1
			&& y > getHeight()/2f - Card.HEIGHT/2f
			&& y < getHeight()/2f + Card.HEIGHT/2f;
	}

	/** Check whether the game has ended and sets the player's rank. */
	private boolean hasGameEnded() {
		int playersAlive = players.size();
		for (List<Card> player: players)
			if (player.isEmpty())
				playersAlive--;
		if (!players.get(player).isEmpty())
			rank = playersAlive;
		return playersAlive <= 1;
	}

	private void gameEnd() {
		hideButtons();
		showGameEndScreen.accept(rank);
	}
}
