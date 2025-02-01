package com.example.zamzamir.game;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;

public class GameActivity extends AppCompatActivity {

	private GameView gameView;
	private Button skipButton;
	private Button attackButton;

	private int player = 0;
	private int playerCount = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_game);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		StaticUtils.setOrientationToHorizontal(this);

		findButtons();

		initGameView();
	}

	/** Finds all the buttons. */
	private void findButtons() {
		skipButton = findViewById(R.id.skipButton);
		attackButton = findViewById(R.id.attackButton);
	}

	/** Finds the game-view and starts it. */
	private void initGameView() {
		gameView = findViewById(R.id.gameView);
		gameView.start(playerCount, player, skipButton, attackButton);
	}


}