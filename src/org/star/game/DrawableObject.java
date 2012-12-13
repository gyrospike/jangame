package org.star.game;


import org.star.types.Vector2;

public class DrawableObject extends BaseObject {
	
	/** sprite for the drawable object */
	public Sprite mSprite;
	
	/** how fast should this object rotate */
	private float mRotationSpeed = 0.0f;
	
	/** the base level of opacity */
	private float mBaseOpacityDeficit = 0.0f;
	
	public DrawableObject(ImagePack imagePack, int drawPriority) {
		mSprite = new Sprite(imagePack, drawPriority);
	}
	
	public DrawableObject(ImagePack imagePack, int drawPriority, int polyWidth, int polyHeight) {
		mSprite = new Sprite(imagePack, drawPriority, polyWidth, polyHeight);
	}
	
	/**
	 * 
	 * @param xOffset	the x position to set
	 * @param yOffset 	the y position to set
	 */
	public void setPosition(float xOffset, float yOffset) {
		mSprite.setPosition(xOffset, yOffset);
	}
	
	/**
	 * Certain drawable objects are slightly transparent, this setting
	 * determines how transparent they are
	 * 
	 * @param num
	 */
	public void setBaseOpacityDeficit(float num) {
		mBaseOpacityDeficit = num;
	}
	
	/**
	 * 
	 * @param speed		the rotation speed in degrees
	 */
	public void setRotationSpeed(float speed) {
		mRotationSpeed = speed;
	}
	
	/**
	 * 
	 * @param newPos	new position to use
	 */
	public void setRelativePosition(Vector2 newPos) {
		mSprite.setPosition(newPos.x, newPos.y);
	}
	
	/**
	 * how much of the base opacity deficit should be used
	 * 
	 * @param frac
	 */
	public void setFlashOpacity(float frac) {
		mSprite.setOpacity((1.0f-mBaseOpacityDeficit) + (mBaseOpacityDeficit*frac));
	}
	
	@Override
	public void update(long timeDelta) {
		if(mRotationSpeed != 0.0f) {
			mSprite.setRotationDegrees(mSprite.getRotation()+mRotationSpeed);
		}
        mSprite.updateFrame(timeDelta);
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}
