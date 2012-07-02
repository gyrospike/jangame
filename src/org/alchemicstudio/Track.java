package org.alchemicstudio;

import android.graphics.Point;
import android.util.Log;

public class Track extends BaseObject {

	/** the nodes that make up the track */
	private Node[][] mNodes = null;
	
	/** the spark that traverses the track */
	private Spark mSpark = null;
	
	private Node mStartNode = null;
	
	private Node mEndNode = null;
	
	private Node mPreviousNode = null;
	
	public Track() {
		
	}
	
	/**
	 * load the nodes
	 * 
	 * @param nodes
	 */
	public void loadNodes(Node[][] nodes, Node[] borderNodes) {
		mNodes = nodes;
		int len = borderNodes.length;
		for(int i = 0; i < len; i++) {
			if(borderNodes[i].getOrder() == Node.BORDER_TYPE_START) {
				mStartNode = borderNodes[i];
			} else if(borderNodes[i].getOrder() == Node.BORDER_TYPE_END) {
				mEndNode = borderNodes[i];
			}
		}
	}
	
	/**
	 * release the spark
	 */
	public void releaseSpark() {
		mSpark = new Spark();
		mSpark.setPosition(mStartNode.getPosition().x, mStartNode.getPosition().y);
		mSpark.setTarget(resolveNextTargetNode(mStartNode));
	}
	
	@Override
	public void update(float timeDelta) {
		if(mSpark != null) {
			mSpark.update(timeDelta);
			if(mSpark.readyForNextTarget()) {
				mSpark.setTarget(resolveNextTargetNode(mSpark.getTarget()));
			}
		}
	}
	
	/**
	 * Figure out what the next node connection will be from any given node
	 * 
	 * @param currentNode
	 * @return
	 */
	private Node resolveNextTargetNode(Node currentNode) {
		NodeConnection result = null;
		NodeConnection pConnection = currentNode.getPreferredConnection();
		// don't add the connection to this node that the spark just came from
		if(pConnection != null && (mPreviousNode.getI() != pConnection.getI() || mPreviousNode.getJ() != pConnection.getJ())) {
			result = pConnection;
		} else {
			NodeConnection[] connections = currentNode.getConnections();
			for(int i = 0; i < connections.length; i++) {
				// if there is no previous connection we assume we are just starting
				if(mPreviousNode == null) {
					result = connections[i];
				} else if (mPreviousNode.getI() != connections[i].getI() || mPreviousNode.getJ() != connections[i].getJ()) {
					result = connections[i];
					break;
				}
			}
		}
		mPreviousNode = currentNode;
		return getNodeFromConnection(result);
	}
	
	/**
	 * @param nC	node connection
	 * @return		node that matches the node connection passed in
	 */
	private Node getNodeFromConnection(NodeConnection nC) {
		return mNodes[nC.getI()][nC.getJ()];
	}
	
}
