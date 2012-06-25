package org.alchemicstudio;

import android.util.Log;

public class Spark extends BaseObject {

	public Sprite mSprite;

	public boolean active;
	public boolean readyForNextTarget;
	public float velocity;

	public boolean explode;
	public float explodeX;
	public float explodeY;

	private final float MAX_VELOCITY = 100.0f;

	private float posX;
	private float posY;
	private float targetX;
	private float targetY;
	private float nextTargetX;
	private float nextTargetY;
	private float acceleration;

	private float gateMinVelocity;
	private float gateMaxVelocity;
	private int gateType;

	private int xDir;
	private int yDir;
	private int nextXDir;
	private int nextYDir;
	private boolean done;
	private boolean lastTarget;

	public Spark() {
		int[] textureArray = {R.drawable.spark};
		mSprite = new Sprite(textureArray, 2, 32.0f, 32.0f);
		
		explode = false;
		active = false;
		hide();
	}

	public void explode() {
		explodeX = posX + 16.0f;
		explodeY = posY - 16.0f;
		Log.d("DEBUG", "explodeX, explodeY: " + explodeX + ", " + explodeY);
		explode = true;
	}

	public void hide() {
		active = false;
		readyForNextTarget = false;
		lastTarget = false;
		done = false;
		posX = -32.0f;
		posY = -32.0f;
		xDir = 0;
		yDir = 0;
		targetX = 0.0f;
		targetY = 0.0f;
		nextTargetX = 0.0f;
		nextTargetY = 0.0f;
		gateMinVelocity = 0.0f;
		gateMaxVelocity = 0.0f;
		Log.d("DEBUG", "Spark deactivated!");
	}

	public void setPosition(float x, float y) {
		mSprite.setPosition(x, y);
	}

	public void activate(float x, float y) {
		mSprite.setPosition(x, y);
		velocity = 10.0f;
		acceleration = 1.0f;
		posX = x;
		posY = y;

		targetX = posX;
		targetY = posY;
		nextTargetX = posX;
		nextTargetY = posY;

		active = true;
		Log.d("DEBUG", "Spark activated!");
	}

	public void setNextTarget(float x, float y, int type,
			float newGateMinVelocity, float newGateMaxVelocity, boolean last) {
		Log.d("DEBUG", "Setting Next Target... ");
		nextTargetX = x;
		nextTargetY = y;
		gateType = type;
		gateMinVelocity = newGateMinVelocity;
		gateMaxVelocity = newGateMaxVelocity;
		lastTarget = last;
		// Log.d("DEBUG", "targetX, targetY: " + targetX + ", " + targetY);
		// Log.d("DEBUG", "nextTargetX, nextTargetY: " + nextTargetX + ", " +
		// nextTargetY);

		if (targetX == nextTargetX) {
			nextXDir = 0;
			if (targetY > nextTargetY) {
				nextYDir = -1;
			} else {
				nextYDir = 1;
			}
		}
		if (targetY == nextTargetY) {
			nextYDir = 0;
			if (targetX > nextTargetX) {
				nextXDir = -1;
			} else {
				nextXDir = 1;
			}
		}

		// Log.d("DEBUG", "So, nextXDir, nextYDir: " + nextXDir + ", " +
		// nextYDir);

		readyForNextTarget = false;
	}

	private void refreshTarget() {
		if (!done) {
			Log.d("DEBUG", "Refreshing Target...");
			if (gateType == 2) {
				if (velocity >= gateMinVelocity && velocity <= gateMaxVelocity) {
					targetX = nextTargetX;
					targetY = nextTargetY;
					xDir = nextXDir;
					yDir = nextYDir;
					readyForNextTarget = true;
				} else {
					Log.d("DEBUG", "Not fast enough, velocity: " + velocity
							+ ", gate velocity: " + gateMinVelocity);
					explode();
					hide();
				}
			} else {
				targetX = nextTargetX;
				targetY = nextTargetY;
				xDir = nextXDir;
				yDir = nextYDir;
				readyForNextTarget = true;
			}
		} else {
			Log.d("DEBUG", "about to hide");
			explode();
			hide();
		}
		if (lastTarget) {
			done = true;
		}
		// Log.d("DEBUG", "target refreshed, xDir, yDir: " + xDir + ", " +
		// yDir);
	}

	@Override
	public void update(float timeDelta) {
		if (active) {

			// scaling timeDelta so that it won't increase or decrease the
			// speeds too much
			timeDelta = timeDelta / 100;

			float distance = 0.0f;

			if (xDir == -1) {
				distance = posX - targetX;
			} else if (xDir == 1) {
				distance = targetX - posX;
			} else if (yDir == -1) {
				distance = posY - targetY;
			} else if (yDir == 1) {
				distance = targetY - posY;
			}

			if (distance <= 0 || (xDir == 0 && yDir == 0)) {
				// Log.d("DEBUG", "update: xDir, yDir: " + xDir + ", " + yDir);
				// Log.d("DEBUG", "update: distance: " + distance);
				Log.d("DEBUG", "targetX, targetY: " + targetX + ", " + targetY);
				posX = targetX;
				posY = targetY;
				refreshTarget();
			} else {
				float time = Math.abs(distance / (velocity * timeDelta));
				if (time < 1) {
					posX += (velocity * xDir * time * timeDelta);
					posY += (velocity * yDir * time * timeDelta);
				} else {
					posX += (velocity * xDir * timeDelta);
					posY += (velocity * yDir * timeDelta);
				}
			}
			if (velocity < MAX_VELOCITY) {
				velocity += acceleration * timeDelta;
			}
			mSprite.setPosition(posX, posY);
			sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
		}
	}
}
