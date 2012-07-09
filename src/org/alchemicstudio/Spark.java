package org.alchemicstudio;

import android.graphics.Color;
import android.util.Log;


public class Spark extends BaseObject {
	
	/** the max velocity at which a spark will be able to move */
	public static final float MAX_VELOCITY = 300.0f;
	
	/** the starting velocity at which a spark will move */
	public static final float STARTING_VELOCITY = 70.0f;
	
	/** the standard acceleration for a spark */
	private static final float BASE_ACCELERATION = 20.0f;

	/** the spark's sprite */
	public Sprite mSprite;
	
	/** the physical representation of the spark */
	private OnRailsPhysicsObject mPhysicsObject;
	
	/** the current target node for this spark */
	private Node mTargetNode = null;
	
	/** the vector which leads from the spark last node to it's next one */
	private Vector2 mTargetVector = new Vector2();
	
	/** the force being applied to this spark */
	private float mForce = 0.0f;
	
	/** the x position of the target node, stored so we don't have to look it up */
	private float mTargetX = 0.0f;
	
	/** the y position of the target node, stored so we don't have to look it up */
	private float mTargetY = 0.0f;
	
	/** the normal x component of the target vector, stored so we don't have to look it up */
	private float mTargetVectorNormalX = 0.0f;
	
	/** the normal y component of the target vector, stored so we don't have to look it up */
	private float mTargetVectorNormalY = 0.0f;
	
	/** the distance to the target node for the x component, store so we don't have to look it up */
	private double mDistanceToTargetX;
	
	/** the distance to the target node for the y component, store so we don't have to look it up */
	private double mDistanceToTargetY;
	
	/** has this spark been released yet? */
	private boolean isReleased = false;
	
	/** the number of milliseconds that should elapse for each frame */
	private int mMillisecondPerFrame;
	
	/** tracking how much time has elapsed every update call for animation purposes */
	private long mElapsedTime = 0;
	
	
	public Spark() {
		int[] ids = {R.drawable.spark1, R.drawable.spark2, R.drawable.spark3};
		Texture[] textures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(ids);
		mSprite = new Sprite(textures, 2, textures[0].width, textures[0].height);
		mMillisecondPerFrame = 100;
		
		mPhysicsObject = new OnRailsPhysicsObject();
	}
	
	/**
	 * calculate the force behind the spark
	 */
	private void calculateForce() {
		mDistanceToTargetX = mTargetX - mPhysicsObject.getXPos();
		mDistanceToTargetY = mTargetY - mPhysicsObject.getYPos();
		if(mPhysicsObject.getVelocity() < MAX_VELOCITY) {
			mForce = BASE_ACCELERATION;
		} else {
			//Log.d("DEBUG", "reached max velocity");
			mForce = 0.0f;
		}
		mPhysicsObject.setForce(mForce * mTargetVectorNormalX, mForce * mTargetVectorNormalY);
	}
	
	/**
	 * set the position of the sprite
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y) {
		mPhysicsObject.setPosition(x, y);
	}
	
	/**
	 * @param d	the starting velocity the spark should travel at
	 */
	public void setStartingSpeed(float d) {
		mPhysicsObject.setStartingSpeed(d * mTargetVectorNormalX, d * mTargetVectorNormalY);
	}
	
	public double getCurrentSpeed() {
		return mPhysicsObject.getVelocity();
	}
	
	/**
	 * assign a new target node for the spark to navigate towards
	 * @param node
	 */
	public void setTarget(Node node) {
		mTargetNode = node;
		// assume there are no more valid targets
		if(mTargetNode != null) {
			mTargetX = mTargetNode.getPosition().x;
			mTargetY = mTargetNode.getPosition().y;
			
			Vector2 oldTarget = new Vector2(mTargetVector.x, mTargetVector.y);
			mTargetVector.x = mTargetX - mPhysicsObject.getXPos();
			mTargetVector.y = mTargetY - mPhysicsObject.getYPos();
			
			mTargetVectorNormalX = mTargetVector.x / mTargetVector.normalize();
			mTargetVectorNormalY = mTargetVector.y / mTargetVector.normalize();
			
			if(oldTarget.x == mTargetVector.y || oldTarget.y == mTargetVector.x) {
				//Log.d("DEBUG", "switching momentum");
				float nonZero = (mTargetVector.x == 0.0f) ? mTargetVector.y : mTargetVector.x;
				mPhysicsObject.switchMomentum((int)(nonZero/Math.abs(nonZero)));
			} else {
				mPhysicsObject.addRemainder(1, false);
			}
			
			setReleased(true);
			//Log.d("DEBUG", "set target x: " + mTargetNode.getPosition().x);
			//Log.d("DEBUG", "set target y: " + mTargetNode.getPosition().y);
		} else {
			setReleased(false);
		}
	}
	
	/**
	 * reset the spark to its beginning state
	 */
	public void resetSpark() {
		mPhysicsObject = new OnRailsPhysicsObject();
		mForce = 0.0f;
		mDistanceToTargetX = 0.0;
		mDistanceToTargetY = 0.0;
		mTargetVector = new Vector2();
		mTargetNode = null;
		setReleased(false);
	}
	
	/**
	 * @return	has the spark been released?
	 */
	public boolean getReleased() {
		return isReleased;
	}
	
	/**
	 * @param val set if the spark has been released or not
	 */
	public void setReleased(boolean val) {
		isReleased = val;
	}
	
	/**
	 * @return	the current target node
	 */
	public Node getTarget() {
		return mTargetNode;
	}
	
	/**
	 * @return	true if the spark is ready for a new target
	 */
	public boolean getReadyForNextTarget() {
		return mPhysicsObject.hasRemainder();
	}
	
	/**
	 * update the sprite position and send it to be drawn
	 */
	public void updateSprite(long timeDelta) {
		mSprite.setPosition(mPhysicsObject.getXPos(), mPhysicsObject.getYPos());
		if (mMillisecondPerFrame != 0) {
			mElapsedTime += timeDelta;
			if (mElapsedTime > mMillisecondPerFrame) {
				mElapsedTime = mElapsedTime - mMillisecondPerFrame;
				mSprite.incrementFrame();
			}
		}
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
	
	@Override
	public void update(long timeDelta) {
		calculateForce();
		mPhysicsObject.updateState(timeDelta, mDistanceToTargetX, mDistanceToTargetY);
		HUD.getInstance().modifyTextElement("Spark Speed: " + (int) getCurrentSpeed(), "sparkSpeed");
		if(!mPhysicsObject.hasRemainder()) {
			updateSprite(timeDelta);
		}
	}
}

