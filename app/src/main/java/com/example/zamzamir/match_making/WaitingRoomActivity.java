package com.example.zamzamir.match_making;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Objects;

public class WaitingRoomActivity extends AppCompatActivity {

	private static final int UNINITIALIZED_VALUE = -1;

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference roomDocument;
	private CollectionReference playersReference;
	private ListenerRegistration roomListener;
	private ListenerRegistration playersListener;

	private TextView[] playersTextViews;
	private Button readyButton;

	private boolean ready = false;
	private boolean host = false;

	private Room room;

	private int playerCount;

	private boolean started;

	// Which is the player is the current user
	private int player = UNINITIALIZED_VALUE;

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

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));
		assert roomID != null;
		roomDocument = DB.collection(getString(R.string.waiting_room_collection)).document(roomID);
		// Listen for changes in the room
		roomListener = roomDocument.addSnapshotListener(this::onRoomChange);
		// Listen for changes in the players
		playersReference = roomDocument.collection(getString(R.string.players_in_room));
		playersListener = playersReference.addSnapshotListener(this::onPlayerChange);

	}

	/** Finds all relevant views in the activity and assigns the correct variables value. */
	private void findViews() {
		playersTextViews = new TextView[4];
		playersTextViews[0] = findViewById(R.id.player1TextView);
		playersTextViews[1] = findViewById(R.id.player2TextView);
		playersTextViews[2] = findViewById(R.id.player3TextView);
		playersTextViews[3] = findViewById(R.id.player4TextView);

		readyButton = findViewById(R.id.readyButton);
	}

	/** Triggers when the ready button is clicked.
	 *  Updates the ready players count and changes ready buttons text. */
	private void onReady(View view) {
		ready = !ready;
		if (ready) {
			readyButton.setText(R.string.unready_text);
			room.ready++;
		}
		else {
			readyButton.setText(R.string.ready_text);
			room.ready--;
		}
		roomDocument.set(room);
	}

	/** Updates room object and starts the game if enough players are ready. */
	private void onRoomChange(DocumentSnapshot room, FirebaseFirestoreException exception) {
		if (room != null && !started) {
			this.room = room.toObject(Room.class);
			int readyCount = this.room != null ? this.room.ready : 0;
			if (readyCount == playerCount && playerCount > 1) {
				startGame();
			}
		}
	}

	private void onPlayerChange(QuerySnapshot playersSnapshot, FirebaseFirestoreException exception) {
		if (started && host && playersSnapshot.isEmpty())
			roomDocument.delete();
		if (playersSnapshot == null || started)
			return;
		List<GameUser> players = playersSnapshot.toObjects(GameUser.class);
		playerCount = players.size();


		if (player == UNINITIALIZED_VALUE)
			player = playerCount - 1;

		if (players.size() == 1) {
			host = true;
			readyButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.disabled_button)));
			readyButton.setEnabled(false);
		}
		else {
			readyButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.enabled_button)));
			readyButton.setEnabled(true);
		}
		for (int i = 0; i < 4; i++) {
			if (players.size()-1 < i)
				playersTextViews[i].setText("");
			else
				playersTextViews[i].setText(players.get(i).getUsername());
		}
	}

	@Override
	protected void onStop() {
		roomListener.remove();
		playersListener.remove();
		if (!isChangingConfigurations() && !started) {
			if (room != null && ready) {
				room.ready--;
				roomDocument.set(room);
			}
			playersReference.document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).delete();
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

		// Create relevant extras
		HashMap<String, String> extras = new HashMap<>();
		extras.put(getString(R.string.room_id_extra), roomDocument.getId());
		extras.put(getString(R.string.current_player_extra), Integer.toString(player));

		// Start game activity
		StaticUtils.moveActivity(this, GameActivity.class, extras);

		playersReference.document(FirebaseAuth.getInstance().getUid()).delete();

		// Stop this activity
		finish();
	}
}