
package org.star.circuit;


public class OnRailsPhysicsObject extends PhysicsObject {

	/** amount truncated from delta x that needs to be added to the new target vector */
	private float remainderDX = 0.0f;
	
	/** amount truncated from delta y that needs to be added to the new target vector */
	private float remainderDY = 0.0f;
	
	/**
	 * RK4 method of integrating position
	 * 
	 * @param timeDiff
	 * @returns
	 */
	public void updateState(float timeDiff, double distanceToTargetX, double distanceToTargetY) {
		// slow down the time scale
		// TODO - figure out what to do with this scale
		timeDiff = timeDiff/1000;
		PhysicalState integratedState = integrateState(timeDiff);

		float dx = integratedState.dx*timeDiff;
		float dy = integratedState.dy*timeDiff;
		
		float diffX = Math.abs(dx) - Math.abs((float)distanceToTargetX);
		float diffY = Math.abs(dy) - Math.abs((float)distanceToTargetY);
		
		if(diffX > 0) {
			remainderDX =  diffX;
			dx = (float) distanceToTargetX;
		} else {
			remainderDX = 0.0f;
		}
		
		if(diffY > 0) {
			remainderDY = diffY;
			dy = (float) distanceToTargetY;
		} else {
			remainderDY = 0.0f;
		}
		
		mState.oldX = mState.x;
		mState.oldY = mState.y;
		
		mState.x = mState.x + dx;
		mState.y = mState.y + dy;

		mState.dx = mState.dx + (integratedState.ddx * timeDiff);
		mState.dy = mState.dy + (integratedState.ddy * timeDiff);
	}
	
	/**
	 *
	 * on rails specific fantasy physics, allows the rails object
	 * to maintain it's momentum regardless of axis the object is traveling on
	 * 
	 * @param sign	new axis may require a new sign, ex:
	 * 
	 * traveling to the east on the X axis, then heading North on Y axis
	 * 
	 */
	public void switchMomentum(int sign) {
		float stateDX = mState.dx;
		mState.dx = Math.abs(mState.dy) * sign;
		mState.dy = Math.abs(stateDX) * sign;
		
		addRemainder(sign, true);
	}
	
	/**
	 * specify the starting speed for this object
	 * @param dx
	 * @param dy
	 */
	public void setStartingSpeed(float dx, float dy) {
		mState.dx = dx;
		mState.dy = dy;
	}
	
	/**
	 * gets the total velocity of this object
	 */
	public double getVelocity() {
		return Math.pow((Math.pow(mState.dx, 2) + Math.pow(mState.dy, 2)), 0.5);
	}
	
	/**
	 * Apply the remainder to the object position
	 * 
	 * @param sign			scalar for the remainder (this may not be what the original delta 
	 * 						would have used - supports switching direction and therefore sign)
	 * @param applySwitch	whether the delta should now apply to a new axis
	 */
	public void addRemainder(int sign, boolean applySwitch) {
		float xRemainderAmount = remainderDX * sign;
		float yRemainderAmount = remainderDY * sign;
		if(applySwitch) {
			xRemainderAmount =  remainderDY * sign;
			yRemainderAmount =  remainderDX * sign;
		}
		mState.x = mState.x + xRemainderAmount;
		mState.y = mState.y + yRemainderAmount;
	}
	
	/**
	 * Remainder signifies a delta that still needs to be applied to the position
	 * of the object but has not yet been done so due to the object reaching the node
	 * and still having some delta to apply
	 * 
	 * @return	true if there is still some delta to add to the position
	 */
	public boolean hasRemainder() {
		return (remainderDX == 0.0f && remainderDY == 0.0f) ? false : true;
	}
	
}


