package com.example.zamzamir.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Represents an image that is drawn on screen. */
public class ImageObject extends Point {

	private Bitmap bitmap;

	private static final List<ImageObject> objects = new ArrayList<>();

	public ImageObject(@Nullable Bitmap bitmap, int x, int y) {
		this.bitmap = bitmap;
		objects.add(this);
		this.x = x;
		this.y = y;
	}

	/** Draws all image objects on the canvas. */
	public static void drawAll(Canvas canvas) {
		for (int i = 0; i < objects.size(); i++) {
			objects.get(i).draw(canvas);
		}
	}

	/** Draws this on the canvas. */
	private void draw(Canvas canvas) {
		if (bitmap != null)
			canvas.drawBitmap(bitmap, x, y, null);
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	/** Deletes this image object. */
	public void delete() {
		objects.remove(this);
	}
}
