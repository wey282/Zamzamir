package com.example.zamzamir;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.graphics.PointF;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.example.zamzamir.game.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** Houses static general use methods */
public class StaticUtils {
	/** Moves from @from to @to. */
	public static void moveActivity(Context from, Class<? extends Activity> to) {
		from.startActivity(new Intent(from, to));
	}

	/** Moves from @from to @to, uses extra as an extra. */
	public static void moveActivity(Context from, Class<? extends Activity> to, String extraName, String extraValue) {
		Intent intent = new Intent(from, to);
		intent.putExtra(extraName, extraValue);
		from.startActivity(intent);
	}

	/** Moves from @from to @to, with given extras. */
	public static void moveActivity(Context from, Class<? extends Activity> to, Map<String, String> extras) {
		Intent intent = new Intent(from, to);
		extras.forEach(intent::putExtra);
		from.startActivity(intent);
	}

	/** Sets the phone orientation to horizontal. */
	public static void setOrientationToHorizontal(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/** Hides keyboard for given dialog */
	public static void hideKeyboard(Dialog dialog) {
		Context context = dialog.getContext();
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (dialog.getCurrentFocus() != null)
			imm.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), 0);
	}

	/** Counts how many cards are in the xml file. */
	public static int countCards(Context context) {
		int cardCount = 0;

		try {
			// Open the XML resource
			XmlResourceParser parser = context.getResources().getXml(R.xml.cards);

			// Start parsing the XML
			int eventType = parser.getEventType();
			while (eventType != XmlResourceParser.END_DOCUMENT) {
				if (eventType == XmlResourceParser.START_TAG && parser.getName().equals("card")) {
					cardCount++; // Count every <card> tag
				}
				eventType = parser.next(); // Move to the next element
			}
			parser.close(); // Close parser
		} catch (Exception e) {
			Log.e("XML", "Error parsing XML", e);
		}

		return cardCount;
	}

	/** Creates a template for shuffled cards. */
	public static List<Integer> createShuffle() {
		Card.resetDeck();
		int cardCount = Card.deck.size();

		// Create an array containing indexes from 0 to card count
		List<Integer> shuffle = new ArrayList<>();
		for (int i = 0; i < cardCount; i++)
			shuffle.add(i);

		// Shuffle the indexes
		for (int i = 0; i < cardCount; i++) {
			int temp = shuffle.get(i);
			int oIndex = (int)(Math.random()*cardCount);
			shuffle.set(i, shuffle.get(oIndex));
			shuffle.set(oIndex, temp);
		}

		return shuffle;
	}

	public static PointF lerp(PointF a, PointF b, float progress) {
		return new PointF(a.x*(1-progress) + b.x*progress, a.y*(1-progress) + b.y*progress);
	}
}
