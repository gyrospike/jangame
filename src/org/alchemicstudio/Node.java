package org.alchemicstudio;

import android.graphics.Point;
import android.util.Log;

public class Node extends BaseObject {

	public Sprite mSprite;
	
	public int iX, iY;
	public boolean source; 
	public boolean hasPower;
	
	public int sourceKey;
	public int type;
	public int link;
	public float minSpeedLimit;
	public float maxSpeedLimit;
	
	private Point[] targetArray;
	private int maxConnections;
	private int[] targetWireTypeArray;
	private Vector2 posVector;
	private RenderSystem system = sSystemRegistry.renderSystem;

	public Node(int i, int j, Vector2 vec, int maxC) {
		iX = i;
		iY = j;
		posVector = vec;
		
		maxConnections = maxC;

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

		mSprite = new Sprite(1, 3);
		mSprite.cameraRelative = false;
		mSprite.setPosition(posVector.x, posVector.y);
		mSprite.currentTextureIndex = 0;

		//Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
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
		int nullIndex = -1;
		for(int i = 0; i < targetArray.length; i++) {
			if(nullIndex != -1 && targetArray[i].x != -1) {
				targetArray[nullIndex] = targetArray[i];
				targetWireTypeArray[nullIndex] = targetWireTypeArray[i];
				targetArray[i] = new Point(-1, -1);
				targetWireTypeArray[i] = -1;
			}
			if(targetArray[i].x == -1) {
				nullIndex = i;
			}
		}
		Log.d("DEBUG", "After running setConnectionNull: " + targetArray[0] + ", " + targetArray[1] + ", " + targetArray[2] + ", " + targetArray[3]);
	}

	public void setConnectionsNull() {
		for (int i = 0; i < targetArray.length; i++) {
			targetArray[i] = new Point(-1, -1);
			targetWireTypeArray[i] = -1;
		}
	}
	
	public boolean hasMaxConnections() {
		if(getNumConnections() >= maxConnections) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getNumConnections() {
		int len = 0;
		for(int i = 0; i < targetArray.length; i++) {
			if(!targetArray[i].equals(new Point(-1, -1))) {
				len++;
			}
		}
		return len;
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

	public void activate(int level, int key) {
		if (!source) {
			sourceKey = key;
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