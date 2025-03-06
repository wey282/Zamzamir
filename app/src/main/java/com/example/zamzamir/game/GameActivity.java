package com.example.zamzamir.game;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.match_making.GameRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class GameActivity extends AppCompatActivity {
	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference lastTurn;

	private GameView gameView;
	private Button skipButton;
	private Button attackButton;

	private int player;

	private boolean started = false;

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

		findButtons();

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));

		player = Integer.parseInt(getIntent().getStringExtra(getString(R.string.current_player_extra)));

		if (roomID == null)
			throw new RuntimeException();


		DocumentReference game = DB.collection(getString(R.string.games_collection)).document(roomID);
		game.addSnapshotListener(this::start);

		lastTurn = DB.collection(getString(R.string.last_turn_collection)).document(roomID);

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
		lastTurn.addSnapshotListener((documentSnapshot, exception) -> {
			if (documentSnapshot != null)
				gameView.onTurn(documentSnapshot);
		});
	}

	/** Starts the game. */
	private void start(DocumentSnapshot gameSnapshot, FirebaseFirestoreException exception) {

		if (gameSnapshot == null || started)
			return;

		GameRoom gameRoom = gameSnapshot.toObject(GameRoom.class);

		if (gameRoom == null)
			return;

		started = true;

		Card.resetDeck();
		Card.shuffle(gameRoom.getShuffle());
		gameView.start(gameRoom.getPlayerCount(), player, skipButton, attackButton, lastTurn, this::showGameEndScreen);
	}

	private void showGameEndScreen(int rank) {
		gameView.post(() ->
			new EndScreenDialog(this, rank, this::finish).show());
	}
}