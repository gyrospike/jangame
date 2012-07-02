
package org.alchemicstudio;

public class PhysicsObject {

	/** the force vector, determined by classes that use a physics object*/
	private Vector2 mForce = new Vector2();
	
	/** state of object represents position, velocity, and acceleration */
	private PhysicalState mState = new PhysicalState();
	
	/**
	 * evaluates derivative using initial state and time passed in
	 * 
	 * @param initialState
	 * @param derivState
	 * @param dt
	 * @returns
	 */
	private PhysicalState evalDeriv(PhysicalState initialState, PhysicalState derivState, float dt) {
		PhysicalState state = new PhysicalState();
		state.x = initialState.x + (derivState.dx*dt);
		state.y = initialState.y + (derivState.dy*dt);
		state.dx = initialState.dx + (derivState.ddx*dt);
		state.dy = initialState.dy + (derivState.ddy*dt);

		PhysicalState deriv = new PhysicalState();
		deriv.dx = state.dx;
		deriv.dy = state.dy;
		deriv.ddx = mForce.x / 1;
		deriv.ddy = mForce.y / 1;

		return deriv;
	}

	/**
	 * RK4 method of integrating position
	 * 
	 * @param timeDiff
	 * @returns
	 */
	public void integrateState(float timeDiff) {
		PhysicalState emptyDeriv = new PhysicalState();
		PhysicalState a = evalDeriv(mState, emptyDeriv, 0);
		PhysicalState b = evalDeriv(mState, a, timeDiff*0.5f);
		PhysicalState c = evalDeriv(mState, b, timeDiff*0.5f);
		PhysicalState d = evalDeriv(mState, c, timeDiff);

	    float dxdt = (1.0f/6.0f) * (a.dx + 2.0f*(b.dx + c.dx) + d.dx);
		float dydt = (1.0f/6.0f) * (a.dy + 2.0f*(b.dy + c.dy) + d.dy);

		float dvxdt = (1.0f/6.0f) * (a.ddx + 2.0f*(b.ddx + c.ddx) + d.ddx);
		float dvydt = (1.0f/6.0f) * (a.ddy + 2.0f*(b.ddy + c.ddy) + d.ddy);

		mState.oldX = mState.x;
		mState.oldY = mState.y;

		mState.x = mState.x + (dxdt * timeDiff);
		mState.y = mState.y + (dydt * timeDiff);

		mState.dx = mState.dx + (dvxdt * timeDiff);
		mState.dy = mState.dy + (dvydt * timeDiff);
	}
	
	/**
	 * setter for the spark force
	 * 
	 * @param x
	 * @param y
	 */
	public void setForce(float x, float y) {
		mForce.set(x,  y);
	}
	
	/**
	 * setter for the position
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y) {
		mState.x = x;
		mState.y = y;
	}
	
	/**
	 * @return	the state x position
	 */
	public float getXPos() {
		return mState.x;
	}
	
	/**
	 * @return	the state y position
	 */
	public float getYPos() {
		return mState.y;
	}
}

class PhysicalState {
	public float oldX = 0.0f;
	public float oldY = 0.0f;
	
	public float x = 0.0f;
	public float y = 0.0f;
	
	public float dx = 0.0f;
	public float dy = 0.0f;
	
	public float ddx = 0.0f;
	public float ddy = 0.0f;
}
