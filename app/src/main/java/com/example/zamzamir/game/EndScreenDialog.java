package com.example.zamzamir.game;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zamzamir.MainActivity;
import com.example.zamzamir.R;
import com.example.zamzamir.authentication.GameUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EndScreenDialog extends Dialog {
	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private final Runnable end;

	public EndScreenDialog(@NonNull Context context, int rank, Runnable end) {
		super(context);
		setContentView(R.layout.dialog_end_screen);

		this.end = end;

		if (rank != 1) {
			TextView text = findViewById(R.id.gameEndText);
			text.setText(context.getString(R.string.game_end_text_for_loser, rank));
		}

		CollectionReference USER_COLLECTION = DB.collection(context.getString(R.string.user_collection));

		final FirebaseAuth AUTH = FirebaseAuth.getInstance();
		if (AUTH.getUid() != null)
			USER_COLLECTION.document(AUTH.getUid()).get().addOnSuccessListener(this::onGetUser);

		setCancelable(false);
		setCanceledOnTouchOutside(false);
	}

	private void onGetUser(DocumentSnapshot documentSnapshot) {
		findViewById(R.id.homeButton).setOnClickListener(v -> end.run());
		final CollectionReference WAITING_ROOM = DB.collection(getContext().getString(R.string.waiting_room_collection));

		GameUser user = documentSnapshot.toObject(GameUser.class);

		findViewById(R.id.playAgainButton).setOnClickListener(v -> MainActivity.findRoom(getContext(), WAITING_ROOM, user));
	}
}
