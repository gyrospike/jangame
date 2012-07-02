package org.alchemicstudio;

import android.os.SystemClock;

public class DrawableObject extends BaseObject {
	
	/** sprite for the drawable object */
	public Sprite mSprite;
	
	/** the number of milliseconds that should elapse for each frame */
	private int mMillisecondPerFrame;
	
	/** tracking how much time has elapsed every update call for animation purposes */
	private float mElapsedTime;
	
	/**
	 * Basic drawable object that holds a sprite object and can update that sprite object
	 * for animations
	 * 
	 * @param textureArray
	 * @param drawPriority
	 * @param width
	 * @param height
	 * @param mspf
	 */
	public DrawableObject(int[] textureArray, int drawPriority, float width, float height, int mspf) {
		mMillisecondPerFrame = mspf;
		mSprite = new Sprite(textureArray, drawPriority, width, height);
	}
	
	public void setPositionAndAngle(float angle, float xOffset, float yOffset) {
		mSprite.setPosition(xOffset, yOffset);
		mSprite.setRotation(angle);
	}
	
	@Override
	public void update(float timeDelta) {
		
		if (mMillisecondPerFrame != 0) {
			mElapsedTime += timeDelta;
			if (mElapsedTime > mMillisecondPerFrame) {
				mElapsedTime = mElapsedTime - mMillisecondPerFrame;
				mSprite.incrementFrame();
			}
		}
		
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}
