package jan.game;

import android.graphics.Point;
import android.util.Log;

public class Spark extends BaseObject {

	public Sprite mSprite;
	public boolean needsDirections, active, readyForNextTarget;

	private float posX, posY, targetX, targetY, targetXNow, targetYNow, velocity;
	private int xDir, yDir, xDirNow, yDirNow;
	private int targetIndexX, targetIndexY, oldTargetIndexX, oldTargetIndexY;

	public Spark() {
		mSprite = new Sprite(2);
		velocity = 0.0f;
		needsDirections = false;
		active = false;
		readyForNextTarget = false;
	}

	public void hide() {
		mSprite.setPosition(-32.0f, -32.0f);
	}

	public void setPosition(float x, float y) {
		mSprite.setPosition(x, y);
	}

	public void activate(float x, float y) {
		mSprite.setPosition(x, y);
		posX = x;
		posY = y;
		velocity = 0.5f;
		needsDirections = true;
		active = true;
		readyForNextTarget = true;
		Log.d("DEBUG", "Spark activated!");
	}
	
	public void setNextTarget(float x, float y) {
		Log.d("DEBUG", "setNextTarget called!");
		targetX = x;
		targetY = y;
		if (targetX == posX) {
			xDir = 0;
			if (targetY > posY) {
				yDir = 1;
			} else {
				yDir = -1;
			}
		}
		if (targetY == posY) {
			yDir = 0;
			if (targetX > posX) {
				xDir = 1;
			} else {
				xDir = -1;
			}
		}
		readyForNextTarget = false;
	}

	public void refreshTarget() {
		targetXNow = targetX;
		targetYNow = targetY;
		xDirNow = xDir;
		yDirNow = yDir;
		needsDirections = false;
		readyForNextTarget = true;
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		if (active) {

			posX += (velocity * xDirNow);
			posY += (velocity * yDirNow);

			mSprite.setPosition(posX, posY);
			
			if(((posX - targetXNow)<3 && (posY - targetYNow)<3) || (xDirNow==0 && yDirNow==0)) {
				needsDirections = true;
			}
			
			if(needsDirections) {
				refreshTarget();
			}
		}
		system.scheduleForDraw(mSprite);
	}
}
