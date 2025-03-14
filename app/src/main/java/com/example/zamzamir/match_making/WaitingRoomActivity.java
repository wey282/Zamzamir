package com.example.zamzamir.match_making;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
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
import com.example.zamzamir.game.GameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class WaitingRoomActivity extends AppCompatActivity {

	private static final int timePerPlayerInSeconds = 10;

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference roomDocument;
	private CollectionReference playersReference;
	private CollectionReference readyReference;
	private ListenerRegistration roomListener;
	private ListenerRegistration playersListener;
	private ListenerRegistration readyListener;

	private TextView[] playersTextViews;
	private TextView timeRemainingTextView;
	private Button readyButton;

	private final Ready ready = new Ready();
	private boolean host = false;

	private Room room;

	private List<GameUser> players;

	private int playerCount;

	private boolean started;

	private String playerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_waiting_room);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		findViews();

		readyButton.setOnClickListener(this::onReady);

		// Find player's id
		playerId = FirebaseAuth.getInstance().getUid();

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));
		assert roomID != null;
		roomDocument = DB.collection(getString(R.string.waiting_room_collection)).document(roomID);
		// Listen for changes in the room
		roomListener = roomDocument.addSnapshotListener(this::onRoomChange);
		// Listen for changes in the players
		playersReference = roomDocument.collection(getString(R.string.players_in_room));
		playersListener = playersReference.addSnapshotListener(this::onPlayerChange);
		readyReference = roomDocument.collection(getString(R.string.ready_in_room));
		readyListener = readyReference.addSnapshotListener(this::onReadyChange);
		readyReference.document(playerId).set(ready);


	}

	/** Finds all relevant views in the activity and assigns the correct variables value. */
	private void findViews() {
		playersTextViews = new TextView[4];
		playersTextViews[0] = findViewById(R.id.player1TextView);
		playersTextViews[1] = findViewById(R.id.player2TextView);
		playersTextViews[2] = findViewById(R.id.player3TextView);
		playersTextViews[3] = findViewById(R.id.player4TextView);

		readyButton = findViewById(R.id.readyButton);

		timeRemainingTextView = findViewById(R.id.remainingTimeTextView);
	}

	/** Triggers when the ready button is clicked.
	 *  Updates the ready players count and changes ready buttons text. */
	private void onReady(View view) {
		ready.flip();
		if (ready.isReady()) {
			readyButton.setText(R.string.unready_text);
		}
		else {
			readyButton.setText(R.string.ready_text);;
		}
		readyReference.document(playerId).set(ready);
	}

	/** Updates room object and starts the game if enough players are ready. */
	private void onRoomChange(DocumentSnapshot room, FirebaseFirestoreException exception) {
		if (room != null && !started) {
			this.room = room.toObject(Room.class);

			if (this.room != null && this.room.getRemainingTime() < 30) {
				timeRemainingTextView.post(() -> {
					timeRemainingTextView.setVisibility(View.VISIBLE);
					timeRemainingTextView.setText(getString(R.string.int_text, Math.round(this.room.getRemainingTime())));
				});
			}
			else  {
				timeRemainingTextView.post(() -> timeRemainingTextView.setVisibility(View.GONE));
			}
		}
	}

	private void onPlayerChange(QuerySnapshot playersSnapshot, FirebaseFirestoreException exception) {
		if (started && host && playersSnapshot.isEmpty())
			roomDocument.delete();
		if (playersSnapshot == null || started)
			return;
		players = playersSnapshot.toObjects(GameUser.class);
		playerCount = players.size();

		room.setRemainingTime(timePerPlayerInSeconds*(5-players.size()));

		if (playerCount == 1) {
			host = true;
			readyButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.disabled_button)));
			readyButton.setEnabled(false);
			decreaseRemainingTime();
		}
		else {
			readyButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.enabled_button)));
			readyButton.setEnabled(true);
		}
		for (int i = 0; i < 4; i++) {
			if (playerCount-1 < i)
				playersTextViews[i].setText("");
			else
				playersTextViews[i].setText(players.get(i).getUsername());
		}
	}

	private void decreaseRemainingTime() {
		new Thread(() -> {
			long prevTime, time = System.currentTimeMillis();

			while (!started && room.getRemainingTime() > 0) {
				prevTime = time;
				time = System.currentTimeMillis();
				if (room.getRemainingTime() <= 3*timePerPlayerInSeconds) {
					room.setRemainingTime(room.getRemainingTime() - (time - prevTime) / 1000f);
					roomDocument.set(room);
				}
				SystemClock.sleep(100);
			}

			if (!started) {
				for (GameUser user: players) {
					readyReference.document(user.getId()).set(new Ready(true));
				}
			}

		}).start();
	}

	private void onReadyChange(QuerySnapshot documentSnapshots, FirebaseFirestoreException exception) {
		if (documentSnapshots == null)
			return;

		List<Ready> readyList = documentSnapshots.toObjects(Ready.class);

		int readyCount = 0;

		for (int i = 0; i < readyList.size(); i++) {
			if (readyList.get(i).isReady()) {
				playersTextViews[i].setTextColor(getColor(R.color.ready_text));
				readyCount++;
			}
			else
				playersTextViews[i].setTextColor(getColor(R.color.unready_text));
		}

		if (playerCount > 1 && readyCount == playerCount)
			startGame();
	}

	@Override
	protected void onStop() {
		roomListener.remove();
		playersListener.remove();
		readyListener.remove();
		if (!isChangingConfigurations() && !started) {
			readyReference.document(playerId).delete();
			if (room != null && ready.isReady()) {
				roomDocument.set(room);
			}
			playersReference.document(playerId).delete();
			if (playerCount <= 1) {
				roomDocument.delete();
			}
		}
		super.onStop();
	}

	/** Starts the game. */
	private void startGame() {
		started = true;
		// Create game room
		CollectionReference games = DB.collection(getString(R.string.games_collection));
		DocumentReference game = games.document(roomDocument.getId());
		if (host) {
			game.set(new GameRoom(playerCount));
		}

		for (GameUser user : players) {
			game.collection(getString(R.string.players_in_room)).add(user);

			playersReference.document(user.getId()).delete();
			readyReference.document(user.getId()).delete();
		}

		// Create relevant extras
		HashMap<String, String> extras = new HashMap<>();
		extras.put(getString(R.string.room_id_extra), roomDocument.getId());
		extras.put(getString(R.string.player_id_extra), playerId);
		extras.put(getString(R.string.host), Boolean.toString(host));

		// Start game activity
		StaticUtils.moveActivity(this, GameActivity.class, extras);

		// Stop this activity
		finish();
	}
}