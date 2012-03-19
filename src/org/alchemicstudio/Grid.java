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
	private final static float WIRE_SCALAR = (7.0f / 27.0f);

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
	
	private int sourceAX;
	private int sourceAY;
	private int sourceBX;
	private int sourceBY;

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
	
	private TextBox sampleTextBox;
	private RenderSystem system;
	
	public Grid(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		
		system = sSystemRegistry.mRenderSystem;
		mHeight = dataSet.mapHeight;
		mWidth = dataSet.mapWidth;
		mSpacing = dataSet.mapSpacing;
		maxBestPathLength = (mHeight * mWidth) - 1;
		maxWireSegments = (mWidth - 1) * mWidth * (mHeight - 1) + 2;
		nodeDimension = 32.0f;
		mScreenWidth = screenWidth;
		
		sourceAX = dataSet.sourceAX;
		sourceAY = dataSet.sourceAY;
		sourceBX = dataSet.sourceBX;
		sourceBY = dataSet.sourceBY;

		mNodes = new Node[mWidth][mHeight];
		mWire = new Wire[maxWireSegments];
		mSpark = new Spark();
		mSpark.hide();

		sparkActive = false;
		nodePressed = false;
		numWires = 1;
		currentI = sourceAX;
		currentJ = sourceAY;
		lastI = sourceAX;
		lastJ = sourceAY;
		
		xSideBuffer = (screenWidth - ((mWidth*nodeDimension) + ((mWidth-1)*mSpacing)))/2;
		ySideBuffer = (screenHeight - ((mHeight*nodeDimension) + ((mHeight-1)*mSpacing)))/2;
		
		for (int i = 0; i < mWidth; i++) {
			for (int j = 0; j < mHeight; j++) {
				int maxConnections;
				if(i==sourceAX && j==sourceAY) {
					maxConnections = 1;
				} else {
					maxConnections = 4;
				}
				
				int type = 0;
				int link = 0;
				float minSpeedLimit = 0.0f;
				float maxSpeedLimit = 0.0f;
				boolean isSource = false;
				for (int k = 0; k < dataSet.specialNodes.getCount(); k++) {
					int tempI = dataSet.specialNodes.get(k).i;
					int tempJ = dataSet.specialNodes.get(k).j;
					
					if(tempI == i && tempJ == j) {
						isSource = true;
						maxSpeedLimit = dataSet.specialNodes.get(k).maxSpeed;
						minSpeedLimit = dataSet.specialNodes.get(k).minSpeed;
						link = dataSet.specialNodes.get(k).link;
						type = dataSet.specialNodes.get(k).type;
						break;
					}
				}
				
				mNodes[i][j] = new Node(i, j, new Vector2(xSideBuffer + ((mSpacing + nodeDimension) * i), ySideBuffer + ((mSpacing + nodeDimension) * j)), maxConnections, 
						isSource, maxSpeedLimit, minSpeedLimit, link, type);
			}
		}


		for (int k = 0; k < maxWireSegments; k++) {
			mWire[k] = new Wire();
			mWire[k].mSprite.setScale(0.0f, 0.0f);
		}

		createWire(sourceAX, sourceAY, 0, 0, true);
		createWire(sourceBX, sourceBY, 0, 0, true);
		
		sampleTextBox = new TextBox(0, 0, "Hello");
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
			Vector2 newPoint = new Vector2(x, y);
			
			wireOriginX = tempNode.getX()+ 8.0f;
			wireOriginY = tempNode.getY() - 16.0f;
			
			double angle= Math.atan((newPoint.y - wireOriginY)/(wireOriginX - newPoint.x)) - (Math.PI / 2);
			//.02 tolerance stops the angle from exploding to infinity and the wire switching signs
			if((newPoint.x - wireOriginX) <= .02) angle += Math.PI;
			
			float distance = newPoint.distance(new Vector2(wireOriginX, wireOriginY));
			
			mWire[0].mSprite.setPosition(wireOriginX, wireOriginY);
			mWire[0].mSprite.setScale(1.0f, distance * WIRE_SCALAR);
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
			
			float ax = mNodes[ai][aj].getX() + 8.0f;
			float ay = mNodes[ai][aj].getY() - 14.0f;

			float bx = mNodes[bi][bj].getX() + 8.0f;
			float by = mNodes[bi][bj].getY() - 14.0f;

			if (offScreenWire) {
				if (ai == 0) {
					bx = -8.0f;
					by = ay;
				} else if(ai == mWidth-1){
					bx = mScreenWidth;
					by = ay;
					Log.d("DEBUG", "source B is down: " + bx + ", " + by);
				}
			} else {
				bx = mNodes[bi][bj].getX() + 8.0f;
				by = mNodes[bi][bj].getY() - 14.0f;
			}
			
			float distance = Math.abs(new Vector2(bx, by).distance(new Vector2(ax, ay)));
			
			double angle= Math.atan((by - ay)/(ax - bx)) - (Math.PI / 2);
			if((ax-bx) < 0) angle += Math.PI;

			for (int i = 1; i < maxWireSegments; i++) {
				if (mWire[i].mSprite.xScale == 0.0f && mWire[i].mSprite.yScale == 0.0f) {
					if (offScreenWire) {
						mWire[i].permanent = true;
					}
					mWire[i].mSprite.setPosition(bx, by);
					mWire[i].mSprite.setScale(1.0f, distance * WIRE_SCALAR);
					mWire[i].mSprite.setRotation((float) angle);
					mWire[i].setOrigin(bi, bj);
					mWire[i].setTarget(ai, aj);
					numWires++;
					Log.d("DEBUG", "Wire " + i + " created between: old (" + bi + ", " + bj + ") and new (" + ai + ", " + aj + ")");
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
			createParticle((int) (mNodes[i][j].getX() + 16.0f), (int) (mNodes[i][j].getY() - 16.0f), SPARKS_PER_TOUCH);
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
		if (currentPoint.equals(new Point(sourceBX, sourceBY))) {
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
				if (currentPoint.equals(new Point(sourceAX, sourceAY)) || currentPoint.equals(new Point(sourceBX, sourceBY))) {
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
			Node originNode = mNodes[sourceAX][sourceAY];
			mSpark.setNextTarget(originNode.getX(), originNode.getY(),  originNode.type, originNode.minSpeedLimit, originNode.maxSpeedLimit, !sparkActive);
			mSpark.activate(originNode.getX(), originNode.getY());
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
		checkNodePower(new Point(sourceAX, sourceAY), null, 1);
		checkNodePower(new Point(sourceBX, sourceBY), null, 2);
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
		Log.d("DEBUG", "x, y : " + x + ", " + y);
		Log.d("DEBUG", "mSpacing : " + mSpacing);
		Log.d("DEBUG", "xSideBuffer, ySideBuffer : " + xSideBuffer + ", " + ySideBuffer);
		int xIndex = (int) Math.round((x - xSideBuffer) / (mSpacing + 32));
		int yIndex = (int) Math.round((y - ySideBuffer) / (mSpacing + 32));
		//int xIndex = Math.round((x + (nodeDimension/2) - xSideBuffer) / mSpacing);
		//int yIndex = Math.round((y + (nodeDimension/2) - ySideBuffer) / mSpacing);
		Log.d("DEBUG", "xIndex, yIndex : " + xIndex + ", " + yIndex);

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
	public void update(float timeDelta) {
		sampleTextBox.theText = "Spark Speed: " + Math.round((100.0f * mSpark.velocity))/100.0f;
		
		system.scheduleForWrite(sampleTextBox);
		
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
					if ((currentI == sourceBX && currentJ == sourceBY) || (currentI == sourceAX && currentJ == sourceAY)) {
						sparkActive = false;
					}
					Log.d("DEBUG", "---Grid Update: Next target: (" + currentI + ", " + currentJ + ")");
					Node tempCurrentNode = mNodes[currentI][currentJ];
					Node tempLastNode = mNodes[lastI][lastJ];
					mSpark.setNextTarget(tempCurrentNode.getX(), tempCurrentNode.getY(),  tempLastNode.type, tempLastNode.minSpeedLimit, tempLastNode.maxSpeedLimit, !sparkActive);
					Log.d("DEBUG", "Spark given new gate properties");
				}
			}
		} else if(!sparkReset){
			currentI = sourceAX;
			currentJ = sourceAY;
			lastI = sourceAX;
			lastJ = sourceAY;
			sparkReset = true;
		}

		if (mSpark.explode) {
			createParticle((int) mSpark.explodeX, (int) mSpark.explodeY, 15);
			mSpark.explode = false;
		}
		
		for(int w = 0; w < mNodes.length; w++) {
			for(int q = 0; q < mNodes[w].length; q++) {
				mNodes[w][q].update(timeDelta);
			}
		}
		
		for(int e = 0; e < mWire.length; e++) {
			mWire[e].update(timeDelta);
		}
		
		mSpark.update(timeDelta);
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
