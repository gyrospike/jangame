package org.alchemicstudio;

import android.util.Log;

public class Grid extends BaseObject {
	
	/** the array index of the track segment used for visualizing where your current track piece will connect */
	private final static int POINTER_TRACK_SEGMENT_INDEX = 0;
	
	/** number of sparks to be released on touch */
	private final static int SPARKS_PER_TOUCH = 5;
	
	/** max number of particles we can have on screen */
	private final static int MAX_PARTICLE_ARRAY_SIZE = 30;
	
	/** TODO - what? */
	private final static float TRACK_SCALAR = (7.0f / 27.0f);
	
	/** node size */
	private final static float NODE_DIMENSION = 32.0f;
	
	/** dragable track offset x */
	private final static float TRACK_OFFSET_X = 8.0f;
	
	/** dragable track offset y */
	private final static float TRACK_OFFSET_Y = 14.0f;
	
	/** the amount to hide the border nodes beyond the screen by */
	private final static float BORDER_NODE_OFFSET = 10.0f;

	/** current index for particle array */
	private int mParticleIndex = 0;

	/** current number of track segments we have */
	private int mNumTrackSegments = 1;
	
	/** maximum number of track segments we'll have to allocate, based on size of grid passed in */
	private int mMaxTrackSegments;
	
	/** the base unit that track segments connect between */
	private Node[][] mNodes;
	
	/** the base unit that surrounds the game board */
	private Node[] mBorderNodes;
	
	/** the array of track segments */
	private TrackSegment[] mTrackSegments;

	/** the space between nodes */
	private int mSpacing;
	
	/** height in nodes of this grid */
	private int mHeight;
	
	/** width in nodes of this grid */
	private int mWidth;

	/** the i index for the node being used as the source for the current track segment */
	private int mCurrentTrackSourceNodeI;
	
	/** the j index for the node being used as the source for the current track segment */
	private int mCurrentTrackSourceNodeJ;

	/** the screen width */
	private float mScreenWidth;
	
	/** the screen height */
	private float mScreenHeight;
	
	/** how much buffer space we have in the x direction */
	private float mSideBufferX;
	
	/** how much buffer space we have in the y direction */
	private float mSideBufferY;
	
	/** array of particles */
	private Particle[] mParticleArray;
	
	public Grid(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		mHeight = dataSet.mMapHeight;
		mWidth = dataSet.mMapWidth;
		mSpacing = dataSet.mMapSpacing;

		mMaxTrackSegments = (mWidth * mHeight) * 4;

		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;

		mNodes = new Node[mWidth][mHeight];
		mBorderNodes = new Node[2*mWidth + 2*mHeight];
		mTrackSegments = new TrackSegment[mMaxTrackSegments];
		// this has to happen before creeateTrackSegmentBetweenPoints
		for (int k = 0; k < mMaxTrackSegments; k++) {
			mTrackSegments[k] = new TrackSegment();
			mTrackSegments[k].mSprite.setScale(0.0f, 0.0f);
		}
		mTrackSegments[POINTER_TRACK_SEGMENT_INDEX].setInUse(true);

		mSideBufferX = (screenWidth - ((mWidth*NODE_DIMENSION) + ((mWidth-1)*mSpacing)))/2;
		mSideBufferY = (screenHeight - ((mHeight*NODE_DIMENSION) + ((mHeight-1)*mSpacing)))/2;
		
		for (int k = 0; k < dataSet.mNodes.getCount(); k++) {
			int tempI = dataSet.mNodes.get(k).i;
			int tempJ = dataSet.mNodes.get(k).j;

			int maxSpeedLimit = dataSet.mNodes.get(k).maxSpeed;
			int minSpeedLimit = dataSet.mNodes.get(k).minSpeed;
			int link = dataSet.mNodes.get(k).link;
			int type = dataSet.mNodes.get(k).type;
			
			Boolean isFixed =  dataSet.mNodes.get(k).fixed;

			Vector2 nodePosition = new Vector2(mSideBufferX + ((mSpacing + NODE_DIMENSION) * tempI), mSideBufferY + ((mSpacing + NODE_DIMENSION) * tempJ));
			mNodes[tempI][tempJ] = new Node(tempI,tempJ, nodePosition, false, false, isFixed, maxSpeedLimit, minSpeedLimit, link, type);
		}
		
		Vector2[] nodePositions = getBorderNodePositions();
		for (int m = 0; m < dataSet.mBorderNodes.getCount(); m++) {

			Boolean isStartNode =  dataSet.mBorderNodes.get(m).start;
			Boolean isEndNode =  dataSet.mBorderNodes.get(m).end;
			Boolean isFixed =  dataSet.mBorderNodes.get(m).fixed;

			Vector2 nodePosition = nodePositions[m];
			mBorderNodes[m] = new Node(-1,-1, nodePosition, isStartNode, isEndNode, isFixed, 0, 0, 0, 0);
		}
		
		for (int k = 0; k < dataSet.mNodes.getCount(); k++) {
			
			int tempI = dataSet.mNodes.get(k).i;
			int tempJ = dataSet.mNodes.get(k).j;
			
			for(int p = 0; p < dataSet.mNodes.get(k).pretargets.getCount(); p++) {
				int borderIndex = dataSet.mNodes.get(k).pretargets.get(p).borderIndex;
				conditionallyCreateConnectionWithBorder(tempI, tempJ, borderIndex);
			}
		}

		mParticleArray = new Particle[MAX_PARTICLE_ARRAY_SIZE];
		for (int i = 0; i < MAX_PARTICLE_ARRAY_SIZE; i++) {
			mParticleArray[i] = new Particle();
		}
	}

	/**
	 * Creates a shower of spark particles at the passed on position 
	 *
	 * @param x		x position
	 * @param y		y position
	 * @param num	how many particles to make
	 */
	private void createParticle(int x, int y, int num) {
		if (mParticleArray != null) {
			for (int i = 0; i < num; i++) {
				mParticleArray[mParticleIndex].createParticle(x, y);
				mParticleIndex++;
				if (mParticleIndex > mParticleArray.length - 1)
					mParticleIndex = 0;
			}
		}
	}
	
	/**
	 * Start at node[0][0] and place the first border node directly to the north, then proceed heading west,
	 * then turn south, then east, then north again placing the border nodes just off the screen but even with
	 * the game board nodes
	 * 
	 * @return
	 */
	private Vector2[] getBorderNodePositions() {
		Vector2[] result = new Vector2[mBorderNodes.length];
		int index = 0;
		// North
		for( int i = 0; i < mWidth; i++) {
			float x = mNodes[i][0].mSprite.getPosition().x;
			float y = -BORDER_NODE_OFFSET;
			result[index] = new Vector2(x, y);
			index++;
		}
		// East
		for( int i = 0; i < mHeight; i++) {
			float x = mScreenWidth + BORDER_NODE_OFFSET;
			float y = mNodes[mWidth-1][i].mSprite.getPosition().y;
			result[index] = new Vector2(x, y);
			index++;
		}
		// South
		for( int i = mWidth-1; i > -1; i--) {
			float x = mNodes[i][mHeight-1].mSprite.getPosition().x;
			float y = mScreenHeight + BORDER_NODE_OFFSET;
			result[index] = new Vector2(x, y);
			index++;
		}
		// West
		for( int i = mHeight-1; i > -1; i--) {
			float x = -BORDER_NODE_OFFSET; 
			float y = mNodes[0][i].mSprite.getPosition().y;
			result[index] = new Vector2(x, y);
			index++;
		}
		return result;
	}
	
	/**
	 * creates a segment of track between a game board node and a border node
	 * 
	 * @param ai
	 * @param aj
	 * @param borderIndex
	 */
	private void conditionallyCreateConnectionWithBorder(int ai, int aj, int borderIndex) {
		if (mNumTrackSegments < mMaxTrackSegments) {
			
			float ax = mNodes[ai][aj].mSprite.getPosition().x + TRACK_OFFSET_X;
			float ay = mNodes[ai][aj].mSprite.getPosition().y - TRACK_OFFSET_Y;
			
			float bx = mBorderNodes[borderIndex].mSprite.getPosition().x + TRACK_OFFSET_X;
			float by = mBorderNodes[borderIndex].mSprite.getPosition().y - TRACK_OFFSET_Y;
			
			int trackID = createTrackSegmentBetweenPoints(ax, ay, bx, by);
			
			mNodes[ai][aj].setConnection(-1, -1, borderIndex, trackID);
		}
	}

	/**
	 * create a segment of track between the two node indicies passed in, 
	 * 
	 * @param ai				node a, i index
	 * @param aj				node a, j index
	 * @param bi				node b, i index
	 * @param bj				node b, j index
	 * @param offScreenTrack	if true then can't remove the track and has special case logic TODO - should be data driven
	 */
	private void conditionallyCreateConnectionBetweenNodes(int ai, int aj, int bi, int bj, boolean offScreenTrack) {
		if(!mNodes[ai][aj].hasMaxConnections()) {
			if(!mNodes[bi][bj].hasMaxConnections()) {
				if(!connectionBetween(ai, aj, bi, bj)) {
					if (mNumTrackSegments < mMaxTrackSegments) {

						float ax = mNodes[ai][aj].mSprite.getPosition().x + TRACK_OFFSET_X;
						float ay = mNodes[ai][aj].mSprite.getPosition().y - TRACK_OFFSET_Y;

						float bx = mNodes[bi][bj].mSprite.getPosition().x + TRACK_OFFSET_X;
						float by = mNodes[bi][bj].mSprite.getPosition().y - TRACK_OFFSET_Y;

						int trackID = createTrackSegmentBetweenPoints(ax, ay, bx, by);

						mNodes[ai][aj].setConnection(bi,bj,-1,trackID);
						mNodes[bi][bj].setConnection(ai,aj,-1,trackID);

						Log.d("DEBUG", "Created connection between: ("+ai+","+aj+") and ("+bi+","+bj+")");
					} else {
						Log.d("DEBUG", "Failed to create a connection between: ("+ai+","+aj+") and ("+bi+","+bj+") - reached max track segments");
					}
				} else {
					Log.d("DEBUG", "Failed to create a connection between: ("+ai+","+aj+") and ("+bi+","+bj+") - a connection already existed");
				}
			} else {
				Log.d("DEBUG", "Failed to create a connection between: ("+ai+","+aj+") and ("+bi+","+bj+") - reached ("+bi+","+bj+")'s connection limit");
			}
		} else {
			Log.d("DEBUG", "Failed to create a connection between: ("+ai+","+aj+") and ("+bi+","+bj+") - reached ("+ai+","+aj+")'s connection limit");
		}
	}

	/**
	 * create a track segment between two points
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return
	 */
	private int createTrackSegmentBetweenPoints(float ax, float ay, float bx, float by) {
		int result = -1;
		float distance = Math.abs(new Vector2(bx, by).distance(new Vector2(ax, ay)));
		
		double angle= Math.atan((by - ay)/(ax - bx)) - (Math.PI / 2);
		if((ax-bx) < 0) angle += Math.PI;

		// start at index 1 here as we use the segment at position 0 for the "pointer track"
		for (int i = 1; i < mMaxTrackSegments; i++) {
			if (mTrackSegments[i].getInUse() == false) {
				result = i;
				mTrackSegments[i].mSprite.setPosition(bx, by);
				mTrackSegments[i].mSprite.setScale(1.0f, distance * TRACK_SCALAR);
				mTrackSegments[i].mSprite.setRotation((float) angle);
				mTrackSegments[i].setInUse(true);
				mNumTrackSegments++;
				break;
			}
		}
		return result;
	}

	/**
	 * does a connection exist between these two nodes, indicies provided
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return		true if a connection exists between the two passed in nodes
	 */
	private boolean connectionBetween(int x1, int y1, int x2, int y2) {
		Boolean result = false;
		NodeConnection[] pArray = mNodes[x1][y1].getConnections();
		for (int i = 0; i < pArray.length; i++) {
			if (pArray[i].hasValueOf(x2, y2,-1)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * handle touch up on different nodes
	 * 
	 * @param i		the i index for node being touched up
	 * @param j		the j index for node being touched up
	 */
	private void nodeReleased(int i, int j) {
		Log.d("DEBUG", "Connection between: " + connectionBetween(mCurrentTrackSourceNodeI, mCurrentTrackSourceNodeJ, i, j));
		
		// if the user clicked a released the same node
		if (mCurrentTrackSourceNodeI == i && mCurrentTrackSourceNodeJ == j) {
			deactivateNode(i, j);
			createParticle((int) (mNodes[i][j].mSprite.getPosition().x + 16.0f), (int) (mNodes[i][j].mSprite.getPosition().y - 16.0f), SPARKS_PER_TOUCH);
		}
		// if the user clicked a node and released on a node to the direct left or right, or above or below (just not diagonal)
		else if ((mCurrentTrackSourceNodeI == i && mCurrentTrackSourceNodeJ != j) || (mCurrentTrackSourceNodeI != i && mCurrentTrackSourceNodeJ == j)) {
			if (mCurrentTrackSourceNodeI == i) {
				int difJ = Math.abs(mCurrentTrackSourceNodeJ - j);
				int smallerJ = Math.min(j, mCurrentTrackSourceNodeJ);
				for (int p = 1; p <= difJ; p++) {
					conditionallyCreateConnectionBetweenNodes(i, smallerJ + p, i, smallerJ + p - 1, false);
				}
			} else if (mCurrentTrackSourceNodeJ == j) {
				int difI = Math.abs(mCurrentTrackSourceNodeI - i);
				int smallerI = Math.min(i, mCurrentTrackSourceNodeI);
				for (int p = 1; p <= difI; p++) {
					conditionallyCreateConnectionBetweenNodes(smallerI + p, j, smallerI + p - 1, j, false);
				}
			}
		}
	}
	
	/**
	 * deactivate the node specified by index, deactivate that node and remove all connections to it from other nodes
	 * 
	 * @param i
	 * @param j
	 */
	private void deactivateNode(int i, int j) {
		NodeConnection[] nodeConnections = mNodes[i][j].getConnections();
		for(int n = 0; n < nodeConnections.length; n++) {
			//TODO - this makes it so you could never remove a border connection, this should be shapped up a bit
			if(nodeConnections[n].getI() != -1 && nodeConnections[n].getJ() != -1) {
				mNodes[nodeConnections[n].getI()][nodeConnections[n].getJ()].removeConnection(i, j);
				mTrackSegments[nodeConnections[n].getTrackID()].setInUse(false);
			}
		}
		
		mNodes[i][j].removeAllConnections();
	}

	/**
	 * translates a position (x,y) the user touches to the nearest node
	 * 
	 * @param x		position user is touching, x
	 * @param y		position user is touching, y
	 * @return		Point that is closest to the above coordinates
	 */
	private NodeConnection determineNodeTouched(int x, int y) {
		Log.d("DEBUG", "x, y : " + x + ", " + y);
		Log.d("DEBUG", "mSpacing : " + mSpacing);
		Log.d("DEBUG", "xSideBuffer, ySideBuffer : " + mSideBufferX + ", " + mSideBufferY);
		int xIndex = (int) Math.round((x - mSideBufferX) / (mSpacing + 32));
		int yIndex = (int) Math.round((y - mSideBufferY) / (mSpacing + 32));
		Log.d("DEBUG", "xIndex, yIndex : " + xIndex + ", " + yIndex);

		if (xIndex < 0) {
			xIndex = 0;
		} else if (xIndex > mWidth - 1) {
			xIndex = mWidth - 1;
		}
		
		if (yIndex < 0) {
			yIndex = 0;
		} else if (yIndex > mHeight - 1) {
			yIndex = mHeight - 1;
		}

		return new NodeConnection(xIndex, yIndex, -1);
	}
	
	/**
	 * updates the position of the current track being laid
	 * 
	 * @param x		the position of the user's finger, x
	 * @param y		the position of the user's finger, y
	 */
	public void updateTrackDrag(int x, int y) {
		Vector2 dragPoint = new Vector2(x, y);
		Node tempNode = mNodes[mCurrentTrackSourceNodeI][mCurrentTrackSourceNodeJ];

		//Log.d("DEBUG", "current track i, j: " + mCurrentTrackSourceNodeI + ", " + mCurrentTrackSourceNodeJ);
		
		float trackOriginX = tempNode.mSprite.getPosition().x + TRACK_OFFSET_X;
		float trackOriginY = tempNode.mSprite.getPosition().y - TRACK_OFFSET_Y;
		
		double angle= Math.atan((dragPoint.y - trackOriginY)/(trackOriginX - dragPoint.x)) - (Math.PI / 2);
		//.02 tolerance stops the angle from exploding to infinity and the track switching signs
		if((dragPoint.x - trackOriginX) <= .02) angle += Math.PI;

		float distance = dragPoint.distance(new Vector2(trackOriginX, trackOriginY));

		mTrackSegments[POINTER_TRACK_SEGMENT_INDEX].mSprite.setPosition(trackOriginX, trackOriginY);
		mTrackSegments[POINTER_TRACK_SEGMENT_INDEX].mSprite.setScale(1.0f, distance * TRACK_SCALAR);
		mTrackSegments[POINTER_TRACK_SEGMENT_INDEX].mSprite.setRotation((float) angle);
	}
	
	/**
	 * set the node pressed as the current track source node
	 * 
	 * @param x		position user touched, x
	 * @param y		position user touched, y
	 */
	public void checkNodePress(int x, int y) {
		NodeConnection node = determineNodeTouched(x, y);
		mCurrentTrackSourceNodeI = node.getI();
		mCurrentTrackSourceNodeJ = node.getJ();
		Log.d("DEBUG", "You pressed node: (" + mCurrentTrackSourceNodeI + ", " + mCurrentTrackSourceNodeJ + ")");
	}

	/**
	 * set the user removed his finger from to 'released'
	 * 
	 * @param x
	 * @param y
	 */
	public void checkNodeRelease(int x, int y) {
		NodeConnection node = determineNodeTouched(x, y);
		mTrackSegments[0].mSprite.setScale(0.0f, 0.0f);
		nodeReleased(node.getI(),node.getJ());
	}
	
	/**
	 * @return	the nodes of the grid
	 */
	public Node[][] getNodes() {
		return mNodes;
	}

	@Override
	public void update(float timeDelta) {	
		
		for(int w = 0; w < mNodes.length; w++) {
			for(int q = 0; q < mNodes[w].length; q++) {
				mNodes[w][q].update(timeDelta);
			}
		}
		
		/*
		 * Uncomment this when you need to debug the border nodes, also remember to set the offset
		 * BORDER_NODE_OFFSET = -50.0f seems to work
		 * 
		for(int q = 0; q < mBorderNodes.length; q++) {
			mBorderNodes[q].update(timeDelta);
		}
		*/
		
		for(int e = 0; e < mTrackSegments.length; e++) {
			mTrackSegments[e].update(timeDelta);
		}
		
		for(int r = 0; r < mParticleArray.length; r++) {
			mParticleArray[r].update(timeDelta);
		}
	}
}
