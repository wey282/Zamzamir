package com.example.zamzamir.authentication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zamzamir.*;
import com.example.zamzamir.R;
import com.example.zamzamir.StaticUtils;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

	private final FirebaseAuth AUTH = FirebaseAuth.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_sign_in);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		StaticUtils.setOrientationToHorizontal(this);

		// If a user is already signed in, skip signing/logging in
		if (AUTH.getCurrentUser() != null)
			StaticUtils.moveActivity(this, MainActivity.class);

		findViewById(R.id.signinButton).setOnClickListener(v -> new SignInDialog(this, true).show());
		findViewById(R.id.signupButton).setOnClickListener(v -> new SignInDialog(this, false).show());
	}
}