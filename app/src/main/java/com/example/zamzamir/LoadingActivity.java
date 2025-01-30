package com.example.zamzamir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.xmlpull.v1.XmlPullParser;

public class LoadingActivity extends AppCompatActivity {

	private ProgressBar progressBar;
	private ImageView imageView;

	private int cardCount = 52;
	private int loadedCards = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_loading);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		progressBar = findViewById(R.id.progressBar2);
		progressBar.setMax(cardCount);
		imageView = findViewById(R.id.imageView);
		startLoading();
	}

	private void startLoading() {
		Card.back = BitmapFactory.decodeResource(getResources(), R.drawable.back);
		Card.back = Bitmap.createScaledBitmap(Card.back, Card.WIDTH, Card.HEIGHT, false);
		Card.createBorderPaint(ContextCompat.getColor(this, R.color.border));
		new Thread(() -> loadCards(this, () -> moveActivity(GameActivity.class))).start();
	}

	private void moveActivity(Class<? extends Activity> activity) {
		startActivity(new Intent(this, activity));
	}

	/** Loads all the cards from cards.xml to the deck */
	public void loadCards(Context context, Runnable onFinish) {
		try {
			XmlResourceParser parser = context.getResources().getXml(R.xml.cards);
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG && "card".equals(parser.getName())) {
					int attack = Integer.parseInt(parser.getAttributeValue(null, "attack"));
					int defence = Integer.parseInt(parser.getAttributeValue(null, "defence"));
					String drawableName = parser.getAttributeValue(null, "image");

					// Convert drawable resource name to resource ID
					int drawableResId = Integer.parseInt(drawableName.substring(1));

					// decode the resource into a bitmap
					Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableResId);
					// Process card data
					new Card(bitmap, attack, defence);
				}
			}
			parser.close();
			onFinish.run();
		} catch (Exception e) {
			Log.e("banana", e.getMessage() + "\n" + e.getCause());
		}
	}
}