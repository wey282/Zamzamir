package com.example.zamzamir;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.authentication.GameUser;
import com.example.zamzamir.match_making.Room;
import com.example.zamzamir.match_making.WaitingRoomActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

	private final FirebaseAuth AUTH = FirebaseAuth.getInstance();
	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private CollectionReference WAITING_ROOM;

	private GameUser user;
	private TextView nameTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeCollections();

		initializeButtons();

		nameTextView = findViewById(R.id.nameTextView);

	}

	/** Adds the appropriate click listener to all buttons. */
	private void initializeButtons() {
		findViewById(R.id.signoutButton).setOnClickListener(v -> signOut());
		findViewById(R.id.playButton).setOnClickListener(v -> findRoom());
	}

	/** Initializes all the collection references. */
	private void initializeCollections() {
		CollectionReference USER_COLLECTION = DB.collection(getString(R.string.user_collection));

		if (AUTH.getUid() != null)
			USER_COLLECTION.document(AUTH.getUid()).get().addOnSuccessListener(this::onGetUser);

		WAITING_ROOM = DB.collection(getString(R.string.waiting_room_collection));
	}

	/** Finds a waiting room, or creates one if none are available, and then joins it. */
	private void findRoom() {
		findRoom(this, WAITING_ROOM, user);
	}

	/** Finds a waiting room, or creates one if none are available, and then joins it. */
	public static void findRoom(Context context, CollectionReference WAITING_ROOM, GameUser user) {
		WAITING_ROOM.get().addOnSuccessListener(querySnapshot -> {

			// Search in all waiting rooms and joins the first non-full room.
			for (DocumentSnapshot room : querySnapshot) {
				Boolean full = room.getBoolean(context.getString(R.string.full));
				if (full != null && !full) {
					joinRoom(context, room, user);
					return;
				}
			}
			// After not finding a room, create one and join it.
			WAITING_ROOM.add(new Room(false)).addOnSuccessListener(snapshot -> snapshot.get().addOnSuccessListener(documentSnapshot -> joinRoom(context, documentSnapshot, user)));
		});
	}

	/** Joins given waiting room. */
	private void joinRoom(DocumentSnapshot room) {
		joinRoom(this, room, user);
	}

	/** Joins given waiting room. */
	public static void joinRoom(Context context, DocumentSnapshot room,  GameUser user) {
		DocumentReference roomReference = room.getReference();
		CollectionReference playerInRoom = roomReference.collection(context.getString(R.string.players_in_room));

		playerInRoom.document(user.getId()).set(user).addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				playerInRoom.get().addOnCompleteListener(getPlayersTask -> {
					if (getPlayersTask.isSuccessful()) {
						QuerySnapshot players = getPlayersTask.getResult();
						if (players.size() == 4) {
							Room roomObj = room.toObject(Room.class);
							assert roomObj != null;
							roomObj.setFull(true);
							roomReference.set(roomObj);
						}
						StaticUtils.moveActivity(context, WaitingRoomActivity.class, context.getString(R.string.room_id_extra), room.getId());
					}
				});
			}
		});
	}

	/** Logs current user out and returns to sign in/up. */
	private void signOut() {
		AUTH.signOut();
		finish();
	}

	/** Method called upon finding current user's info, sets user accordingly. */
	private void onGetUser(DocumentSnapshot documentSnapshot) {
		user = documentSnapshot.toObject(GameUser.class);
		assert user != null;
		nameTextView.setText(user.getUsername());
	}
}