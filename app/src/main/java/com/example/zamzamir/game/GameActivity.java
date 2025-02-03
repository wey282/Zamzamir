package com.example.zamzamir.game;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.example.zamzamir.match_making.GameRoom;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GameActivity extends AppCompatActivity {

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference game;
	private DocumentReference lastMove;

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

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));

		if (roomID == null)
			throw new RuntimeException();

		game = DB.collection(getString(R.string.games_collection)).document(roomID);
		game.get().addOnSuccessListener(this::start);

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
	}

	private void start(DocumentSnapshot game) {
		GameRoom gameRoom = game.toObject(GameRoom.class);

		if (gameRoom == null)
			return;

		Card.shuffle(gameRoom.getShuffle());
		gameView.start(gameRoom.getPlayerCount(), player, skipButton, attackButton);
	}
}