package org.alchemicstudio;

import android.util.Log;


public class Node extends BaseObject {
	
	/** the number of connections a node may have by default */
	public final static int CONNECTION_LIMIT_DEFAULT = 4;
	
	/** sprite for the node */
	public Sprite mSprite;
	
	// TODO - remove
	public int sourceKey;
	public int type;
	public int link;
	public float minSpeedLimit;
	public float maxSpeedLimit;
	
	/** the number of connections a node may have if it is an off/on ramp */
	private final static int CONNECTION_LIMIT_TERMINAL = 1;
	
	/** track indices that linking this node to other nodes */
	private int[] mTrackIdArray;
	
	/** the i index for this node's position in the grid */
	private int mI;
	
	/** the j index for this node's position in the grid */
	private int mJ;
	
	/** the maximum number of connections to other nodes this node can have */
	private int mMaxConnections;
	
	/** keeps track of the current number of connections */
	private int mNumCurrentConnections;
	
	/** the other nodes that have connections to this node */
	private NodeConnection[] mNodeConnections;

	public Node(int i, int j, Vector2 vec, boolean isStartNode, boolean isEndNode, boolean isFixed, float maxSpeedLimit, float minSpeedLimit, int link, int type) {
		mI = i;
		mJ = j;
		
		this.type = type;
		this.link = link;
		this.maxSpeedLimit = maxSpeedLimit;
		this.minSpeedLimit = minSpeedLimit;

		mNumCurrentConnections = 0;
		// create an array of connections so we can have {i, j, k} - k being used for the border array
		mNodeConnections = new NodeConnection[4];

		for (int k = 0; k < mNodeConnections.length; k++) {
			mNodeConnections[k] = new NodeConnection(-1, -1, -1);
		}

		if(this.type==2) {
			int[] spriteArray = {R.drawable.grey_gate_node, R.drawable.yellow_gate_node, R.drawable.green_gate_node};
			mSprite = new Sprite(spriteArray, 1, 32.0f, 32.0f, 3, 0);
		} else {
			int[] spriteArray = {R.drawable.grey_node, R.drawable.yellow_node, R.drawable.green_node};
			mSprite = new Sprite(spriteArray, 1, 32.0f, 32.0f, 3, 0);
		}
		
		mSprite.cameraRelative = false;
		mSprite.setPosition(vec.x, vec.y);
		mSprite.currentTextureIndex = 0;
		
		
		if(isStartNode || isEndNode) {
			mMaxConnections = CONNECTION_LIMIT_TERMINAL;
			mTrackIdArray = new int[CONNECTION_LIMIT_TERMINAL];
			mSprite.currentTextureIndex = 2;
		} else {
			mMaxConnections = CONNECTION_LIMIT_DEFAULT;
			mTrackIdArray = new int[CONNECTION_LIMIT_DEFAULT];
		}
		
		for(int h = 0; h < mTrackIdArray.length; h++) {
			mTrackIdArray[h] = -1;
		}

		//Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
	}
	
	/**
	 * create a connection to another node
	 * 
	 * @param i			the i index of the other node
	 * @param j			the j index of the other node
	 * @param k			the k index of the other node (used for the border nodes)
	 * @param trackID	the id for the track segment that represents the visual component of a connection
	 */
	public void setConnection(int i, int j, int k, int trackID) {
		for (int p = 0; p < mNodeConnections.length; p++) {
			if (mNodeConnections[p].isEmptyNodeConnection()) {
				mNodeConnections[p] = new NodeConnection(i, j, k);
				mNodeConnections[p].setTrackID(trackID);
				mNumCurrentConnections++;
				break;
			}
		}
	}

	/**
	 * remove a connection, currently this only supports removing other game board nodes (not border nodes)
	 * 
	 * @param i		the i index for the node to be removed
	 * @param j		the k index for the node to be removed
	 */
	public void removeConnection(int i, int j) {
		for (int p = 0; p < mNodeConnections.length; p++) {
			if (mNodeConnections[p].hasValueOf(i, j, -1)) {
				mNodeConnections[p] = new NodeConnection(-1, -1, -1);
				mNumCurrentConnections--;
				break;
			}
		}
	}

	/**
	 * remove all non-border node connections from this node
	 */
	public void removeAllConnections() {
		for (int i = 0; i < mNodeConnections.length; i++) {
			if((mNodeConnections[i].getI() != -1 || mNodeConnections[i].getJ() != -1) && mNodeConnections[i].getK() == -1) {
				mNodeConnections[i] = new NodeConnection(-1, -1, -1);
				mNumCurrentConnections--;
			}	
		}
	}
	
	/**
	 * @return	true is this node has reached the maximum number of connections it can support
	 */
	public boolean hasMaxConnections() {
		if(getNumConnections() >= mMaxConnections) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return	the number of connections this node has
	 */
	public int getNumConnections() {
		return mNumCurrentConnections;
	}
	
	/**
	 * @return	the i index
	 */
	public int getI() {
		return mI;
	}
	
	/**
	 * @return	the j index
	 */
	public int getJ() {
		return mJ;
	}
	
	/**
	 * gets the connection data for this node (makes sure to not return empty connections)
	 * 
	 * @return		this node's connections
	 */
	public NodeConnection[] getConnections() {
		NodeConnection[] nonEmptyConnections = new NodeConnection[mNumCurrentConnections];
		int index = 0;
		for(int i = 0; i < mNodeConnections.length; i++) {
			if(!mNodeConnections[i].isEmptyNodeConnection()) {
				nonEmptyConnections[index] = mNodeConnections[i];
				index++;
			}
		}
		return nonEmptyConnections;
	}

	// TODO - remove
	public void removePower() {
		mSprite.currentTextureIndex = 0;
	}

	@Override
	public void update(float timeDelta) {
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}