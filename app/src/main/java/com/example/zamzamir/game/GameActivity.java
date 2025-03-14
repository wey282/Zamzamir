package com.example.zamzamir.game;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.example.zamzamir.authentication.GameUser;
import com.example.zamzamir.match_making.GameRoom;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class GameActivity extends AppCompatActivity {
	private static final long TURN_LENGTH = 1000*90;

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference lastTurn;
	private DocumentReference gameRef;
	private CollectionReference playersRef;

	private GameRoom gameRoom;
	private List<GameUser> players;

	private GameView gameView;
	private Button skipButton;
	private Button attackButton;
	private Button lastBattleButton;
	private TextView timeRemainingTextView;

	private boolean started = false;
	private boolean host;

	private String playerID;

	private CountDownTimer countDownTimer;

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
		timeRemainingTextView = findViewById(R.id.timeRemainingTextView);

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));
		playerID = getIntent().getStringExtra(getString(R.string.player_id_extra));
		host = Boolean.parseBoolean(getIntent().getStringExtra(getString(R.string.host)));

		if (roomID == null)
			throw new RuntimeException();


		gameRef = DB.collection(getString(R.string.games_collection)).document(roomID);
		playersRef = gameRef.collection(getString(R.string.players_in_room));
		playersRef.addSnapshotListener(this::setPlayers);
		gameRef.addSnapshotListener(this::setGameRoom);

		lastTurn = DB.collection(getString(R.string.last_turn_collection)).document(roomID);

		initGameView();
	}

	/** Finds all the buttons. */
	private void findButtons() {
		skipButton = findViewById(R.id.skipButton);
		attackButton = findViewById(R.id.attackButton);
		lastBattleButton = findViewById(R.id.lastBattleButton);
	}

	/** Finds the game-view and starts it. */
	private void initGameView() {
		gameView = findViewById(R.id.gameView);
		lastTurn.addSnapshotListener((documentSnapshot, exception) -> {
			if (documentSnapshot != null)
				gameView.onTurn(documentSnapshot);
		});
	}

	/** Updates the players and starts the game if gameRoom is already available. */
	private void setPlayers(QuerySnapshot documentSnapshots, FirebaseFirestoreException exception) {
		if (documentSnapshots != null) {
			players = documentSnapshots.toObjects(GameUser.class);
			if (gameRoom != null && exception == null && !players.isEmpty())
				start();
		}
	}

	/** Updates the players and starts the game if players are already available. */
	private void setGameRoom(DocumentSnapshot documentSnapshot, FirebaseFirestoreException exception) {
		if (documentSnapshot != null) {
			gameRoom = documentSnapshot.toObject(GameRoom.class);
			if (players != null && gameRoom != null && exception == null)
				start();
		}
	}

	/** Starts the game. */
	private void start() {
		if (started)
			return;

		int player = calculateIdentity();
		orderPlayers();

		started = true;

		Card.resetDeck();
		Card.shuffle(gameRoom.getShuffle());
		gameView.start(players, player, skipButton, attackButton, lastBattleButton, lastTurn, this::showGameEndScreen, this::startCountDown, this::stopCountDown);
	}

	private void startCountDown() {
		countDownTimer = new CountDownTimer(TURN_LENGTH, 100) {

			@Override
			public void onTick(long millisUntilFinished) {
				long totalSeconds = millisUntilFinished / 1000;
				long minutes = totalSeconds / 60;
				long seconds = totalSeconds % 60;

				timeRemainingTextView.post(() ->
						timeRemainingTextView.setText(getString(R.string.time_left, minutes, seconds)));
			}

			@Override
			public void onFinish() {
				gameView.onTurnTimeEnd();
			}
		};
		countDownTimer.start();
	}

	private void stopCountDown() {
		countDownTimer.cancel();
	}

	/** Sorts the players based on play order. */
	private void orderPlayers() {
		players = StaticUtils.applyShuffle(players, gameRoom.getOrder());
	}

	/** Calculates which player this is. */
	private int calculateIdentity() {
		int index = -1;

		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getId().equals(playerID)) {
				index = i;
				break;
			}
		}

		return gameRoom.getOrder().get(index);
	}

	/** Displays the game end screen. */
	private void showGameEndScreen(int rank) {
		gameView.post(() ->
			new EndScreenDialog(this, rank, this::finish).show());
	}

	@Override
	protected void onStop() {
		super.onStop();
		playersRef.document(playerID).delete();
		if (host) {
			gameRef.delete();
			lastTurn.delete();
		}
	}
}