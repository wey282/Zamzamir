package com.example.zamzamir;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;


/** Houses static general use methods */
public class StaticUtils {
	/** Moves from @from to @to */
	public static void moveActivity(Context from, Class<? extends Activity> to) {
		from.startActivity(new Intent(from, to));
	}

	/** Moves from @from to @to, uses extra as an extra */
	public static void moveActivity(Context from, Class<? extends Activity> to, String extraName, String extraValue) {
		Intent intent = new Intent(from, to);
		intent.putExtra(extraName, extraValue);
		from.startActivity(intent);
	}

	/** Sets the phone orientation to horizontal */
	public static void setOrientationToHorizontal(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public static void hideKeyboard(Activity a) {
		InputMethodManager m = (InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE);

		View focus = a.getCurrentFocus();

		if (m != null && focus != null)
			m.hideSoftInputFromWindow(focus.getWindowToken(), 0);
	}

	public static void hideKeyboard(Dialog dialog) {
		Context context = dialog.getContext();
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (dialog.getCurrentFocus() != null)
			imm.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), 0);
	}
}
