package com.example.zamzamir.match_making;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.example.zamzamir.authentication.GameUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class WaitingRoomActivity extends AppCompatActivity {

	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private DocumentReference roomDocument;
	private ListenerRegistration roomListener;
	private ListenerRegistration playersListener;

	private TextView[] playersTextViews;
	private Button readyButton;

	private boolean ready = false;

	private Room room;

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
		StaticUtils.setOrientationToHorizontal(this);

		findViews();

		readyButton.setOnClickListener(this::onReady);

		String roomID = getIntent().getStringExtra(getString(R.string.room_id_extra));
		assert roomID != null;
		roomDocument = DB.collection(getString(R.string.waiting_room)).document(roomID);
		// Listen for changes in the room
		roomListener = roomDocument.addSnapshotListener(this::onRoomChange);
		// Listen for changes in the players
		playersListener = roomDocument.collection(getString(R.string.players_in_room)).addSnapshotListener(this::onPlayerChange);
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
			readyButton.setText(R.string.unready);
			room.ready++;
		}
		else {
			readyButton.setText(R.string.ready);
			room.ready--;
		}
		roomDocument.set(room);
	}

	private void onRoomChange(DocumentSnapshot room, FirebaseFirestoreException exception) {
		if (room != null)
			this.room = room.toObject(Room.class);
	}

	private void onPlayerChange(QuerySnapshot playersSnapshot, FirebaseFirestoreException exception) {
		List<GameUser> players = playersSnapshot.toObjects(GameUser.class);
		for (int i = 0; i < 4; i++) {
			if (players.size()-1 < i)
				playersTextViews[i].setText("");
			else
				playersTextViews[i].setText(players.get(i).getUsername());
		}
	}

	@Override
	protected void onDestroy() {
		roomListener.remove();
		playersListener.remove();
		super.onDestroy();
	}
}