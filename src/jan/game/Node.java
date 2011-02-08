package jan.game;

import android.graphics.Point;
import android.util.Log;

public class Node extends BaseObject {

	public Sprite mSprite;
	public int iX, iY;
	public boolean source, hasPower;

	private Point[] targetArray;
	private int[] targetWireTypeArray;
	private Vector2 posVector;
	private RenderSystem system = sSystemRegistry.renderSystem;

	public Node(int i, int j, Vector2 vec) {
		iX = i;
		iY = j;
		posVector = vec;

		source = false;
		hasPower = false;

		targetArray = new Point[4];
		targetWireTypeArray = new int[4];

		for (int k = 0; k < targetArray.length; k++) {
			targetArray[k] = new Point(-1, -1);
		}

		for (int l = 0; l < targetWireTypeArray.length; l++) {
			targetWireTypeArray[l] = -1;
		}

		mSprite = new Sprite(1);
		mSprite.cameraRelative = false;
		mSprite.setPosition(posVector.x, posVector.y);
		mSprite.currentTextureIndex = 0;

		Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
	}

	public void setSource() {
		source = true;
		mSprite.currentTextureIndex = 2;
	}

	public void setConnection(Point point, int wireType) {
		for (int i = 0; i < targetArray.length; i++) {
			if (targetArray[i].equals(new Point(-1, -1))) {
				targetArray[i] = point;
				targetWireTypeArray[i] = wireType;
				i = targetArray.length;
			}
		}
	}

	public void setConnectionNull(int index) {
		targetArray[index] = new Point(-1, -1);
		targetWireTypeArray[index] = -1;
	}

	public void setConnectionsNull() {
		for (int i = 0; i < targetArray.length; i++) {
			targetArray[i] = new Point(-1, -1);
			targetWireTypeArray[i] = -1;
		}
	}

	public Point getConnection(int i) {
		return targetArray[i];
	}

	public Point[] getConnections() {
		return targetArray;
	}

	public int[] getWireTypes() {
		return targetWireTypeArray;
	}
	
	public void removePower() {
		if (!source) {
			hasPower = false;
			mSprite.currentTextureIndex = 0;
		}
	}

	public void activate(int level) {
		if (!source) {
			mSprite.currentTextureIndex = level;
			hasPower = true;
		}
	}

	public void deactivate() {
		if (!source) {
			hasPower = false;
			mSprite.currentTextureIndex = 0;
			setConnectionsNull();
		}
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