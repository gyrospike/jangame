package org.alchemicstudio;


import android.graphics.Point;
import android.util.Log;

public class Grid extends BaseObject {

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

	/** current index for particle array */
	private int mParticleIndex = 0;

	/** current number of track segments we have */
	private int mNumTrackSegments = 1;
	
	/** maximum number of track segments we'll have to allocate, based on size of grid passed in */
	private int mMaxTrackSegments;
	
	/** the base unit that track segments connect between */
	private Node[][] mNodes;
	
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
	
	/** how much buffer space we have in the x direction */
	private float mSideBufferX;
	
	/** how much buffer space we have in the y direction */
	private float mSideBufferY;
	
	/** array of particles */
	private Particle[] mParticleArray;
	
	public Grid(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		mHeight = dataSet.mapHeight;
		mWidth = dataSet.mapWidth;
		mSpacing = dataSet.mapSpacing;

		int onRampX = dataSet.sourceAX;
		int onRampY = dataSet.sourceAY;
		int offRampX = dataSet.sourceBX;
		int offRampY = dataSet.sourceBY;
		
		mMaxTrackSegments = (mWidth - 1) * mWidth * (mHeight - 1) + 2;

		mScreenWidth = screenWidth;

		mNodes = new Node[mWidth][mHeight];
		mTrackSegments = new TrackSegment[mMaxTrackSegments];

		mSideBufferX = (screenWidth - ((mWidth*NODE_DIMENSION) + ((mWidth-1)*mSpacing)))/2;
		mSideBufferY = (screenHeight - ((mHeight*NODE_DIMENSION) + ((mHeight-1)*mSpacing)))/2;
		
		for (int i = 0; i < mWidth; i++) {
			for (int j = 0; j < mHeight; j++) {
				int type = 0;
				int link = 0;
				float minSpeedLimit = 0.0f;
				float maxSpeedLimit = 0.0f;
				boolean isRampNode = false;
				for (int k = 0; k < dataSet.specialNodes.getCount(); k++) {
					int tempI = dataSet.specialNodes.get(k).i;
					int tempJ = dataSet.specialNodes.get(k).j;
					
					if(tempI == i && tempJ == j) {
						isRampNode = true;
						maxSpeedLimit = dataSet.specialNodes.get(k).maxSpeed;
						minSpeedLimit = dataSet.specialNodes.get(k).minSpeed;
						link = dataSet.specialNodes.get(k).link;
						type = dataSet.specialNodes.get(k).type;
						break;
					}
				}
				
				mNodes[i][j] = new Node(i, j, new Vector2(mSideBufferX + ((mSpacing + NODE_DIMENSION) * i), mSideBufferY + ((mSpacing + NODE_DIMENSION) * j)), 
						isRampNode, maxSpeedLimit, minSpeedLimit, link, type);
			}
		}

		for (int k = 0; k < mMaxTrackSegments; k++) {
			mTrackSegments[k] = new TrackSegment();
			mTrackSegments[k].mSprite.setScale(0.0f, 0.0f);
		}

		createTrackSegment(onRampX, onRampY, 0, 0, true);
		createTrackSegment(offRampX, offRampY, 0, 0, true);
		
		mParticleArray = new Particle[MAX_PARTICLE_ARRAY_SIZE];
		for (int i = 0; i < MAX_PARTICLE_ARRAY_SIZE; i++) {
			mParticleArray[i] = new Particle();
		}
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

		float trackOriginX = tempNode.getX() + TRACK_OFFSET_X;
		float trackOriginY = tempNode.getY() - TRACK_OFFSET_Y;

		double angle= Math.atan((dragPoint.y - trackOriginY)/(trackOriginX - dragPoint.x)) - (Math.PI / 2);
		//.02 tolerance stops the angle from exploding to infinity and the track switching signs
		if((dragPoint.x - trackOriginX) <= .02) angle += Math.PI;

		float distance = dragPoint.distance(new Vector2(trackOriginX, trackOriginY));

		mTrackSegments[0].mSprite.setPosition(trackOriginX, trackOriginY);
		mTrackSegments[0].mSprite.setScale(1.0f, distance * TRACK_SCALAR);
		mTrackSegments[0].mSprite.setRotation((float) angle);
	}

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
	 * create a segment of track between the two node indicies passed in, 
	 * 
	 * @param ai				node a, i index
	 * @param aj				node a, j index
	 * @param bi				node b, i index
	 * @param bj				node b, j index
	 * @param offScreenTrack	if true then can't remove the track and has special case logic TODO - should be data driven
	 */
	private void createTrackSegment(int ai, int aj, int bi, int bj, boolean offScreenTrack) {
		if (mNumTrackSegments < mMaxTrackSegments) {
			
			float ax = mNodes[ai][aj].getX() + TRACK_OFFSET_X;
			float ay = mNodes[ai][aj].getY() - TRACK_OFFSET_Y;

			float bx = mNodes[bi][bj].getX() + TRACK_OFFSET_X;
			float by = mNodes[bi][bj].getY() - TRACK_OFFSET_Y;

			if (offScreenTrack) {
				if (ai == 0) {
					bx = -TRACK_OFFSET_X;
					by = ay;
				} else if(ai == mWidth-1){
					bx = mScreenWidth;
					by = ay;
					Log.d("DEBUG", "source B is down: " + bx + ", " + by);
				}
			} else {
				bx = mNodes[bi][bj].getX() + TRACK_OFFSET_X;
				by = mNodes[bi][bj].getY() - TRACK_OFFSET_Y;
			}
			
			float distance = Math.abs(new Vector2(bx, by).distance(new Vector2(ax, ay)));
			
			double angle= Math.atan((by - ay)/(ax - bx)) - (Math.PI / 2);
			if((ax-bx) < 0) angle += Math.PI;

			for (int i = 1; i < mMaxTrackSegments; i++) {
				if (mTrackSegments[i].mSprite.xScale == 0.0f && mTrackSegments[i].mSprite.yScale == 0.0f) {
					if (offScreenTrack) {
						mTrackSegments[i].permanent = true;
					}
					mTrackSegments[i].mSprite.setPosition(bx, by);
					mTrackSegments[i].mSprite.setScale(1.0f, distance * TRACK_SCALAR);
					mTrackSegments[i].mSprite.setRotation((float) angle);
					mTrackSegments[i].setOrigin(bi, bj);
					mTrackSegments[i].setTarget(ai, aj);
					mNumTrackSegments++;
					Log.d("DEBUG", "Wire " + i + " created between: old (" + bi + ", " + bj + ") and new (" + ai + ", " + aj + ")");
					i = mMaxTrackSegments;
				}
			}
		}
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
		Point[] pArray = mNodes[x1][y1].getConnections();
		for (int i = 0; i < pArray.length; i++) {
			if (pArray[i].equals(new Point(x2, y2))) {
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
			createParticle((int) (mNodes[i][j].getX() + 16.0f), (int) (mNodes[i][j].getY() - 16.0f), SPARKS_PER_TOUCH);
		}
		// if the user clicked a node and released on a node to the direct left or right, or above or below (just not diagonal)
		else if ((mCurrentTrackSourceNodeI == i && mCurrentTrackSourceNodeJ != j) || (mCurrentTrackSourceNodeI != i && mCurrentTrackSourceNodeJ == j)) {
			if (mCurrentTrackSourceNodeI == i) {
				int difJ = Math.abs(mCurrentTrackSourceNodeJ - j);
				int smallerJ = Math.min(j, mCurrentTrackSourceNodeJ);
				for (int p = 1; p <= difJ; p++) {
					if (!connectionBetween(i, smallerJ + p, i, smallerJ + p - 1) && !mNodes[i][smallerJ + p].hasMaxConnections() && !mNodes[i][smallerJ + p - 1].hasMaxConnections()) {

						createTrackSegment(i, smallerJ + p, i, smallerJ + p - 1, false);
						mNodes[i][smallerJ + p].setConnection(new Point(i, smallerJ + p - 1), 0);
						mNodes[i][smallerJ + p - 1].setConnection(new Point(i, smallerJ + p), 0);
					}
				}
			} else if (mCurrentTrackSourceNodeJ == j) {
				int difI = Math.abs(mCurrentTrackSourceNodeI - i);
				int smallerI = Math.min(i, mCurrentTrackSourceNodeI);
				for (int p = 1; p <= difI; p++) {
					if (!connectionBetween(smallerI + p, j, smallerI + p - 1, j) && !mNodes[smallerI + p][j].hasMaxConnections() && !mNodes[smallerI + p - 1][j].hasMaxConnections()) {

						createTrackSegment(smallerI + p, j, smallerI + p - 1, j, false);
						mNodes[smallerI + p][j].setConnection(new Point(smallerI + p - 1, j), 0);
						mNodes[smallerI + p - 1][j].setConnection(new Point(smallerI + p, j), 0);
					}
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
		Point temp = new Point(i, j);

		mNodes[i][j].deactivate();

		if (i > 0) {
			Point[] pArray = mNodes[i - 1][j].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i - 1][j].setConnectionNull(m);
					Log.d("DEBUG", "node: (" + (i - 1) + ", " + j + ") set it's (" + i + ", " + j + ") node to null");
				}
			}
		}

		if (i < mWidth - 1) {
			Point[] pArray = mNodes[i + 1][j].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i + 1][j].setConnectionNull(m);
					Log.d("DEBUG", "node: (" + (i + 1) + ", " + j + ") set it's (" + i + ", " + j + ") node to null");
				}
			}
		}

		if (j > 0) {
			Point[] pArray = mNodes[i][j - 1].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i][j - 1].setConnectionNull(m);
					Log.d("DEBUG", "node: (" + i + ", " + (j - 1) + ") set it's (" + i + ", " + j + ") node to null");
				}
			}
		}

		if (j < mHeight - 1) {
			Point[] pArray = mNodes[i][j + 1].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i][j + 1].setConnectionNull(m);
					Log.d("DEBUG", "node: (" + i + ", " + (j + 1) + ") set it's (" + i + ", " + j + ") node to null");
				}
			}
		}

		for (int k = 0; k < mMaxTrackSegments; k++) {
			if (((mTrackSegments[k].targetNode.x == i && mTrackSegments[k].targetNode.y == j) || 
					(mTrackSegments[k].originNode.x == i && mTrackSegments[k].originNode.y == j)) && !mTrackSegments[k].permanent) {
				mTrackSegments[k].setTarget(-1, -1);
				mTrackSegments[k].setOrigin(-1, -1);
				mTrackSegments[k].mSprite.setScale(0.0f, 0.0f);
				mNumTrackSegments--;
			}
		}
	}

	/**
	 * set the node pressed as the current track source node
	 * 
	 * @param x		position user touched, x
	 * @param y		position user touched, y
	 */
	public void checkNodePress(int x, int y) {
		Point myPoint = determineNodeTouched(x, y);
		mCurrentTrackSourceNodeI = myPoint.x;
		mCurrentTrackSourceNodeJ = myPoint.y;
		Log.d("DEBUG", "You pressed node: (" + mCurrentTrackSourceNodeI + ", " + mCurrentTrackSourceNodeJ + ")");
	}

	/**
	 * set the user removed his finger from to 'released'
	 * 
	 * @param x
	 * @param y
	 */
	public void checkNodeRelease(int x, int y) {
		Point myPoint = determineNodeTouched(x, y);
		mTrackSegments[0].mSprite.setScale(0.0f, 0.0f);
		nodeReleased(myPoint.x, myPoint.y);
	}

	/**
	 * translates a position (x,y) the user touches to the nearest node
	 * 
	 * @param x		position user is touching, x
	 * @param y		position user is touching, y
	 * @return		Point that is closest to the above coordinates
	 */
	private Point determineNodeTouched(int x, int y) {
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

		return new Point(xIndex, yIndex);
	}

	@Override
	public void update(float timeDelta) {	
		
		for(int w = 0; w < mNodes.length; w++) {
			for(int q = 0; q < mNodes[w].length; q++) {
				mNodes[w][q].update(timeDelta);
			}
		}
		
		for(int e = 0; e < mTrackSegments.length; e++) {
			mTrackSegments[e].update(timeDelta);
		}
		
		for(int r = 0; r < mParticleArray.length; r++) {
			mParticleArray[r].update(timeDelta);
		}
	}

	/*
	
	private void deactivateAllNodes() {
		for (int i = 1; i < mHeight; i++) {
			for (int j = 1; j < mWidth; j++) {
				mNodes[i][j].deactivate();
			}
		}
	}

	private void printList(FixedSizeArray<Point> list) {
		for (int u = 0; u < list.getCount(); u++) {
			Log.d("DEBUG", "List: (" + list.get(u).x + ", " + list.get(u).y + "), length: " + list.getCount());
		}
	} 
	 
	public void setParticles(Particle[] pArray) {
		mParticleArray = pArray;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public boolean isAdjacent(int x1, int y1, int x2, int y2) {
		if (((x1 + 1 == x2 || x1 - 1 == x2) && (y1 == y2)) || ((y1 + 1 == y2 || y1 - 1 == y2) && (x1 == x2))) {
			return true;
		} else {
			return false;
		}
	}
	*/
}
