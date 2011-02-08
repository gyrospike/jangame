package jan.game;

import android.graphics.Point;
import android.util.Log;

public class Spark extends BaseObject {

	public Sprite mSprite;
	public boolean needsDirections, active, readyForNextTarget;

	private float posX, posY, targetX, targetY, targetXNow, targetYNow, velocity;
	private int xDir, yDir, xDirNow, yDirNow;
	private boolean gridActive, lastRun;

	public Spark() {
		mSprite = new Sprite(2);
		velocity = 0.0f;
		needsDirections = false;
		active = false;
		readyForNextTarget = false;
		lastRun = false;
	}

	public void hide() {
		mSprite.setPosition(-32.0f, -32.0f);
		active = false;
		Log.d("DEBUG", "Spark deactivated!");
	}

	public void setPosition(float x, float y) {
		mSprite.setPosition(x, y);
	}

	public void activate(float x, float y) {
		mSprite.setPosition(x, y);
		posX = x;
		posY = y;
		velocity = 3.0f;
		needsDirections = true;
		active = true;
		readyForNextTarget = true;
		gridActive = true;
		Log.d("DEBUG", "Spark activated!");
	}

	public void setNextTarget(float x, float y, boolean gActive) {
		Log.d("DEBUG", "setNextTarget called, targetX: " + x + ", " + y);
		Log.d("DEBUG", "setNextTarget called, targetXNow: " + targetXNow + ", " + targetYNow);
		targetX = x;
		targetY = y;
		gridActive = gActive;
		if (targetXNow == 0 && targetYNow == 0) {
			targetXNow = posX;
			targetYNow = posY;
		}
		if (targetX == targetXNow) {
			xDir = 0;
			if (targetY > targetYNow) {
				yDir = 1;
			} else {
				yDir = -1;
			}
		}
		if (targetY == targetYNow) {
			yDir = 0;
			if (targetX > targetXNow) {
				xDir = 1;
			} else {
				xDir = -1;
			}
		}
		readyForNextTarget = false;
		Log.d("DEBUG", "setNextTarget called, new directions: " + xDir + ", " + yDir);
	}

	public void refreshTarget() {
		if(lastRun) {
			hide();
		}
		targetXNow = targetX;
		targetYNow = targetY;
		xDirNow = xDir;
		yDirNow = yDir;
		needsDirections = false;
		readyForNextTarget = true;
		if(!gridActive) {
			lastRun = true;
		}
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		if (active) {
			
			float distance = 0;
			if(xDirNow != 0) {
				distance = Math.abs(posX - targetXNow);
			} else if (yDirNow != 0) {
				distance = Math.abs(posY - targetYNow);
			}
			//Log.d("DEBUG", "distance: " + distance);
			
			if ((distance == 0) || (xDirNow == 0 && yDirNow == 0)) {
				Log.d("DEBUG", "needs directions");
				needsDirections = true;
			}
			
			float time = (distance / velocity);
			
			if(time > 0 && time < 1) {
				posX += (velocity * xDirNow * time);
				posY += (velocity * yDirNow * time);
			} else {
				posX += (velocity * xDirNow);
				posY += (velocity * yDirNow);
			}

			mSprite.setPosition(posX, posY);

			if (needsDirections) {
				refreshTarget();
			}
		}
		system.scheduleForDraw(mSprite);
	}
}
