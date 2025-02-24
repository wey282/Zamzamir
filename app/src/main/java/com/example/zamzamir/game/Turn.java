package com.example.zamzamir.game;

import androidx.annotation.Discouraged;
import androidx.annotation.RequiresPermission;
import androidx.annotation.VisibleForTesting;

public class Turn {

	public static final int EMPTY = -1;
	public static final int DRAW = 0;
	public static final int ATTACK = 1;
	public static final int SKIP = 2;
	public static final int RECEIVE = 3;
	public static final int DISCARD = 4;

	public static final int FROM_DECK = 0;
	public static final int FROM_DISCARD_PILE = 1;

	private int type;
	private int from;
	private int selectedCard;
	private int targetPlayer;
	private int targetCard;
	private int player;
	private int attackerRoll;
	private int defenderRoll;

	/** Creates empty turn. */
	public static Turn emptyTurn(int player) {
		Turn turn = new Turn();
		turn.player = player;
		turn.type = EMPTY;
		return turn;
	}

	/** Creates a skip turn. */
	public static Turn skipTurn(int player) {
		Turn turn = new Turn();
		turn.player = player;
		turn.type = SKIP;
		return turn;
	}

	/** Creates a turn consisting of receiving a card. */
	public static Turn receiveTurn(int player, int from) {
		Turn turn = new Turn();
		turn.player = player;
		turn.from = from;
		turn.type = RECEIVE;
		return turn;
	}

	/** Creates a turn consisting of drawing a card. */
	public static Turn drawTurn(int player, int selectedCard, int from) {
		Turn turn = new Turn();
		turn.player = player;
		turn.selectedCard = selectedCard;
		turn.from = from;
		turn.type = DRAW;
		return turn;
	}

	/** Creates a turn where a player drew a card and decided to discard it. */
	public static Turn discardTurn(int player) {
		Turn turn = new Turn();
		turn.player = player;
		turn.type = DISCARD;
		return turn;
	}

	/** Creates an attack turn. */
	public static Turn attackTurn(int player, int selectedCard, int targetPlayer, int targetCard) {
		Turn turn = new Turn();
		turn.player = player;
		turn.type = ATTACK;
		turn.selectedCard = selectedCard;
		turn.targetPlayer = targetPlayer;
		turn.targetCard = targetCard;
		turn.attackerRoll = (int)(Math.random()*6)+1;
		turn.defenderRoll = (int)(Math.random()*6)+1;
		return turn;
	}

	/** DO NOT USE
	 *  Exists for Firebase serialization
	 *  Use one of these instead: Turn.emptyTurn(), Turn.skipTurn(), Turn.receiveTurn(), Turn.drawTurn(), Turn.attackTurn()
	 *  */
	@VisibleForTesting
	public Turn() {}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSelectedCard() {
		return selectedCard;
	}

	public void setSelectedCard(int selectedCard) {
		this.selectedCard = selectedCard;
	}

	public int getTargetPlayer() {
		return targetPlayer;
	}

	public void setTargetPlayer(int targetPlayer) {
		this.targetPlayer = targetPlayer;
	}

	public int getTargetCard() {
		return targetCard;
	}

	public void setTargetCard(int targetCard) {
		this.targetCard = targetCard;
	}

	public int getPlayer() {
		return player;
	}

	public int getDefenderRoll() {
		return defenderRoll;
	}

	public int getAttackerRoll() {
		return attackerRoll;
	}
}
