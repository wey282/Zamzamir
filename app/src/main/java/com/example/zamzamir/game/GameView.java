package com.example.zamzamir.game;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.authentication.GameUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
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
	private TextPaint rollPaint;
	private TextPaint activeUserPaint;
	private TextPaint userPaint;
	private TextPaint deadUserPaint;


	private Button skipButton;
	private Button attackButton;
	private Button lastBattleButton;

	private boolean deckSelected;
	private boolean discardPileSelected;

	private boolean started = false;
	private boolean skipped = false;

	private boolean receivingCard = false;

	private int rank = 4;
	private Consumer<Integer> showGameEndScreen;
	private Runnable startTurnTimer, endTurnTimer;

	private Animation.AnimationManager animationManager;

	private int WIDTH, HEIGHT;

	private boolean sizeInitiated = false;

	private Battle lastBattle;
	private boolean viewingLastBattle = false;

	private List<GameUser> users;

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

		rollPaint = new TextPaint();
		rollPaint.setColor(getColor(R.color.roll_text));
		rollPaint.setTextSize(200);

		activeUserPaint = new TextPaint();
		activeUserPaint.setTextSize(50);
		activeUserPaint.setColor(getColor(R.color.current_player));
		userPaint = new TextPaint(activeUserPaint);
		userPaint.setColor(getColor(R.color.player));
		deadUserPaint = new TextPaint(activeUserPaint);
		deadUserPaint.setColor(getColor(R.color.dead_player));

		animationManager = new Animation.AnimationManager(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		sizeInitiated = true;
		WIDTH = w;
		HEIGHT = h;

		if (started) {
			if (player == currentPlayer)
				startTurnTimer.run();

			dealCards();

			invalidate();
		}
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

		if (viewingLastBattle) {
			if (inAttackerCard(event.getX(), event.getY()))
				selectedCard = lastBattle.getAttacker();
			else if (inDefenderCard(event.getX(), event.getY()))
				selectedCard = lastBattle.getDefender();
			else {
				viewingLastBattle = false;
				showButtons();
			}
		}

		invalidate();
		return true;
	}

	/** Starts the game. Initializes values retrieved from Firebase. */
	public void start(List<GameUser> users, int player,
	                  Button skipButton, Button attackButton, Button lastBattleButton,
	                  DocumentReference lastTurnReference,
	                  Consumer<Integer> showGameEndScreen,
	                  Runnable startTurnTimer,
	                  Runnable endTurnTimer)
	{
		Card.player = player;

		players = new ArrayList<>();
		for (int i = 0; i < users.size(); i++) {
			players.add(new ArrayList<>());
		}

		this.users = users;

		this.player = player;

		this.skipButton = skipButton;
		this.attackButton = attackButton;
		this.lastBattleButton = lastBattleButton;

		this.skipButton.setOnClickListener(v -> skip());
		this.attackButton.setOnClickListener(v -> attack());
		this.lastBattleButton.setOnClickListener(v -> viewLastBattle());

		disableButtons();

		this.lastTurnReference = lastTurnReference;

		this.showGameEndScreen = showGameEndScreen;
		this.startTurnTimer = startTurnTimer;
		this.endTurnTimer = endTurnTimer;

		started = true;

		if (sizeInitiated) {
			if (player == currentPlayer)
				startTurnTimer.run();

			dealCards();

			invalidate();
		}
	}

	/** Deals cards to all players, 2 hidden, one revealed */
	private void dealCards() {
		centerCards();
		Card lowest = null;
		int delayBetweenDraws = 350;
		for (int i = 0; i < players.size(); i++) {
			// deal 1 hidden card
			addCardToPlayer(delayBetweenDraws*3*i, i, 0, 3, Turn.FROM_DECK, false);

			// deal 1 revealed card
			Card card = addCardToPlayer(delayBetweenDraws*3*i+delayBetweenDraws, i, 1, 3, Turn.FROM_DECK, true);

			// check if current card is the lowest defense value, and if so make that player start
			if (lowest == null || lowest.getDefenceValue() > card.getDefenceValue()) {
				lowest = card;
				currentPlayer = i;
			}

			// deal 1 hidden card
			addCardToPlayer(delayBetweenDraws*3*i+2*delayBetweenDraws, i, 2, 3, Turn.FROM_DECK, false);
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

		int cx = WIDTH/2-Card.WIDTH/2, cy = HEIGHT/2-Card.HEIGHT/2;

		// draw borders
		canvas.drawRect(cx + (int)(Card.WIDTH*0.6), cy, cx + (int)(Card.WIDTH*0.6)+Card.WIDTH, cy+Card.HEIGHT, discardPillePaint);
		if (deckSelected)
			canvas.drawRect(getDeckPosition().x, getDeckPosition().y, getDeckPosition().x + Card.WIDTH, getDeckPosition().y + Card.HEIGHT, highlightPaint);
		if (selectedCard != null) {
			canvas.rotate(selectedCard.angle, selectedCard.x + Card.WIDTH/2f, selectedCard.y + Card.HEIGHT/2f);
			canvas.drawRect(selectedCard.x, selectedCard.y, selectedCard.x + Card.WIDTH, selectedCard.y + Card.HEIGHT, highlightPaint);
			canvas.rotate(-selectedCard.angle, selectedCard.x + Card.WIDTH/2f, selectedCard.y + Card.HEIGHT/2f);
		}
		if (confirmedCard != null) {
			canvas.rotate(confirmedCard.angle, confirmedCard.x + Card.WIDTH/2f, confirmedCard.y + Card.HEIGHT/2f);
			canvas.drawRect(confirmedCard.x, confirmedCard.y, confirmedCard.x + Card.WIDTH, confirmedCard.y + Card.HEIGHT, confirmedPaint);
			canvas.rotate(-confirmedCard.angle, confirmedCard.x + Card.WIDTH/2f, confirmedCard.y + Card.HEIGHT/2f);
		}
		if (targetCard != null) {
			canvas.rotate(targetCard.angle, targetCard.x + Card.WIDTH/2f, targetCard.y + Card.HEIGHT/2f);
			canvas.drawRect(targetCard.x, targetCard.y, targetCard.x + Card.WIDTH, targetCard.y + Card.HEIGHT, targetedPaint);
			canvas.rotate(-targetCard.angle, targetCard.x + Card.WIDTH/2f, targetCard.y + Card.HEIGHT/2f);
		}

		// draw deck
		if (!Card.deck.isEmpty())
			Card.deck.get(0).draw(canvas);

		// draw discard pile
		if (!Card.discardPile.isEmpty()) {
			if (Card.discardPile.size() > 1)
				Card.discardPile.get(1).draw(canvas);
			Card.discardPile.get(0).draw(canvas);
		}


		// draw player's and names
		for (int i = 0; i < players.size(); i++) {
			// draw cards
			List<Card> player = players.get(i);
			for (int j = 0; j < player.size(); j++) {
				Card card = player.get(j);
				card.draw(canvas);
			}

			// draw name
			PointF p = getNamePosition(i);
			TextPaint paint;

			if (i == currentPlayer)
				paint = activeUserPaint;
			else if (player.isEmpty())
				paint = deadUserPaint;
			else
				paint = userPaint;

			canvas.drawText(users.get(i).getUsername(), p.x, p.y, paint);
		}
		// show resized selected card
		if (selectedCard != null) {
			canvas.drawBitmap(selectedCard.getFullSprite(), 20, HEIGHT/2f-selectedCard.getFullSprite().getHeight()/2f, null);
		}
		// show resized drawn card
		if (drawnCard != null) {
			canvas.drawBitmap(drawnCard.getFullSprite(), WIDTH-20-drawnCard.getFullSprite().getWidth(), HEIGHT/2f-drawnCard.getFullSprite().getHeight()/2f, null);
		}

		// show attacker and defender
		if (viewingLastBattle) {
			Card attacker = lastBattle.getAttacker();
			Bitmap attackerFullSprite = attacker.getFullSprite();
			canvas.drawRect(20, HEIGHT/2f-attackerFullSprite.getHeight()/2f, 20+attackerFullSprite.getWidth(), HEIGHT/2f+attackerFullSprite.getHeight()/2f, targetedPaint);
			canvas.drawBitmap(attackerFullSprite, 20, HEIGHT/2f-attackerFullSprite.getHeight()/2f, null);
			canvas.drawText(Integer.toString(lastBattle.getAttackerRoll()), attackerFullSprite.getWidth()/2f - 30, HEIGHT/2f-attackerFullSprite.getHeight()/2f + 120, rollPaint);

			Card defender = lastBattle.getDefender();
			Bitmap defenderFullSprite = defender.getFullSprite();
			canvas.drawRect(WIDTH-20-defenderFullSprite.getWidth(), HEIGHT/2f-defenderFullSprite.getHeight()/2f, WIDTH-20, HEIGHT/2f+defenderFullSprite.getHeight()/2f, confirmedPaint);
			canvas.drawBitmap(defenderFullSprite, WIDTH-20-defenderFullSprite.getWidth(), HEIGHT/2f-defenderFullSprite.getHeight()/2f, null);
			canvas.drawText(Integer.toString(lastBattle.getDefenderRoll()), WIDTH-defenderFullSprite.getWidth()/2f - 50, HEIGHT/2f-defenderFullSprite.getHeight()/2f+120, rollPaint);
		}

		ImageObject.drawAll(canvas);
	}

	private void centerCards() {
		Point deckPosition = getDeckPosition();
		for (Card card: Card.deck) {
			card.x = deckPosition.x;
			card.y = deckPosition.y;
		}
		Point discardPilePosition = getDeckPosition();
		for (Card card: Card.discardPile) {
			card.x = discardPilePosition.x;
			card.y = discardPilePosition.y;
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
		receivingCard = false;
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
				Card.discard(this, animationManager, player.get(turn.getSelectedCard()));
				card = turn.getFrom() == Turn.FROM_DECK ? Card.deck.remove(0) : Card.discardPile.remove(1);
				player.set(turn.getSelectedCard(), card);
				card.setOwner(turn.getPlayer());
				if (card.isRevealed())
					new Animation.CardFlipAnimation(animationManager, 200, card);
				new Animation.CardMoveAnimation(animationManager, 200, card, new PointF(getDeckPosition()), new PointF(getCardPosition(turn.getPlayer(), turn.getSelectedCard(), player.size())));
				card.setAttackPosition(getAttackPosition(turn.getPlayer(), turn.getSelectedCard(), player.size()));
				card.angle = calculateAngle(turn.getPlayer());
				break;
			case Turn.RECEIVE:
				addCardToPlayer(turn.getPlayer(), turn.getFrom());
				sortCards(turn.getPlayer(), Animation.CardMoveAnimation.length);
				break;
			case Turn.ATTACK:
				Card attacker = players.get(turn.getPlayer()).get(turn.getSelectedCard());
				Card defender = players.get(turn.getTargetPlayer()).get(turn.getTargetCard());
				Animation.AttackAnimation.play(animationManager, 0, attacker, defender, turn.getAttackerRoll(), turn.getDefenderRoll());
				if (attacker.getAttackValue() + turn.getAttackerRoll() >= defender.getDefenceValue() + turn.getDefenderRoll()) {
					Card.discard(Animation.AttackAnimation.totalLength, this, animationManager, players.get(turn.getTargetPlayer()).remove(turn.getTargetCard()));
					sortCards(turn.getTargetPlayer(), Animation.AttackAnimation.totalLength + Animation.CardMoveAnimation.length);
				}
				if (attacker.getAttackValue() + turn.getAttackerRoll() <= defender.getDefenceValue() + turn.getDefenderRoll()) {
					Card.discard(Animation.AttackAnimation.totalLength, this, animationManager, players.get(turn.getPlayer()).remove(turn.getSelectedCard()));
					sortCards(turn.getPlayer(), Animation.AttackAnimation.totalLength + Animation.CardMoveAnimation.length);
				}
				if (this.player == turn.getPlayer() || this.player == turn.getTargetPlayer()) {
					lastBattleButton.setVisibility(VISIBLE);
					lastBattle = new Battle(attacker, defender, turn.getAttackerRoll(), turn.getDefenderRoll());
				}
				break;
			case Turn.DISCARD:
				Card.discard(this, animationManager, Card.deck.remove(0));
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
			startTurnTimer.run();

			if (players.get(player).isEmpty()) {
				noTurn();
			}
			else if (skipped) {
				skipped = false;
				receiveCard();	
			}
			else if (players.get(player).size() < 5){
				enableSkipButton();
			}
		}
		else {
			if (currentPlayer-1 % players.size() == player)
				endTurnTimer.run();
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
		lastBattleButton.setVisibility(GONE);
	}

	public void showButtons() {
		skipButton.setVisibility(VISIBLE);
		attackButton.setVisibility(VISIBLE);
		if (lastBattle != null)
			lastBattleButton.setVisibility(VISIBLE);
	}

	/** Returns whether the given point is inside the discard pile. */
	private boolean inDiscardPile(float x, float y) {
		return x < WIDTH/2f + Card.WIDTH*1.1
			&& x > WIDTH/2f + Card.WIDTH*0.1
			&& y > HEIGHT/2f - Card.HEIGHT/2f
			&& y < HEIGHT/2f + Card.HEIGHT/2f;
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
		new Animation.Delay(animationManager, Animation.AttackAnimation.totalLength*2, () -> showGameEndScreen.accept(rank));
	}

	public Point getDeckPosition() {
		return new Point(WIDTH/2 - (int)(Card.WIDTH*1.1), HEIGHT/2-Card.HEIGHT/2);
	}

	public Point getDiscardPilePosition() {
		return new Point(WIDTH/2 + (int)(Card.WIDTH*0.1), HEIGHT/2-Card.HEIGHT/2);
	}

	public Point getCardPosition(int player, int card, int handSize) {
		double angle = Math.PI*2*(player-this.player)/players.size()+Math.PI/2;
		return new Point(WIDTH/2 - Card.WIDTH/2 + (int)(Math.cos(angle)*Card.HEIGHT*1.7)
				    + (int)((card - handSize/2.0 + 0.5) * Card.WIDTH * 1.5*Math.sin(angle)),
					     HEIGHT/2 - Card.HEIGHT/2 + (int)(Math.sin(angle)*Card.HEIGHT*1.7)
				    - (int)((card - handSize/2.0 + 0.5) * Card.WIDTH * 1.5*Math.cos(angle)));
	}

	private PointF getAttackPosition(int player, int card, int handSize) {
		double angle = Math.PI*2*(player-this.player)/players.size()+Math.PI/2;
		return new PointF(WIDTH/2f - Card.WIDTH/2f + (int)(Math.cos(angle)*Card.HEIGHT*0.7)
					 + (int)((card - handSize/2.0 + 0.5) * Card.WIDTH * 1.5*Math.sin(angle)),
				          HEIGHT/2f - Card.HEIGHT/2f + (int)(Math.sin(angle)*Card.HEIGHT*0.7)
					 - (int)((card - handSize/2.0 + 0.5) * Card.WIDTH * 1.5*Math.cos(angle)));
	}

	private PointF getNamePosition(int player) {
		double angle = Math.PI*2*(player-this.player)/players.size()+Math.PI/2;
		return new PointF(WIDTH/2f - Card.WIDTH/2f + (int)(Math.cos(angle)*Card.HEIGHT*2.7),
				HEIGHT/2f - Card.HEIGHT/2f + (int)(Math.sin(angle)*Card.HEIGHT*2.7) + 100);
	}

	/**
	 * Adds a card to a player.
	 * @param player The player receiving the card.
	 * @param from   Where the card is drawn from.
	 */
	private void addCardToPlayer(int player, int from) {
		addCardToPlayer(0, player, players.get(player).size(), players.get(player).size() + 1, from, true);
	}

	/** Adds a card to a player.
	 * @param player The player receiving the card.
	 * @param from Where the card is drawn from.
	 * @param delayInMillis The time between current time to when the animation will play. */
	private Card addCardToPlayer(int delayInMillis, int player, int cardIndex, int handSize, int from, boolean revealed) {
		Card card = null;

		switch (from) {
			case Turn.FROM_DECK:
				card = Card.deck.remove(0);
				players.get(player).add(card);
				new Animation.CardMoveAnimation(animationManager, delayInMillis, card,
						new PointF(getDeckPosition()),
						new PointF(getCardPosition(player, cardIndex, handSize)));
				break;
			case Turn.FROM_DISCARD_PILE:
				card = Card.discardPile.remove(0);
				players.get(player).add(card);
				new Animation.CardMoveAnimation(animationManager, delayInMillis, card,
						new PointF(getDiscardPilePosition()),
						new PointF(getCardPosition(player, cardIndex, handSize)));
				break;
		}

		assert card != null;

		card.setOwner(player);
		if (card.isRevealed() != revealed)
			new Animation.CardFlipAnimation(animationManager, delayInMillis, card);

		card.setAttackPosition(getAttackPosition(player, cardIndex, handSize));

		card.angle = calculateAngle(player);

		return card;
	}

	/** Returns the angle of cards of given player in degrees. */
	private float calculateAngle(int player) {
		return 360f*(player-this.player)/players.size();
	}

	/** Order given players cards. */
	private void sortCards(int playerIndex, int delay) {
		List<Card> player = players.get(playerIndex);
		for (int i = 0; i < player.size(); i++) {
			Card card = player.get(i);
			new Animation.CardMoveAnimation(animationManager, delay, card, new PointF(card), new PointF(getCardPosition(playerIndex, i, player.size())));
			card.setAttackPosition(getAttackPosition(playerIndex, i, player.size()));
		}
	}

	/** Shows the last battle. */
	private void viewLastBattle() {
		viewingLastBattle = true;
		hideButtons();
		invalidate();
	}

	/** Checks whether the coordinate is inside the preview of the attacker card. */
	private boolean inAttackerCard(float x, float y) {
		Bitmap attacker = lastBattle.getAttacker().getFullSprite();
		return x > 20 && x < 20 + attacker.getWidth()
			&& y > HEIGHT/2f-attacker.getHeight()/2f && y < HEIGHT/2f+attacker.getHeight()/2f;
	}

	/** Checks whether the coordinate is inside the preview of the attacker card. */
	private boolean inDefenderCard(float x, float y) {
		Bitmap defender = lastBattle.getDefender().getFullSprite();
		return x > WIDTH-20-defender.getWidth() && x < WIDTH-20
				&& y > HEIGHT/2f-defender.getHeight()/2f && y < HEIGHT/2f+defender.getHeight()/2f;
	}

	/** Makes the player lose if they ran out of time for the turn. */
	public void onTurnTimeEnd() {
		for (Card card: players.get(player)) {
			Card.discard(this, animationManager, card);
		}
		players.get(player).clear();
		lastTurnReference.set(Turn.emptyTurn(player));
	}
}
