package jan.game;

import java.util.ArrayList;
import android.graphics.Point;
import android.util.Log;

public class Grid extends BaseObject {

	public Spark mSpark;
	public Node[][] mNodes;
	public Wire[] mWire;
	public float nodeDimension;

	public final int MAX_WIRE_SEGMENTS = 30;
	public final int DEBUG_WIRES_BEFORE_SPARK = 7;

	private final float SPACING = 100.0f;

	private int numWires;
	private int mHeight, mWidth;
	private int firstIndexI, firstIndexJ;
	private float xSideBuffer, ySideBuffer, wireOriginX, wireOriginY;
	private float timeStep;
	private int currentI = 0;
	private int currentJ = 0;
	private int lastI = 0;
	private int lastJ = 0;
	private boolean nodePressed, sparkActive, chooseRandom, circuitCalcDone;

	private Point[] pointTrace;
	private FixedSizeArray<Node> circuitList = new FixedSizeArray<Node>(25);
	private FixedSizeArray<FixedSizeArray<Point>> finalList = new FixedSizeArray<FixedSizeArray<Point>>(512);
	private int maxBestPathLength;

	public Grid(int width, int height, float nodeDim, float screenWidth, float screenHeight) {

		mHeight = height;
		mWidth = width;
		maxBestPathLength = (mHeight * mWidth) - 1;
		nodeDimension = nodeDim;

		mNodes = new Node[width][height];
		mWire = new Wire[MAX_WIRE_SEGMENTS];
		pointTrace = new Point[mWidth * mHeight];
		mSpark = new Spark();
		mSpark.hide();

		sparkActive = false;
		nodePressed = false;
		numWires = 1;

		float center = (SPACING * (width - 1)) + nodeDim;
		xSideBuffer = (screenWidth - (center)) / 2.0f;

		float center2 = (SPACING * (height - 1)) + nodeDim;
		ySideBuffer = (screenHeight - (center2)) / 2.0f;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				mNodes[i][j] = new Node(i, j, new Vector2(xSideBuffer + (SPACING * i) + (nodeDim / 2), ySideBuffer + (SPACING * j)));
				Log.d("DEBUG", "Node placed at X: " + (xSideBuffer + (SPACING * i)));
			}
		}

		mNodes[0][0].setSource();
		mNodes[mWidth - 1][mHeight - 1].setSource();

		for (int k = 0; k < MAX_WIRE_SEGMENTS; k++) {
			mWire[k] = new Wire();
			mWire[k].mSprite.setScale(0.0f, 0.0f);
		}

		for (int m = 0; m < mWidth * mHeight; m++) {
			pointTrace[m] = new Point(-1, -1);
		}
	}

	public void displayPathList() {
		String pathString = "";
		boolean done = false;
		int i = 0;
		int j = 0;
		int lastI = 0;
		int lastJ = 0;
		int runTimes = 0;
		Log.d("DEBUG", "Display Path Called");

		while (!done) {
			Point[] pArray = mNodes[i][j].getConnections();

			pathString += "Node: (" + i + ", " + j + ")\n";
			for (int b = 0; b < pArray.length; b++) {
				pathString += "pArray[" + b + "]: " + pArray[b] + "\n";
			}

			for (int v = 0; v < pArray.length; v++) {
				Point nextConnection = mNodes[i][j].getConnection(v);
				if (lastI != nextConnection.x || lastJ != nextConnection.y) {
					lastI = i;
					lastJ = j;
					i = nextConnection.x;
					j = nextConnection.y;
					v = pArray.length;
					Log.d("DEBUG", "(i, j): " + i + ", " + j);
				}
			}

			if (i == -1 && j == -1) {
				done = true;
			}
			if (runTimes > 5) {
				done = true;
			}
			runTimes++;
		}
		Log.d("DEBUG", pathString);
	}

	public void nodePressed(int i, int j) {
		nodePressed = true;
		firstIndexI = i;
		firstIndexJ = j;
		Log.d("DEBUG", "You pressed node: (" + i + ", " + j + ")");
	}

	public void updateWire(int x, int y) {
		if (nodePressed) {

			wireOriginX = mNodes[firstIndexI][firstIndexJ].getX() + ((x - mNodes[firstIndexI][firstIndexJ].getX()) / 2);
			wireOriginY = mNodes[firstIndexI][firstIndexJ].getY() + ((y - mNodes[firstIndexI][firstIndexJ].getY()) / 2);

			double angle = (Math.PI / 2) + Math.atan((mNodes[firstIndexI][firstIndexJ].getY() - y) / (x - mNodes[firstIndexI][firstIndexJ].getX()));
			Vector2 newPoint = new Vector2(x, y);
			float distance = newPoint.distance(new Vector2(wireOriginX, wireOriginY));
			// Log.d("DEBUG", "angle: " + angle + ", distance: " + distance);
			mWire[0].mSprite.setPosition(wireOriginX, wireOriginY);
			mWire[0].mSprite.setScale(1.0f, distance * 3 / 9);
			mWire[0].mSprite.setRotation((float) angle);
		}
	}

	public void createWire(int ai, int aj, int bi, int bj) {
		if (numWires < MAX_WIRE_SEGMENTS) {

			float ax = mNodes[ai][aj].getX();
			float ay = mNodes[ai][aj].getY();
			float bx = mNodes[bi][bj].getX();
			float by = mNodes[bi][bj].getY();

			float x = ax + ((bx - ax) / 2);
			float y = ay + ((by - ay) / 2);
			float distance = new Vector2(ax, ay).distance(new Vector2(bx, by));
			double angle = (Math.PI / 2) + Math.atan((ay - by) / (ax - bx));

			for (int i = 1; i < MAX_WIRE_SEGMENTS; i++) {
				if (mWire[i].mSprite.xScale == 0.0f && mWire[i].mSprite.yScale == 0.0f) {
					mWire[i].mSprite.setPosition(x, y);
					mWire[i].mSprite.setScale(1.0f, distance * 2 / 9);
					mWire[i].mSprite.setRotation((float) angle);
					mWire[i].setOrigin(ai, aj);
					mWire[i].setTarget(bi, bj);
					numWires++;
					Log.d("DEBUG", "Wire " + i + " created between: (" + ai + ", " + aj + ") and (" + bi + ", " + bj + ")");
					i = MAX_WIRE_SEGMENTS;
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

	public void nodeReleased(int i, int j) {
		Log.d("DEBUG", "Connection between: " + connectionBetween(firstIndexI, firstIndexJ, i, j));

		if (firstIndexI == i && firstIndexJ == j) {
			deactivateNode(i, j);
		}

		if (isAdjacent(firstIndexI, firstIndexJ, i, j) && !connectionBetween(firstIndexI, firstIndexJ, i, j)) {

			createWire(firstIndexI, firstIndexJ, i, j);

			mNodes[firstIndexI][firstIndexJ].setConnection(new Point(i, j), 0);
			mNodes[i][j].setConnection(new Point(firstIndexI, firstIndexJ), 0);

			// clearing old values in preparation for calculating the best path
			Log.d("DEBUG", "clearPaths called from nodeReleased");
			clearPaths();

			FixedSizeArray<Point> list = new FixedSizeArray<Point>(maxBestPathLength + 1);
			calculateCircuit(new Point(0, 0), null, list);
			if (finalList.getCount() > 0) {
				chooseBestPath();
				circuitCalcDone = true;
			} else {
				checkPowerConnection();
			}

			if (circuitCalcDone) {
				releaseSpark();
			}
		}

		nodePressed = false;
	}

	public void deactivateAllNodes() {
		for (int i = 1; i < mHeight; i++) {
			for (int j = 1; j < mWidth; j++) {
				mNodes[i][j].deactivate();
			}
		}
	}

	public void clearPaths() {
		circuitList.clear();
		for (int j = 0; j < finalList.getCount(); j++) {
			finalList.get(j).clear();
		}
		finalList.clear();
	}

	public void chooseBestPath() {
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
			circuitList.get(u).activate(2);
		}
	}

	public void printList(FixedSizeArray<Point> list) {
		for (int u = 0; u < list.getCount(); u++) {
			Log.d("DEBUG", "List: (" + list.get(u).x + ", " + list.get(u).y + "), length: " + list.getCount());
		}
	}

	public void calculateCircuit(Point currentPoint, Point lastPoint, FixedSizeArray<Point> list) {
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
						list.add(new Point(pArray[k].x, pArray[k].y));
						calculateCircuit(pArray[k], currentPoint, list);
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
						if (!once) {
							list.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list);
						}
						if (once) {
							list1.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list1);
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
						if (!listDone && list2Done && list1Done) {
							list.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list);
							listDone = true;
						}
						if (!list1Done && list2Done) {
							list1.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list1);
							list1Done = true;
						}
						if (!list2Done) {
							list2.add(new Point(pArray[k].x, pArray[k].y));
							calculateCircuit(pArray[k], currentPoint, list2);
							list2Done = true;
						}
					}
				}
			}
		}
	}

	public void releaseSpark() {
		Log.d("DEBUG", "Spark Released!");
		sparkActive = true;
		mSpark.activate(mNodes[0][0].getX(), mNodes[0][0].getY());
	}

	public void completeSpark() {
		Log.d("DEBUG", "Spark Retired!");
		sparkActive = false;
		mSpark.hide();
		// clearPaths();
		// Log.d("DEBUG", "clearPaths called from completeSpark");
	}

	public void checkPowerConnection() {
		for (int q = 0; q < mWidth; q++) {
			for (int p = 0; p < mHeight; p++) {
				mNodes[q][p].removePower();
			}
		}
		checkNodePower(new Point(0, 0), null);
		checkNodePower(new Point(mWidth - 1, mHeight - 1), null);
	}

	public void checkNodePower(Point myPoint, Point lastPoint) {
		//Log.d("DEBUG", "asd: " + myPoint);
		if (!mNodes[myPoint.x][myPoint.y].hasPower) {
			mNodes[myPoint.x][myPoint.y].activate(1);
			Point[] pArray = mNodes[myPoint.x][myPoint.y].getConnections();
			for (int i = 0; i < pArray.length; i++) {
				if (!pArray[i].equals(new Point(-1, -1)) && !pArray[i].equals(lastPoint)) {
					checkNodePower(pArray[i], myPoint);
				}
			}
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

		for (int k = 0; k < MAX_WIRE_SEGMENTS; k++) {
			if ((mWire[k].targetNode.x == i && mWire[k].targetNode.y == j) || (mWire[k].originNode.x == i && mWire[k].originNode.y == j)) {
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
		int xIndex = Math.round((x - (nodeDimension / 2) - xSideBuffer) / SPACING);
		int yIndex = Math.round((y - (nodeDimension / 2) - ySideBuffer) / SPACING);

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
		if (sparkActive) {
			timeStep += timeDelta;
			if (timeStep > 10) {
				timeStep = 0;

				Point[] pArray = mNodes[currentI][currentJ].getConnections();

				if (mSpark.readyForNextTarget) {
					for (int v = 0; v < pArray.length; v++) {
						Point nextConnection = pArray[v];
						if (lastJ != nextConnection.y || lastI != nextConnection.x) {
							chooseRandom = false;
							if ((nextConnection.x == currentI) && (currentI == lastI)) {
								Log.d("DEBUG", "(lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
								Log.d("DEBUG", "(currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
								Log.d("DEBUG", "(pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
								Log.d("DEBUG", "Going Straight on Y");
								lastI = currentI;
								lastJ = currentJ;
								currentI = nextConnection.x;
								currentJ = nextConnection.y;
								v = pArray.length;
							} else if ((nextConnection.y == currentJ) && (currentJ == lastJ)) {
								Log.d("DEBUG", "(lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
								Log.d("DEBUG", "(currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
								Log.d("DEBUG", "(pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
								Log.d("DEBUG", "Going Straight on X");
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

					if (chooseRandom) {
						for (int z = 0; z < pArray.length; z++) {
							Point nextConnection = pArray[z];
							if (lastJ != nextConnection.y || lastI != nextConnection.x) {
								if (nextConnection.x != -1 || nextConnection.y != -1) {
									Log.d("DEBUG", "(lastI, lastJ): " + "(" + lastI + ", " + lastJ + ")");
									Log.d("DEBUG", "(currentI, currentJ): " + "(" + currentI + ", " + currentJ + ")");
									Log.d("DEBUG", "(pArray[v].x, pArray[v].y): " + "(" + nextConnection.x + ", " + nextConnection.y + ")");
									Log.d("DEBUG", "Going whichever...");
									lastI = currentI;
									lastJ = currentJ;
									currentI = nextConnection.x;
									currentJ = nextConnection.y;
									z = pArray.length;
								} else {
									Log.d("DEBUG", "Hit Dead End!");
									completeSpark();
									z = pArray.length;
								}
							}
						}
					}

					if (currentI == mWidth - 1 && currentJ == mHeight - 1) {
						sparkActive = false;
						// clearPaths();
						// Log.d("DEBUG", "clearPaths called from update");
					}

					Log.d("DEBUG", "Next target: (" + currentI + ", " + currentJ + ")");
					mSpark.setNextTarget(mNodes[currentI][currentJ].getX(), mNodes[currentI][currentJ].getY(), sparkActive);
				}
			}
		}
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
