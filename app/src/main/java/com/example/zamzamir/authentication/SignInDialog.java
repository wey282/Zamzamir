package com.example.zamzamir.authentication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.zamzamir.*;
import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInDialog extends Dialog {
	private final FirebaseAuth AUTH = FirebaseAuth.getInstance();
	private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
	private final CollectionReference userCollection = DB.collection(getContext().getString(R.string.user_collection));

	private final EditText email;
	private final EditText password;
	private final EditText username;

	public SignInDialog(@NonNull Context context, boolean userExists) {
		super(context);
		setContentView(R.layout.dialog_sign_in);

		email = findViewById(R.id.editTextTextEmailAddress);
		password = findViewById(R.id.editTextTextPassword);
		username = findViewById(R.id.editTextText);
		Button confirmButton = findViewById(R.id.confirmSignInButton);

		// When user presses outside the keyboard, hide it
		findViewById(R.id.signInConstraintLayout).setOnClickListener(v -> StaticUtils.hideKeyboard(this));


		// Sign in
		if (userExists) {
			username.setVisibility(View.INVISIBLE);

			confirmButton.setOnClickListener(v -> {
				username.setVisibility(View.INVISIBLE);
				AUTH.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(authResult -> {
					if (authResult.isSuccessful()) {
						StaticUtils.moveActivity(context, MainActivity.class);
					}
				})
				.addOnFailureListener(e -> {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
				});

			});
		}
		// Sign up
		else {
			TextView title = findViewById(R.id.signinTitleTextView);
			title.setText(R.string.sign_up);

			confirmButton.setOnClickListener(v -> {
				AUTH.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(authResult -> {
					if (authResult.isSuccessful()) {
						GameUser user = new GameUser(AUTH.getUid(), username.getText().toString());
						assert AUTH.getUid() != null;
						userCollection.document(AUTH.getUid()).set(user);
						StaticUtils.moveActivity(context, MainActivity.class);
					}
				})
				.addOnFailureListener(e -> {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				});

			});
		}
	}
}
