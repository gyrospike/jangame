package org.star.game;


public class TextBox extends TextBoxBase {
	
	/** the x position for this text box */
	private float mPosX;
	
	/** the x position for this text box */
	private float mPosY;
	
	/** the index for this text box */
	private int mIndex;
	
	/** constructor */
	public TextBox(float x, float y) {
		super();
		mPosX = x;
		mPosY = y;
	}
	
	public void setPosition(float posX, float posY) {
		mPosX = posX;
		mPosY = posY;
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
