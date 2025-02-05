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

import com.example.zamzamir.MainActivity;
import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.example.zamzamir.authentication.GameUser;
import com.example.zamzamir.match_making.GameRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class GameActivity extends AppCompatActivity {

	private final FirebaseAuth AUTH = FirebaseAuth.getInstance();

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference game;
	private DocumentReference lastMove;

	private GameView gameView;
	private Button skipButton;
	private Button attackButton;

	private int player;

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

		player = Integer.parseInt(getIntent().getStringExtra(getString(R.string.current_player_extra)));

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

	/** Starts the game. */
	private void start(DocumentSnapshot gameSnapshot) {
		GameRoom gameRoom = gameSnapshot.toObject(GameRoom.class);

		if (gameRoom == null)
			return;

		Card.shuffle(gameRoom.getShuffle());
		gameView.start(gameRoom.getPlayerCount(), player, skipButton, attackButton);
	}
}