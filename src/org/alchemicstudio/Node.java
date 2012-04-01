package org.alchemicstudio;

import android.graphics.Point;
import android.util.Log;

public class Node extends BaseObject {
	
	/** the number of connections a node may have by default */
	private final static int CONNECTION_LIMIT_DEFAULT = 4;
	
	/** the number of connections a node may have if it is an off/on ramp */
	private final static int CONNECTION_LIMIT_RAMP = 1;

	public Sprite mSprite;
	
	public int iX, iY;
	public boolean mRampNode; 
	public boolean hasPower;
	
	public int sourceKey;
	public int type;
	public int link;
	public float minSpeedLimit;
	public float maxSpeedLimit;
	
	private Point[] targetArray;
	private int mMaxConnections;
	private int[] targetWireTypeArray;
	private Vector2 posVector;

	public Node(int i, int j, Vector2 vec, boolean isRampNode, float maxSpeedLimit, float minSpeedLimit, int link, int type) {
		iX = i;
		iY = j;
		posVector = vec;
		
		mMaxConnections = CONNECTION_LIMIT_DEFAULT;
		if(isRampNode) {
			mMaxConnections = CONNECTION_LIMIT_RAMP;
		}
		
		this.type = type;
		this.link = link;
		this.maxSpeedLimit = maxSpeedLimit;
		this.minSpeedLimit = minSpeedLimit;
		
		
		hasPower = false;

		targetArray = new Point[4];
		targetWireTypeArray = new int[4];

		for (int k = 0; k < targetArray.length; k++) {
			targetArray[k] = new Point(-1, -1);
		}

		for (int l = 0; l < targetWireTypeArray.length; l++) {
			targetWireTypeArray[l] = -1;
		}

		if(this.type==2) {
			int[] spriteArray = {R.drawable.grey_gate_node, R.drawable.yellow_gate_node, R.drawable.green_gate_node};
			mSprite = new Sprite(spriteArray, 1, 32.0f, 32.0f, 3, 0);
		} else {
			int[] spriteArray = {R.drawable.grey_node, R.drawable.yellow_node, R.drawable.green_node};
			mSprite = new Sprite(spriteArray, 1, 32.0f, 32.0f, 3, 0);
		}
		
		mSprite.cameraRelative = false;
		mSprite.setPosition(posVector.x, posVector.y);
		mSprite.currentTextureIndex = 0;
		
		if(isRampNode) {
			setSource();
		}

		//Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
	}

	public void setSource() {
		mRampNode = true;
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
		if(getNumConnections() >= mMaxConnections) {
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
		if (!mRampNode) {
			hasPower = false;
			mSprite.currentTextureIndex = 0;
		}
	}

	public void activate(int level, int key) {
		if (!mRampNode) {
			sourceKey = key;
			mSprite.currentTextureIndex = level;
			hasPower = true;
		}
	}

	public void deactivate() {
		if (!mRampNode) {
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
	public void update(float timeDelta) {
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}