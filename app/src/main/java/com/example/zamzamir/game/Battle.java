package com.example.zamzamir.game;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Battle {
	@NonNull
	private final Card attacker;
	@NonNull
	private final Card defender;

	private final int attackerRoll;
	private final int defenderRoll;

	public Battle(@NonNull Card attacker, @NonNull Card defender, int attackerRoll, int defenderRoll) {
		this.attacker = attacker;
		this.defender = defender;
		this.attackerRoll = attackerRoll;
		this.defenderRoll = defenderRoll;
	}

	@NonNull
	public Card getAttacker() {
		return attacker;
	}

	@NonNull
	public Card getDefender() {
		return defender;
	}

	public int getAttackerRoll() {
		return attackerRoll;
	}

	public int getDefenderRoll() {
		return defenderRoll;
	}
}
