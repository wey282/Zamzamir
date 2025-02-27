package com.example.zamzamir.game;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.VisibleForTesting;

import com.example.zamzamir.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Animation {
	private static final float TARGET_FPS = 60;
	private static final int MIN_SLEEP_TIME = 10;

	private static final List<Animation> animations = new ArrayList<>();

	private static boolean running = false;

	private final long start;
	private final int length;

	protected long curTime;
	protected float progress;

	private Runnable onFinish;

	public Animation(AnimationManager manager, int delay, int length) {
		start = System.currentTimeMillis()+delay;
		this.length = length;

		animations.add(this);

		if (!running)
			manager.startThread(this);
	}

	/** Processes a frame of the animation.
	 * @param delta time between frames in seconds.
	 * @return If animation should be played. */
	public boolean play(float delta) {
		curTime = System.currentTimeMillis();
		progress = Math.min((float)(curTime-start)/length, 1);
		if (start + length <= curTime) {
			animations.remove(this);
			if (onFinish != null)
				onFinish.run();
		}
		return start <= curTime;
	}

	public void setOnFinish(Runnable onFinish) {
		this.onFinish = onFinish;
	}

	/** Manages the animations for given view. */
	public static class AnimationManager {
		private final View view;

		public AnimationManager(View view) {
			this.view = view;
		}

		/** Starts a thread running at TARGET_FPS, that runs animations.
		 *  Should only be called from an animation. */
		@VisibleForTesting
		protected void startThread(Object caller) {

			if (!(caller instanceof Animation))
				return;

			running = true;

			new Thread(() -> {
				long prevTime;
				SystemClock.sleep((int)(1000/TARGET_FPS));
				long time = System.currentTimeMillis();

				while (!animations.isEmpty()) {
					prevTime = time;
					time = System.currentTimeMillis();
					float delta = (time-prevTime)/1000f;

					for (int i = 0; i < animations.size(); i++) {
						animations.get(i).play(delta);
					}

					view.invalidate();

					SystemClock.sleep(Math.max((int)(2000/TARGET_FPS-delta*1000), MIN_SLEEP_TIME));
				}
				running = false;
			}).start();
		}
	}

	public static class CardMoveAnimation extends Animation {
		public static final int length = 400;
		private final PointF from, to;
		private final Card card;

		public CardMoveAnimation(AnimationManager manager, int delay, Card card, PointF from, PointF to) {
			super(manager, delay, length);

			this.card = card;
			this.from = from;
			this.to = to;
		}

		public CardMoveAnimation(AnimationManager manager, int delay, int length, Card card, PointF from, PointF to) {
			super(manager, delay, length);

			this.card = card;
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean play(float delta) {
			if (!super.play(delta))
				return false;

			PointF position = StaticUtils.lerp(from, to, progress);
			card.x = (int)position.x;
			card.y = (int)position.y;
			return true;
		}
	}

	public static class CardFlipAnimation extends Animation {
		private static final int length = 400;

		private final Card card;

		private final boolean revealedAtStart;

		/** Creates an animation that flips a card. */
		public CardFlipAnimation(AnimationManager manager, int delay, Card card) {
			super(manager, delay, length);

			this.card = card;
			revealedAtStart = card.isRevealed();
		}

		@Override
		public boolean play(float delta) {
			if (!super.play(delta))
				return false;

			if (progress > 0.5) {
				card.setRevealed(!revealedAtStart);
			}

			Bitmap bitmap = Bitmap.createBitmap(card.isRevealed() ? card.getSprite() : Card.back, 0, 0, getWidth(), Card.HEIGHT);
			card.setActiveSprite(bitmap);
			card.setxOffset(Card.WIDTH/2-getWidth()/2);
			
			return true;
		}

		/** Returns the current width. */
		private int getWidth() {
			return Math.max((int)(Card.WIDTH*Math.abs(progress-0.5)*2), 1);
		}
	}

	public static class AttackAnimation {

		public static final int attackLength = 100;
		public static final int stayLength = 1000;
		public static final int retreatLength = 600;
		public static final int totalLength = attackLength + stayLength + retreatLength;

		public static void play(AnimationManager manager, int delay, Card attacker, Card attacked) {
			PointF start = new PointF(attacker);
			new CardMoveAnimation(manager, delay, attackLength, attacker, start, attacked.getAttackPosition()).setOnFinish(() ->
					new CardMoveAnimation(manager, stayLength, retreatLength, attacker, new PointF(attacker), start));
		}
	}
}
