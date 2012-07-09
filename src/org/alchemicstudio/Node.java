package org.alchemicstudio;

import android.graphics.Color;
import android.util.Log;


public class Node extends BaseObject {
	
	/** the number of connections a node may have by default */
	public final static int CONNECTION_LIMIT_DEFAULT = 4;
	
	public final static int PREFERRED_CONNECTION_REQ_NUM = 3;
	
	public final static String NODE_TYPE_STANDARD = "NODE_TYPE_STANDARD";
	
	public final static String NODE_TYPE_START = "NODE_TYPE_START";
	
	public final static String NODE_TYPE_END = "NODE_TYPE_END";
	
	public final static String NODE_TYPE_EMPTY = "NODE_TYPE_EMPTY";
	
	public final static String NODE_TYPE_DEAD = "NODE_TYPE_DEAD";
	
	public final static String NODE_TYPE_SPEED_TRAP = "NODE_TYPE_SPEED_TRAP";
	
	/** const for angles */
	private final static double ANGLE_EAST = Math.PI;
	
	/** const for angles */
	private final static double ANGLE_SOUTH = Math.PI/2.0;
	
	/** const for angles */
	private final static double ANGLE_WEST = 0.0;
	
	/** const for angles */
	private final static double ANGLE_NORTH = (3.0/2.0)*Math.PI;
	
	/** how far the preferred node arrow indicator is from it's node */
	private final static int PREFERRED_ARROW_OFFSET = 25;
	
	/** how far the speed indicator is from it's node */
	private final static int SPEED_OFFSET_RIGHT = 35;
	
	/** how far the speed indicator is from it's node */
	private final static int SPEED_OFFSET_LEFT = 25;
	
	private final static String NODE_TYPE_KEY = "NODE_TYPE_KEY";
	
	private final static String NODE_TYPE_GATE = "NODE_TYPE_GATE";
	
	private final static String HUD_SPEED_TRAP_MAX_UNIQUE_ID_PREFIX = "ST_MAX:";
	
	private final static String HUD_SPEED_TRAP_MIN_UNIQUE_ID_PREFIX = "ST_MIN:";
	
	/** sprite for the node */
	private Sprite mSprite;
	
	/** stores the nodes type */
	private String mType;
	
	/** speed limits for speed trap nodes */
	private int mMinSpeedLimit;
	
	/** speed limits for speed trap nodes */
	private int mMaxSpeedLimit;
	
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
	
	/** in case of multiple possible paths, use this path */
	private NodeConnection mPreferredConnection = null;
	
	/** the vector position of this node */
	private Vector2 mPosition = null;

	public Node(int i, int j, Vector2 vec, int maxSpeedLimit, int minSpeedLimit, String type) {
		mI = i;
		mJ = j;
		mPosition = vec;
		mType = type;
		mMaxSpeedLimit = maxSpeedLimit;
		mMinSpeedLimit = minSpeedLimit;

		mNumCurrentConnections = 0;
		mMaxConnections = CONNECTION_LIMIT_DEFAULT;
		mNodeConnections = new NodeConnection[mMaxConnections];
		mTrackIdArray = new int[CONNECTION_LIMIT_DEFAULT];

		for (int k = 0; k < mNodeConnections.length; k++) {
			mNodeConnections[k] = new NodeConnection(-1, -1, -1);
		}
		
		if(mType.equals(NODE_TYPE_SPEED_TRAP)) {
			int[] ids = {R.drawable.grey_gate_node, R.drawable.yellow_gate_node, R.drawable.green_gate_node};
			Texture[] textures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(ids);
			mSprite = new Sprite(textures, 1);
			HUD.getInstance().addTextElement(-1, Integer.toString(mMaxSpeedLimit), 24, Color.GREEN, mPosition.x + SPEED_OFFSET_RIGHT, mPosition.y, true, HUD.NOT_UNIQUE_ELEMENT);
			HUD.getInstance().addTextElement(-1, Integer.toString(mMinSpeedLimit), 24, Color.RED, mPosition.x - SPEED_OFFSET_LEFT, mPosition.y, true, HUD.NOT_UNIQUE_ELEMENT);
		} else if(mType.equals(NODE_TYPE_DEAD)){
			int id = R.drawable.node_dead;
			Texture texture = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(id);
			mSprite = new Sprite(texture, 1);
		} else if(mType.equals(NODE_TYPE_KEY)) {
			int[] ids = {R.drawable.grey_node, R.drawable.yellow_node, R.drawable.green_node};
			Texture[] textures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(ids);
			mSprite = new Sprite(textures, 1);
		} else if(mType.equals(NODE_TYPE_GATE)) {
			int[] ids = {R.drawable.grey_node, R.drawable.yellow_node, R.drawable.green_node};
			Texture[] textures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(ids);
			mSprite = new Sprite(textures, 1);
		} else {
			int[] ids = {R.drawable.grey_node, R.drawable.yellow_node, R.drawable.green_node};
			Texture[] textures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(ids);
			mSprite = new Sprite(textures, 1);
		}

		mSprite.setPosition(vec.x, vec.y);
		mSprite.setTextureIndex(0);
		
		for(int h = 0; h < mTrackIdArray.length; h++) {
			mTrackIdArray[h] = -1;
		}

		//Log.d("DEBUG", "Node placed at: (" + i + ", " + j + ") ");
	}
	
	public void setType(String type) {
		mType = type;
	}
	
	public String getType() {
		return mType;
	}
	
	public int getMinSpeedLimit() {
		return mMinSpeedLimit;
	}
	
	public int getMaxSpeedLimit() {
		return mMaxSpeedLimit;
	}
	
	/**
	 * @return	the position this node is located at in game pixels
	 */
	public Vector2 getPosition() {
		return mPosition;
	}
	
	/**
	 * create a connection to another node
	 * 
	 * @param i			the i index of the other node
	 * @param j			the j index of the other node
	 * @param k			the k index of the other node (used for the border nodes)
	 * @param trackID	the id for the track segment that represents the visual component of a connection
	 */
	public void setConnection(int i, int j, int k, int trackID, boolean fixed) {
		for (int p = 0; p < mNodeConnections.length; p++) {
			if (mNodeConnections[p].isEmptyNodeConnection()) {
				mNodeConnections[p] = new NodeConnection(i, j, k);
				mNodeConnections[p].setTrackID(trackID);
				mNodeConnections[p].setFixed(fixed);
				mNumCurrentConnections++;
				break;
			}
		}
		conditionallyRemovePreferredConnection();
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
		if(mPreferredConnection != null && mPreferredConnection.getI() == i && mPreferredConnection.getJ() == j) {
			conditionallyRemovePreferredConnection();
		}
	}

	/**
	 * remove all non-border node connections from this node
	 */
	public void removeAllConnections() {
		for (int i = 0; i < mNodeConnections.length; i++) {
			if(mNodeConnections[i].getI() != -1 || mNodeConnections[i].getJ() != -1 || mNodeConnections[i].getK() != -1) {
				if(!mNodeConnections[i].getFixed()) {
					mNodeConnections[i] = new NodeConnection(-1, -1, -1);
					mNumCurrentConnections--;
				}
			}
		}
		conditionallyRemovePreferredConnection();
	}
	
	public void conditionallyRemovePreferredConnection() {
		if(mPreferredConnection != null) {
			String uniqueID = Integer.toString(getI()) + Integer.toString(getJ());
			HUD.getInstance().removeElement(uniqueID);
			mPreferredConnection = null;
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
	
	/**
	 * does this node have an existing connection to the node connection passed in
	 * 
	 * @param node
	 * @return
	 */
	public boolean hasConnectionTo(int indexI, int indexJ) {
		boolean result = false;
		NodeConnection[] currentNodes = getConnections();
		for(int i = 0; i < currentNodes.length; i++) {
			if(currentNodes[i].getI() == indexI && currentNodes[i].getJ() == indexJ) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * when this node has multiple possible connections, it will prefer to the one passed in now
	 * 
	 * @param node
	 */
	public void setPreferredConnection(NodeConnection nodeStart, NodeConnection nodeEnd) {
		mPreferredConnection = nodeEnd;
		double angle = ANGLE_EAST;
		float posX = 0;
		float posY = 0;
		int originI = nodeStart.getI();
		int originJ = nodeStart.getJ();
		int yOriginOffset = PREFERRED_ARROW_OFFSET;
		int xOriginOffset = PREFERRED_ARROW_OFFSET;
		if(originI == getI()) {
			if(originJ < getJ()) {
				yOriginOffset *= -1;
			}
		} else {
			if(originI < getI()) {
				xOriginOffset *= -1;
			}
		}
		if(mPreferredConnection.getJ() == getJ()) {
			if(mPreferredConnection.getI() > getI()) {
				angle = ANGLE_EAST;
				posX = mPosition.x + PREFERRED_ARROW_OFFSET;
				posY = mPosition.y + yOriginOffset;
			} else {
				angle = ANGLE_WEST;
				posX = mPosition.x - PREFERRED_ARROW_OFFSET;
				posY = mPosition.y + yOriginOffset;
			}
		} else {
			if(mPreferredConnection.getJ() > getJ()) {
				angle = ANGLE_SOUTH;
				posX = mPosition.x + xOriginOffset;
				posY = mPosition.y + PREFERRED_ARROW_OFFSET;
			} else {
				angle = ANGLE_NORTH;
				posX = mPosition.x + xOriginOffset;
				posY = mPosition.y - PREFERRED_ARROW_OFFSET;
			}
		}
		Texture texture = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.arrow);
		String uniqueID = Integer.toString(getI()) + Integer.toString(getJ());
		HUD.getInstance().addElement(-1, texture, posX, posY, angle, 300, true, uniqueID);
		//Log.d("DEBUG", "Node: (" + getI() + ", " + getJ() + ") now prefers to connect to node: (" + mPreferredConnection.getI() + ", " + mPreferredConnection.getJ() + ")");
	}
	
	/**
	 * @return	this node's preferred connection
	 */
	public NodeConnection getPreferredConnection() {
		return mPreferredConnection;
	}

	// TODO - remove
	public void removePower() {
		mSprite.setTextureIndex(0);
	}

	@Override
	public void update(long timeDelta) {
		sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
	}
}