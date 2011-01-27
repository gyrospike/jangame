package jan.game;

import android.graphics.Point;
import android.util.Log;

public class Node extends BaseObject {

	public Sprite mSprite;
	public int iX, iY;
	public boolean active;

	private Point target;
	private Vector2 posVector;
	private RenderSystem system = sSystemRegistry.renderSystem;

	public Node(int i, int j, Vector2 vec) {
		iX = i;
		iY = j;
		posVector = vec;
		target = new Point(-1, -1);
		active = false;
		
		mSprite = new Sprite(0);
		mSprite.cameraRelative = false;
		mSprite.setPosition(posVector.x, posVector.y);
		mSprite.currentTextureIndex = 0;
		
		Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
	}

	public void setTarget(Point point) {
		target = point;
	}

	public void setTargetNull() {
		target = new Point(-1, -1);
	}

	public Point getTarget() {
		return target;
	}

	public void activate() {
		active = true;
		mSprite.currentTextureIndex = 2;
		// mSprite.setCrop(32, 32, 32, 32);
	}

	public void deactivate() {
		active = false;
		mSprite.currentTextureIndex = 0;
		// mSprite.setCrop(0, 32, 32, 32);
	}

	public float getX() {
		return posVector.x;
	}

	public float getY() {
		return posVector.y;
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		system.scheduleForDraw(mSprite);
	}
}