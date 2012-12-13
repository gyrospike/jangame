package org.star.game;

import android.graphics.Paint;
import android.graphics.Typeface;

public class TextBoxBase {

	/** the text to display in the box */
	private String mText;
	
	/** controls the appearance of the text */
	private Paint mPaint;
	
	public TextBoxBase() {
		mText = "";
		
		Typeface myFont = BaseObject.sSystemRegistry.mAssetLibrary.getTypeFace("agency");
		mPaint = new Paint();
		mPaint.setTypeface(myFont);
		mPaint.setTextSize(24);
		mPaint.setAntiAlias(true);
		mPaint.setARGB(255,255, 255, 255);
	}
	
	/** setter for the text size */
	public void setTextSize(int size) {
		mPaint.setTextSize(size);
	}
	
	public void setARGB(int a, int r, int g, int b) {
		mPaint.setARGB(a,r, g, b);
	}
	
	/** getter for the label paint */
	public Paint getPaint() {
		return mPaint;
	}
	
	/** setter for text*/
	public void setText(String newText) {
		mText = newText;
	}
	
	/** getter for text*/
	public String getText() {
		return mText;
	}
}
