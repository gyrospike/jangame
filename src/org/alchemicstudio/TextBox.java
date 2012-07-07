package org.alchemicstudio;

import android.graphics.Paint;
import android.graphics.Typeface;

public class TextBox {
	
	/** the text to display in the box */
	private String mText;
	
	/** the x position for this text box */
	private float mPosX;
	
	/** the x position for this text box */
	private float mPosY;
	
	/** the index for this text box */
	private int mIndex;
	
	/** controls the appearance of the text */
	private Paint mPaint;
	
	/** constructor */
	public TextBox(float x, float y) {
		mPosX = x;
		mPosY = y;
		
		Typeface myFont = BaseObject.sSystemRegistry.mAssetLibrary.getTypeFace("agency");
		mPaint = new Paint();
		mPaint.setTypeface(myFont);
		mPaint.setTextSize(24);
		mPaint.setAntiAlias(true);
		mPaint.setARGB(0xff, 0x00, 0x00, 0x00);
	}
	
	public void setPosition(float posX, float posY) {
		mPosX = posX;
		mPosY = posY;
	}
	
	/** setter for the text size */
	public void setTextSize(int size) {
		mPaint.setTextSize(size);
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
	
	/** getter for x position */
	public float getX() {
		return mPosX;
	}
	
	/** getter for y position */
	public float getY() {
		return mPosY;
	}
	
	/** setter for the index */
	public void setIndex(int index) {
		mIndex = index;
	}
	
	/** getter for the index */
	public int getIndex() {
		return mIndex;
	}
	
}
