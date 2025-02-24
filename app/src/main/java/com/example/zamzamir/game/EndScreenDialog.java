package com.example.zamzamir.game;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zamzamir.R;

public class EndScreenDialog extends Dialog {
	public EndScreenDialog(@NonNull Context context, int rank, Runnable end) {
		super(context);
		setContentView(R.layout.dialog_end_screen);

		if (rank != 1) {
			TextView text = findViewById(R.id.gameEndText);
			text.setText(context.getString(R.string.game_end_text_for_loser, rank));
		}

		findViewById(R.id.homeButton).setOnClickListener(v -> end.run());
	}
}
