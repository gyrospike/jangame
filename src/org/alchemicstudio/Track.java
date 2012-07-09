package org.alchemicstudio;

import android.graphics.Color;
import android.util.Log;

public class Track extends BaseObject {

	/** the nodes that make up the track */
	private Node[][] mNodes = null;
	
	/** the nodes that make up the outside of the track */
	private Node[] mBorderNodes = null;
	
	/** the spark that traverses the track */
	private Spark mSpark = null;
	
	/** the node at which the start begins it's journey */
	private Node mStartNode = null;
	
	/** the node the spark just departed from */
	private Node mPreviousNode = null;
	
	public Track() {
		mSpark = new Spark();
		
		HUD.getInstance().addTextElement(-1, "Spark Speed: 0", 24, Color.YELLOW, 500, 40, true, "sparkSpeed");
	}
	
	/**
	 * load the nodes
	 * 
	 * @param nodes
	 */
	public void loadNodes(Node[][] nodes, Node[] borderNodes) {
		mNodes = nodes;
		mBorderNodes = borderNodes;
		int len = borderNodes.length;
		for(int i = 0; i < len; i++) {
			if(borderNodes[i].getType().equals(Node.NODE_TYPE_START)) {
				mStartNode = borderNodes[i];
			}
		}
	}
	
	/**
	 * reset the spark
	 */
	public void resetSpark() {
		mSpark.resetSpark();
	}
	
	/**
	 * release the spark
	 */
	public void releaseSpark() {
		mSpark.resetSpark();
		if(mStartNode != null) {
			mSpark.setPosition(mStartNode.getPosition().x, mStartNode.getPosition().y);
			mSpark.setTarget(resolveNextTargetNode(mStartNode));
			mSpark.setStartingSpeed(Spark.STARTING_VELOCITY);
		} else {
			Log.e("ERROR", "no start node set");
		}
		
	}
	
	@Override
	public void update(long timeDelta) {
		if(mSpark.getReleased()) {
			mSpark.update(timeDelta);
			if(mSpark.getReadyForNextTarget()) {
				Node currentNode = mSpark.getTarget();
				if(canAdvancePastThisNode(currentNode)) {
					mSpark.setTarget(resolveNextTargetNode(currentNode));
					mSpark.updateSprite(timeDelta);
				} else {
					resetSpark();
				}
			}
		}
	}
	
	private boolean canAdvancePastThisNode(Node currentNode) {
		boolean result = false;
		if(currentNode.getType().equals(Node.NODE_TYPE_SPEED_TRAP)) {
			double speed = mSpark.getCurrentSpeed();
			if(currentNode.getMinSpeedLimit() < speed && currentNode.getMaxSpeedLimit() > speed) {
				result = true;
			}
		} else {
			result = true;
		}
		return result;
	}
	
	/**
	 * Figure out what the next node connection will be from any given node
	 * 
	 * @param currentNode
	 * @return
	 */
	private Node resolveNextTargetNode(Node currentNode) {
		Node result = null;
		NodeConnection pConnection = currentNode.getPreferredConnection();
		// don't add the connection to this node that the spark just came from
		if(pConnection != null && (mPreviousNode.getI() != pConnection.getI() || mPreviousNode.getJ() != pConnection.getJ())) {
			result = getNodeFromConnection(pConnection);
		} else {
			NodeConnection[] connections = currentNode.getConnections();
			int lastResortNodeIndex = -1;
			for(int i = 0; i < connections.length; i++) {
				// if there is no previous connection we assume we are just starting
				if(mPreviousNode == null) {
					result = getNodeFromConnection(connections[i]);
				// if there is a node straight ahead, take that one, ^ is the exclusive or (XOR) operator in java
				} else if(mPreviousNode.getI() == connections[i].getI() ^ mPreviousNode.getJ() == connections[i].getJ()) {
					result = getNodeFromConnection(connections[i]);
					break;
				// else use any node not including the one you just came from
				} else if(mPreviousNode.getI() != connections[i].getI() || mPreviousNode.getJ() != connections[i].getJ()) {
					lastResortNodeIndex = i;
				}
			}
			if(result == null) {
				if(lastResortNodeIndex > -1) {
					result = getNodeFromConnection(connections[lastResortNodeIndex]);
				} else {
					Log.d("DEBUG", "spark reached the end of the line");
					mSpark.resetSpark();
				}
			}
		}
		mPreviousNode = currentNode;
		return result;
	}
	
	/**
	 * @param nC	node connection
	 * @return		node that matches the node connection passed in
	 */
	private Node getNodeFromConnection(NodeConnection nC) {
		Node result = null;
		if(nC.getK() == -1) {
			result = mNodes[nC.getI()][nC.getJ()];
		} else {
			result = mBorderNodes[nC.getK()];
		}
		return result;
	}
	
}
