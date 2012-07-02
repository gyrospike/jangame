package org.alchemicstudio;

import android.util.Log;

public class Spark extends BaseObject {

	public Sprite mSprite;
	
	private PhysicsObject mPhysicsObject;
	
	private Node mTargetNode = null;
	
	private boolean readyForNextTarget = false;
	
	public Spark() {
		int[] textureArray = {R.drawable.spark};
		mSprite = new Sprite(textureArray, 2, 32.0f, 32.0f);
		mSprite.setPosition(10.0f, 70.0f);
		
		mPhysicsObject = new PhysicsObject();
	}
	
	/**
	 * set the force for the spark
	 * 
	 * @param forceX
	 * @param forceY
	 */
	public void calculateForce(float timeDelta) {
		// if less than one full move away, ask for new target
		mPhysicsObject.setForce(0.0001f, 0.0f);
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
	 * assign a new target node for the spark to navigate towards
	 * @param node
	 */
	public void setTarget(Node node) {
		mTargetNode = node;
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
	public boolean readyForNextTarget() {
		return readyForNextTarget;
	}
	
	@Override
	public void update(float timeDelta) {
		calculateForce(timeDelta);
		mPhysicsObject.integrateState(timeDelta);
		mSprite.setPosition(mPhysicsObject.getXPos(), mPhysicsObject.getYPos());
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}

