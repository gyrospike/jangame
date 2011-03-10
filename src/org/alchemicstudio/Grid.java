package org.alchemicstudio;

import android.graphics.Point;
import android.util.Log;

public class Grid extends BaseObject {

	public float nodeDimension;
	public int maxWireSegments;

	public Spark mSpark;
	public float currentSparkVelocity;
	public Node[][] mNodes;
	public Wire[] mWire;

	private final static int SPARKS_PER_TOUCH = 5;
	private final static float WIRE_SCALER = (10.0f / 27.0f);

	private int mSpacing;
	private int numWires;
	private int mHeight;
	private int mWidth;
	private int currentI;
	private int currentJ;
	private int lastI;
	private int lastJ;
	private int firstIndexI;
	private int firstIndexJ;
	private int maxBestPathLength;
	private int particleIndex;

	private float mScreenWidth;
	private float xSideBuffer;
	private float ySideBuffer;
	private float wireOriginX;
	private float wireOriginY;
	private float timeStep;

	private boolean nodePressed;
	private boolean sparkActive;
	private boolean sparkReset;
	private boolean chooseRandom;
	private boolean circuitCalcDone;

	private FixedSizeArray<Node> circuitList = new FixedSizeArray<Node>(25);
	private FixedSizeArray<FixedSizeArray<Point>> finalList = new FixedSizeArray<FixedSizeArray<Point>>(512);
	private Particle[] particleArray;

	public Grid(int width, int height, int spacing, float nodeDim, float screenWidth, float screenHeight) {
		mHeight = height;
		mWidth = width;
		mSpacing = spacing;
		maxBestPathLength = (mHeight * mWidth) - 1;
		maxWireSegments = (mWidth - 1) * mWidth * (mHeight - 1) + 2;
		nodeDimension = nodeDim;
		mScreenWidth = screenWidth;

		mNodes = new Node[width][height];
		mWire = new Wire[maxWireSegments];
		mSpark = new Spark();
		mSpark.hide();

		sparkActive = false;
		nodePressed = false;
		numWires = 1;
		currentI = 0;
		currentJ = 0;
		lastI = 0;
		lastJ = 0;

		float center = (mSpacing * (width - 1)) + nodeDim;
		xSideBuffer = (screenWidth - (center)) / 2.0f;

		float center2 = (mSpacing * (height - 1)) + nodeDim;
		ySideBuffer = (screenHeight - (center2)) / 2.0f;

		for (int i = 0; i < mWidth; i++) {
			for (int j = 0; j < mHeight; j++) {
				mNodes[i][j] = new Node(i, j, new Vector2(xSideBuffer + (mSpacing * i) + (nodeDimension / 2), ySideBuffer + (mSpacing * j)));
			}
		}

		for (int k = 0; k < maxWireSegments; k++) {
			mWire[k] = new Wire();
			mWire[k].mSprite.setScale(0.0f, 0.0f);
		}

		createWire(0, 0, 0, 0, true);
		createWire(mWidth - 1, mHeight - 1, 0, 0, true);
	}

	public void initialize() {

	}

	public void nodePressed(int i, int j) {
		nodePressed = true;
		firstIndexI = i;
		firstIndexJ = j;
		Log.d("DEBUG", "You pressed node: (" + i + ", " + j + ")");
	}

	public void updateWire(int x, int y) {
		if (nodePressed) {

			Node tempNode = mNodes[firstIndexI][firstIndexJ];
			wireOriginX = tempNode.getX() + ((x - tempNode.getX()) / 2);
			wireOriginY = tempNode.getY() + ((y - tempNode.getY()) / 2);

			double angle = (Math.PI / 2) + Math.atan((mNodes[firstIndexI][firstIndexJ].getY() - y) / (x - mNodes[firstIndexI][firstIndexJ].getX()));
			Vector2 newPoint = new Vector2(x, y);
			float distance = newPoint.distance(new Vector2(wireOriginX, wireOriginY));
			// Log.d("DEBUG", "angle: " + angle + ", distance: " + distance);
			mWire[0].mSprite.setPosition(wireOriginX, wireOriginY);
			mWire[0].mSprite.setScale(1.0f, distance * WIRE_SCALER); // really
																		// don't
																		// understand
																		// why I
																		// need
																		// this
																		// value,
																		// about
																		// 1/3
			mWire[0].mSprite.setRotation((float) angle);
		}
	}

	public void createParticle(int x, int y, int type) {
		if (particleArray != null) {
			for (int i = 0; i < type; i++) {
				particleArray[particleIndex].createParticle(x, y);
				particleIndex++;
				if (particleIndex > particleArray.length - 1)
					particleIndex = 0;
			}
		}
	}

	public void createWire(int ai, int aj, int bi, int bj, boolean offScreenWire) {
		if (numWires < maxWireSegments) {

			float ax = mNodes[ai][aj].getX();
			float ay = mNodes[ai][aj].getY();

			float bx = 0.0f;
			float by = 0.0f;

			if (offScreenWire) {
				if (ai == 0 && aj == 0) {
					bx = 0.0f;
					by = ay;
				} else {
					bx = mScreenWidth;
					by = ay;
				}
			} else {
				bx = mNodes[bi][bj].getX();
				by = mNodes[bi][bj].getY();
			}

			float x = ax + ((bx - ax) / 2);
			float y = ay + ((by - ay) / 2);
			float distance = new Vector2(ax, ay).distance(new Vector2(bx, by));
			double angle = (Math.PI / 2) + Math.atan((ay - by) / (ax - bx));

			for (int i = 1; i < maxWireSegments; i++) {
				if (mWire[i].mSprite.xScale == 0.0f && mWire[i].mSprite.yScale == 0.0f) {
					if (offScreenWire) {
						mWire[i].permanent = true;
					}
					mWire[i].mSprite.setPosition(x, y);
					mWire[i].mSprite.setScale(1.0f, distance * 2 / 9);
					mWire[i].mSprite.setRotation((float) angle);
					mWire[i].setOrigin(ai, aj);
					mWire[i].setTarget(bi, bj);
					numWires++;
					Log.d("DEBUG", "Wire " + i + " created between: (" + ai + ", " + aj + ") and (" + bi + ", " + bj + ")");
					i = maxWireSegments;
				}
			}
		}
	}

	public boolean connectionBetween(int x1, int y1, int x2, int y2) {
		Point[] pArray = mNodes[x1][y1].getConnections();
		for (int i = 0; i < pArray.length; i++) {
			if (pArray[i].equals(new Point(x2, y2))) {
				return true;
			}
		}
		return false;
	}

	private void checkCircuit() {
		// clearing old values in preparation for calculating the best path
		Log.d("DEBUG", "clearPaths called from nodeReleased");
		clearPaths();

		checkPowerConnection();
		
		//the below code chunk calculates the most efficient path through the gird, ironically with an extremely inefficient method
		/*
		FixedSizeArray<Point> list = new FixedSizeArray<Point>(maxBestPathLength + 1);
		calculateCircuit(new Point(0, 0), new Point(-1, 0), list);
		if (finalList.getCount() > 0) {
			chooseBestPath();
			circuitCalcDone = true;
		}
		if (circuitCalcDone) {
			releaseSpark();
			circuitCalcDone = false;
		}
		*/
	}

	public void nodeReleased(int i, int j) {
		Log.d("DEBUG", "Connection between: " + connectionBetween(firstIndexI, firstIndexJ, i, j));

		if (firstIndexI == i && firstIndexJ == j) {
			deactivateNode(i, j);
			createParticle((int) mNodes[i][j].getX(), (int) mNodes[i][j].getY(), SPARKS_PER_TOUCH);
		} else if ((firstIndexI == i && firstIndexJ != j) || (firstIndexI != i && firstIndexJ == j)) {
			if (firstIndexI == i) {
				int difJ = Math.abs(firstIndexJ - j);
				int smallerJ = Math.min(j, firstIndexJ);
				for (int p = 1; p <= difJ; p++) {
					if (!connectionBetween(i, smallerJ + p, i, smallerJ + p - 1) && !mNodes[i][smallerJ + p].hasMaxConnections() && !mNodes[i][smallerJ + p - 1].hasMaxConnections()) {

						createWire(i, smallerJ + p, i, smallerJ + p - 1, false);
						mNodes[i][smallerJ + p].setConnection(new Point(i, smallerJ + p - 1), 0);
						mNodes[i][smallerJ + p - 1].setConnection(new Point(i, smallerJ + p), 0);
						checkCircuit();
					}
				}
			} else if (firstIndexJ == j) {
				int difI = Math.abs(firstIndexI - i);
				int smallerI = Math.min(i, firstIndexI);
				for (int p = 1; p <= difI; p++) {
					if (!connectionBetween(smallerI + p, j, smallerI + p - 1, j) && !mNodes[smallerI + p][j].hasMaxConnections() && !mNodes[smallerI + p - 1][j].hasMaxConnections()) {

						createWire(smallerI + p, j, smallerI + p - 1, j, false);
						mNodes[smallerI + p][j].setConnection(new Point(smallerI + p - 1, j), 0);
						mNodes[smallerI + p - 1][j].setConnection(new Point(smallerI + p, j), 0);
						checkCircuit();
					}
				}
			}
		}
		nodePressed = false;
	}

	private void deactivateAllNodes() {
		for (int i = 1; i < mHeight; i++) {
			for (int j = 1; j < mWidth; j++) {
				mNodes[i][j].deactivate();
			}
		}
	}

	private void clearPaths() {
		circuitList.clear();
		for (int j = 0; j < finalList.getCount(); j++) {
			finalList.get(j).clear();
		}
		finalList.clear();
	}

	private void chooseBestPath() {
		int smallestCount = 30;
		int bestIndex = 0;
		int count = 0;
		for (int j = 0; j < finalList.getCount(); j++) {
			count = finalList.get(j).getCount();
			Log.d("DEBUG", "List: " + j + " had count: " + count);
			if (count < smallestCount) {
				smallestCount = count;
				bestIndex = j;
			}
		}
		Log.d("DEBUG", "---BEST LIST---");
		for (int b = 0; b < finalList.get(bestIndex).getCount(); b++) {
			circuitList.add(mNodes[finalList.get(bestIndex).get(b).x][finalList.get(bestIndex).get(b).y]);
		}

		for (int u = 0; u < circuitList.getCount(); u++) {
			Log.d("DEBUG", "Circuit List: (" + circuitList.get(u).iX + ", " + circuitList.get(u).iY + "), length: " + circuitList.getCount());
			circuitList.get(u).activate(2, 0);
		}
	}

	private void printList(FixedSizeArray<Point> list) {
		for (int u = 0; u < list.getCount(); u++) {
			Log.d("DEBUG", "List: (" + list.get(u).x + ", " + list.get(u).y + "), length: " + list.getCount());
		}
	}

	private void calculateCircuit(Point currentPoint, Point lastPoint, FixedSizeArray<Point> list) {
		if (currentPoint.equals(new Point(mWidth - 1, mHeight - 1))) {
			finalList.add(list);
			printList(list);
			Log.d("DEBUG", "Added final point");
		} else if (list.getCount() > maxBestPathLength) {
			Log.d("DEBUG", "List got too big");
		} else {
			Point[] pArray = mNodes[currentPoint.x][currentPoint.y].getConnections();
			Log.d("DEBUG", "Current " + currentPoint + " has: " + pArray[0] + ", " + pArray[1] + ", " + pArray[2] + ", " + pArray[3]);
			int len = 0;
			for (int j = 0; j < pArray.length; j++) {
				if (!pArray[j].equals(new Point(-1, -1))) {
					len++;
				}
			}
			boolean killList = false;
			if (len == 1) {
				list.add(new Point(pArray[0].x, pArray[0].y));
				if (currentPoint.equals(new Point(0, 0)) || currentPoint.equals(new Point(mWidth - 1, mHeight - 1))) {
					calculateCircuit(pArray[0], currentPoint, list);
				} else {
					Log.d("DEBUG", "Dead End at " + currentPoint);
				}
			} else if (len == 2) {
				for (int k = 0; k < len; k++) {
					if (!pArray[k].equals(lastPoint)) {
						killList = false;
						for (int d = 0; d < list.getCount(); d++) {
							if (list.get(d).equals(pArray[k])) {
								killList = true;
								Log.d("DEBUG", "Killing list");
							}
						}
						if (!killList) {
							list.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list);
						}
					}
				}
			} else if (len == 3) {
				FixedSizeArray<Point> list1 = new FixedSizeArray<Point>(maxBestPathLength + 1);

				for (int i = 0; i < list.getCount(); i++) {
					list1.add(list.get(i));
				}

				boolean once = false;
				for (int k = 0; k < len; k++) {
					if (!pArray[k].equals(lastPoint)) {
						killList = false;

						if (!once) {
							for (int d = 0; d < list.getCount(); d++) {
								if (list.get(d).equals(pArray[k])) {
									killList = true;
									Log.d("DEBUG", "Killing list");
								}
							}
							if (!killList) {
								list.add(new Point(pArray[k].x, pArray[k].y));
								calculateCircuit(pArray[k], currentPoint, list);
							}
						}
						if (once) {
							for (int d = 0; d < list1.getCount(); d++) {
								if (list1.get(d).equals(pArray[k])) {
									killList = true;
									Log.d("DEBUG", "Killing list");
								}
							}
							if (!killList) {
								list1.add(new Point(pArray[k].x, pArray[k].y));
								calculateCircuit(pArray[k], currentPoint, list1);
							}
						}
						once = true;
					}
				}
			} else if (len == 4) {
				FixedSizeArray<Point> list1 = new FixedSizeArray<Point>(maxBestPathLength + 1);
				FixedSizeArray<Point> list2 = new FixedSizeArray<Point>(maxBestPathLength + 1);

				for (int i = 0; i < list.getCount(); i++) {
					list1.add(list.get(i));
					list2.add(list.get(i));
				}

				boolean listDone = false;
				boolean list1Done = false;
				boolean list2Done = false;
				for (int k = 0; k < len; k++) {
					if (!pArray[k].equals(lastPoint)) {
						killList = false;

						if (!listDone && list2Done && list1Done) {
							for (int d = 0; d < list.getCount(); d++) {
								if (list.get(d).equals(pArray[k])) {
									killList = true;
									Log.d("DEBUG", "Killing list");
								}
							}
							if (!killList) {
								list.add(new Point(pArray[k].x, pArray[k].y));
								calculateCircuit(pArray[k], currentPoint, list);
								listDone = true;
							}
						}
						if (!list1Done && list2Done) {
							for (int d = 0; d < list1.getCount(); d++) {
								if (list1.get(d).equals(pArray[k])) {
									killList = true;
									Log.d("DEBUG", "Killing list");
								}
							}
							if (!killList) {
								list1.add(new Point(pArray[k].x, pArray[k].y));
								calculateCircuit(pArray[k], currentPoint, list1);
								list1Done = true;
							}
						}
						if (!list2Done) {
							for (int d = 0; d < list2.getCount(); d++) {
								if (list2.get(d).equals(pArray[k])) {
									killList = true;
									Log.d("DEBUG", "Killing list");
								}
							}
							if (!killList) {
								list2.add(new Point(pArray[k].x, pArray[k].y));
								calculateCircuit(pArray[k], currentPoint, list2);
								list2Done = true;
							}
						}
					}
				}
			}
		}
	}

	public void releaseSpark() {
		if (!mSpark.active) {
			Log.d("DEBUG", "Spark Released!");
			sparkActive = true;
			sparkReset = false;
			mSpark.activate(mNodes[0][0].getX(), mNodes[0][0].getY());
		}
	}

	public void completeSpark() {
		Log.d("DEBUG", "Spark Retired!");
		sparkActive = false;
		mSpark.hide();
		// clearPaths();
		// Log.d("DEBUG", "clearPaths called from completeSpark");
	}

	private void checkPowerConnection() {
		for (int q = 0; q < mWidth; q++) {
			for (int p = 0; p < mHeight; p++) {
				mNodes[q][p].removePower();
			}
		}
		checkNodePower(new Point(0, 0), null, 1);
		checkNodePower(new Point(mWidth - 1, mHeight - 1), null, 2);
	}

	private void checkNodePower(Point myPoint, Point lastPoint, int key) {
		if (!mNodes[myPoint.x][myPoint.y].hasPower) {
			mNodes[myPoint.x][myPoint.y].activate(1, key);
			Point[] pArray = mNodes[myPoint.x][myPoint.y].getConnections();
			for (int i = 0; i < pArray.length; i++) {
				if (!pArray[i].equals(new Point(-1, -1)) && !pArray[i].equals(lastPoint)) {
					checkNodePower(pArray[i], myPoint, key);
				}
			}
		} else if(mNodes[myPoint.x][myPoint.y].sourceKey != key){
			Log.d("DEBUG", "There's a connection!");
			releaseSpark();
		}
	}

	public void deactivateNode(int i, int j) {
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

		for (int k = 0; k < maxWireSegments; k++) {
			if (((mWire[k].targetNode.x == i && mWire[k].targetNode.y == j) || (mWire[k].originNode.x == i && mWire[k].originNode.y == j)) && !mWire[k].permanent) {
				mWire[k].setTarget(-1, -1);
				mWire[k].setOrigin(-1, -1);
				mWire[k].mSprite.setScale(0.0f, 0.0f);
				numWires--;
			}
		}

		checkPowerConnection();
	}

	public void checkNodePress(int x, int y) {
		Point myPoint = checkNodeTouch(x, y);
		nodePressed(myPoint.x, myPoint.y);
	}

	public void checkNodeRelease(int x, int y) {
		Point myPoint = checkNodeTouch(x, y);
		mWire[0].mSprite.setScale(0.0f, 0.0f);
		nodeReleased(myPoint.x, myPoint.y);
	}

	public Point checkNodeTouch(int x, int y) {
		int xIndex = Math.round((x - (nodeDimension / 2) - xSideBuffer) / mSpacing);
		int yIndex = Math.round((y - (nodeDimension / 2) - ySideBuffer) / mSpacing);

		if (xIndex < 0) {
			xIndex = 0;
		}
		if (xIndex > mWidth - 1) {
			xIndex = mWidth - 1;
		}
		if (yIndex < 0) {
			yIndex = 0;
		}
		if (yIndex > mHeight - 1) {
			yIndex = mHeight - 1;
		}

		return new Point(xIndex, yIndex);
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		if(!mSpark.active) {
			sparkActive = false;
		}
		if (sparkActive) {
			timeStep += timeDelta;
			if (timeStep > 10) {
				timeStep = 0;

				Point[] pArray = mNodes[currentI][currentJ].getConnections();
				currentSparkVelocity = mSpark.velocity;

				if (mSpark.readyForNextTarget) {
					for (int v = 0; v < pArray.length; v++) {
						Point nextConnection = pArray[v];
						if (lastJ != nextConnection.y || lastI != nextConnection.x) {
							chooseRandom = false;
							if ((nextConnection.x == currentI) && (currentI == lastI)) {
								Log.d("DEBUG", "---Grid Update: (lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
								Log.d("DEBUG", "---Grid Update: (currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
								Log.d("DEBUG", "---Grid Update: (pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
								Log.d("DEBUG", "---Grid Update: Going Straight on Y");
								lastI = currentI;
								lastJ = currentJ;
								currentI = nextConnection.x;
								currentJ = nextConnection.y;
								v = pArray.length;
							} else if ((nextConnection.y == currentJ) && (currentJ == lastJ)) {
								Log.d("DEBUG", "---Grid Update: (lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
								Log.d("DEBUG", "---Grid Update: (currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
								Log.d("DEBUG", "---Grid Update: (pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
								Log.d("DEBUG", "---Grid Update: Going Straight on X");
								lastI = currentI;
								lastJ = currentJ;
								currentI = nextConnection.x;
								currentJ = nextConnection.y;
								v = pArray.length;
							} else {
								chooseRandom = true;
							}
						}
					}

					boolean noOptions = true;
					if (chooseRandom) {
						for (int z = 0; z < pArray.length; z++) {
							Point nextConnection = pArray[z];
							if (lastJ != nextConnection.y || lastI != nextConnection.x) {
								if (nextConnection.x != -1 || nextConnection.y != -1) {
									Log.d("DEBUG", "---Grid Update: (lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
									Log.d("DEBUG", "---Grid Update: (currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
									Log.d("DEBUG", "---Grid Update: (pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
									Log.d("DEBUG", "---Grid Update: Going whichever...");
									lastI = currentI;
									lastJ = currentJ;
									currentI = nextConnection.x;
									currentJ = nextConnection.y;
									z = pArray.length;
									noOptions = false;
								}
							}
						}
						if (noOptions) {
							Log.d("DEBUG", "---Grid Update: Hit Dead End!");
							sparkActive = false;
						}
					}
					if ((currentI == mWidth - 1 && currentJ == mHeight - 1) || (currentI == 0 && currentJ == 0)) {
						sparkActive = false;
					}
					Log.d("DEBUG", "---Grid Update: Next target: (" + currentI + ", " + currentJ + ")");
					mSpark.setNextTarget(mNodes[currentI][currentJ].getX(), mNodes[currentI][currentJ].getY(), mNodes[lastI][lastJ].speedLimit, !sparkActive);
				}
			}
		} else if(!sparkReset){
			currentI = 0;
			currentJ = 0;
			lastI = 0;
			lastJ = 0;
			sparkReset = true;
		}

		if (mSpark.explode) {
			createParticle((int) mSpark.explodeX, (int) mSpark.explodeY, 15);
			mSpark.explode = false;
		}
	}

	public void setParticles(Particle[] pArray) {
		particleArray = pArray;
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
}
