package org.alchemicstudio;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class TextBox {
	
	public String mText;
	
	public TextBox() {
		// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0);

		// get a background image from resources
		// note the image format must match the bitmap format
		//Drawable background = context.getResources().getDrawable(R.drawable.background);
		//background.setBounds(0, 0, 256, 256);
		//background.draw(canvas); // draw the background to our bitmap

		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
		// draw the text centered
		canvas.drawText("Hello World", 16,112, textPaint);
	}

}
