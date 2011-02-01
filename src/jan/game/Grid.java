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

	private int savePointIndex;
	private int numWires;
	private int mHeight, mWidth;
	private int firstIndexI, firstIndexJ;
	private float xSideBuffer, ySideBuffer, wireOriginX, wireOriginY;
	private float timeStep;
	private int currentI = 0;
	private int currentJ = 0;
	private int lastI = 0;
	private int lastJ = 0;
	private int runTimes = 0;
	private int timesSetNodes;
	private boolean nothingPressed, layingWire, nodePressed, sparkActive, chooseRandom, circuitCalcDone;

	private Point[] pointTrace;
	private int pointIndex = 0;
	private FixedSizeArray<Node> circuitList = new FixedSizeArray<Node>(25);
	private Point[] circuitArray = new Point[10];
	private int circuitIndex;

	public Grid(int width, int height, float nodeDim, float screenWidth, float screenHeight) {

		mHeight = height;
		mWidth = width;
		nodeDimension = nodeDim;

		mNodes = new Node[width][height];
		mWire = new Wire[MAX_WIRE_SEGMENTS];
		pointTrace = new Point[mWidth * mHeight];
		mSpark = new Spark();
		mSpark.hide();

		sparkActive = false;
		nodePressed = false;
		layingWire = false;
		nothingPressed = true;
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

			layingWire = true;

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
		if (isAdjacent(firstIndexI, firstIndexJ, i, j) && !connectionBetween(firstIndexI, firstIndexJ, i, j)) {

			if (!mNodes[i][j].source) {
				mNodes[i][j].activate(1);
			}
			createWire(firstIndexI, firstIndexJ, i, j);

			mNodes[firstIndexI][firstIndexJ].setConnection(new Point(i, j), 0);
			mNodes[i][j].setConnection(new Point(firstIndexI, firstIndexJ), 0);

			for (int u = 0; u < mWidth; u++) {
				for (int v = 0; v < mHeight; v++) {
					mNodes[u][v].saveIndex = -1;
					mNodes[u][v].branchPoint = false;
				}
			}

			savePointIndex = 0;
			circuitList.clear();
			calcCircuit(new Point(0, 0), null);
			if (circuitCalcDone) {
				releaseSpark();
			}
			// displayPathList();
			for (int u = 0; u < circuitList.getCount(); u++) {
				Log.d("DEBUG", "Circuit List: (" + circuitList.get(u).iX + ", " + circuitList.get(u).iY + "), length: " + circuitList.getCount());
			}

			/*
			 * timesSetNodes++; if (timesSetNodes > DEBUG_WIRES_BEFORE_SPARK) {
			 * releaseSpark(); timesSetNodes = 0; }
			 */
		}

		if (firstIndexI == i && firstIndexJ == j) {
			deactivateNode(i, j);
			// deactivateAllNodes();
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

	public void calcCircuit(Point myPoint, Point lastPoint) {
		if (myPoint.equals(new Point(mWidth - 1, mHeight - 1))) {
			circuitList.add(mNodes[myPoint.x][myPoint.y]);
			Log.d("DEBUG", "HARD: Reached the End!");
			circuitCalcDone = true;
		}

		boolean newPoint = true;
		int len2 = circuitList.getCount();
		if (len2 > 0) {
			for (int r = 0; r < len2; r++) {
				if (circuitList.get(r).iX == myPoint.x && circuitList.get(r).iY == myPoint.y) {
					newPoint = false;
				}
			}
			if (newPoint) {
				if(!mNodes[lastPoint.x][lastPoint.y].branchPoint) {
					mNodes[myPoint.x][myPoint.y].saveIndex = mNodes[lastPoint.x][lastPoint.y].saveIndex;
				}
				circuitList.add(mNodes[myPoint.x][myPoint.y]);
				Log.d("DEBUG", "HARD: added to circuitList " + myPoint + ", length: " + circuitList.getCount() + ", saveIndex: " + mNodes[myPoint.x][myPoint.y].saveIndex);
			}
		} else {
			circuitList.add(mNodes[myPoint.x][myPoint.y]);
		}

		Point[] pArray = mNodes[myPoint.x][myPoint.y].getConnections();
		int len = 0;
		for (int p = 0; p < pArray.length; p++) {
			if (!pArray[p].equals(new Point(-1, -1))) {
				len++;
				/*
				 * if (!pArray[p].equals(lastPoint)) { Log.d("DEBUG", "HARD: " +
				 * pArray[p] + " with save index: " +
				 * mNodes[myPoint.x][myPoint.y].saveIndex);
				 * //mNodes[pArray[p].x][pArray[p].y].saveIndex =
				 * mNodes[myPoint.x][myPoint.y].saveIndex; }
				 */
			}
		}

		Log.d("DEBUG", "HARD: " + myPoint + " has saveIndex: " + mNodes[myPoint.x][myPoint.y].saveIndex);

		if (len == 1) {
			if (!myPoint.equals(new Point(mWidth - 1, mHeight - 1))) {
				if (pArray[0].equals(lastPoint)) {
					Log.d("DEBUG", "HARD: " + myPoint + " is a dead end, removing nodes with saveIndex: " + mNodes[myPoint.x][myPoint.y].saveIndex);
					// circuitRemove(mNodes[pArray[0].x][pArray[0].y].saveIndex);
					circuitRemove(mNodes[myPoint.x][myPoint.y].saveIndex, true);
					checkNewDeadEnd(mNodes[myPoint.x][myPoint.y].saveIndex - 1);
				} else {
					calcCircuit(pArray[0], myPoint);
				}
			}
		} else if (len == 2) {
			for (int j = 0; j < len; j++) {
				Log.d("DEBUG", "HARD: " + myPoint + " for loop index j: " + j + " is point " + pArray[j] + " that has save index: " + mNodes[pArray[j].x][pArray[j].y].saveIndex);
				if (!pArray[j].equals(lastPoint)) {
					if (mNodes[pArray[j].x][pArray[j].y].saveIndex == -1) {
						Log.d("DEBUG", "HARD: len = 2, calcCircuit called on " + myPoint + ")");
						mNodes[pArray[j].x][pArray[j].y].saveIndex = mNodes[myPoint.x][myPoint.y].saveIndex;
						calcCircuit(pArray[j], myPoint);
					} else {
						Log.d("DEBUG", "HARD: " + myPoint + " tried to call circuit remove");
						// circuitRemove(mNodes[pArray[j].x][pArray[j].y].saveIndex);
						circuitRemove(mNodes[myPoint.x][myPoint.y].saveIndex, true);
					}
				}
			}
		} else if (len > 2) {
			mNodes[myPoint.x][myPoint.y].branchPoint = true;
			mNodes[myPoint.x][myPoint.y].saveIndex = savePointIndex;
			pArray = orderConnectionPreference(myPoint, lastPoint, pArray);
			for (int j = 0; j < len; j++) {
				if (!pArray[j].equals(lastPoint)) {
					if (mNodes[pArray[j].x][pArray[j].y].saveIndex == -1) {
						Log.d("DEBUG", "HARD: BRANCH POINT[" + j + "]: " + myPoint + ", lastPoint: " + lastPoint);
						savePointIndex++;
						mNodes[pArray[j].x][pArray[j].y].saveIndex = savePointIndex;
						Log.d("DEBUG", "HARD: Assigning " + pArray[j] + " saveIndex: " + savePointIndex);
						calcCircuit(pArray[j], myPoint);
					} else {
						Log.d("DEBUG", "HARD: calCircuit denied, didn't have right index for " + pArray[j] + " from " + myPoint + ", had: " + mNodes[pArray[j].x][pArray[j].y].saveIndex);
					}
				}
			}
		}
	}

	public void circuitRemove(int i, boolean branchPointRemove) {
		if (i != -1) {
			int len = circuitList.getCount();
			for (int j = 0; j < len; j++) {
				if (circuitList.get(j).saveIndex == i) {
					Log.d("DEBUG", "removing (" + circuitList.get(j).iX + ", " + circuitList.get(j).iY + ")");
					circuitList.remove(j);
					len--;
					j--;
				}
			}
		}
	}

	public void checkNewDeadEnd(int index) {
		if (index > -1) {
			Log.d("DEBUG", "Checking new dead end for index: " + index);
			for (int i = 0; i < mWidth; i++) {
				for (int j = 0; j < mHeight; j++) {
					if (mNodes[i][j].saveIndex == index) {
						Log.d("DEBUG", "HARD: Got connections for point: (" + i + ", " + j + ")");
						Point[] pArray = mNodes[i][j].getConnections();
						int len = 0;
						for (int h = 0; h < pArray.length; h++) {
							if (!pArray[h].equals(new Point(-1, -1))) {
								len++;
							}
						}
						if (len == 2) {
							circuitRemove(index, false);
						}
					}
				}
			}
		}
	}

	public Point[] orderConnectionPreference(Point currentPoint, Point lastPoint, Point[] pArray) {
		boolean xPreference = false;
		boolean yPreference = false;
		int firstIndex = 0;
		Point holder;

		if (currentPoint != null && lastPoint != null) {
			if (currentPoint.x == lastPoint.x) {
				xPreference = true;
			} else {
				yPreference = true;
			}

			for (int i = 0; i < pArray.length; i++) {
				if (pArray[i].x == currentPoint.x && xPreference) {
					firstIndex = i;
				} else if (pArray[i].y == currentPoint.y && yPreference) {
					firstIndex = i;
				}
			}

			holder = pArray[0];
			pArray[0] = pArray[firstIndex];
			pArray[firstIndex] = holder;
		}
		return pArray;
	}

	/*
	 * public void checkActivePath(Point myPoint) { boolean loop = false; int
	 * targetX = mNodes[myPoint.x][myPoint.y].getTarget().x; int targetY =
	 * mNodes[myPoint.x][myPoint.y].getTarget().y;
	 * 
	 * for (int i = 0; i < pointTrace.length; i++) { Log.d("DEBUG",
	 * "pointTrace " + i + ": " + pointTrace[i]); if (myPoint.x ==
	 * pointTrace[i].x && myPoint.y == pointTrace[i].y) { Log.d("DEBUG",
	 * "LOOPER: pointTrace " + i + ": " + pointTrace[i]); loop = true;
	 * pointIndex = 0; } }
	 * 
	 * pointTrace[pointIndex] = new Point(myPoint.x, myPoint.y); pointIndex++;
	 * 
	 * Log.d("DEBUG", "Target: " + targetX + ", " + targetY);
	 * 
	 * if (targetX != -1 && targetY != -1) { if (!loop) {
	 * mNodes[targetX][targetY].activate();
	 * checkActivePath(mNodes[myPoint.x][myPoint.y].getTarget()); } else {
	 * Log.d("DEBUG", "LOOP!!!!!"); } } else { for (int m = 0; m < mWidth *
	 * mHeight; m++) { pointTrace[m] = new Point(-1, -1); pointIndex = 0; } } }
	 */

	public void releaseSpark() {
		Log.d("DEBUG", "Spark Released!");
		sparkActive = true;
		for (int i = 0; i < circuitList.getCount(); i++) {
			circuitList.get(i).activate(2);
		}
	}

	public void completeSpark() {
		Log.d("DEBUG", "Spark Retired!");
		sparkActive = false;
		mSpark.hide();
		circuitList.clear();
	}

	public void deactivateNode(int i, int j) {
		Log.d("DEBUG", "get i, j: " + i + ", " + j);
		if (!(i == 0 && j == 0)) {
			mNodes[i][j].deactivate();
		}
		Point temp = new Point(i, j);

		if (i > 0) {
			Point[] pArray = mNodes[i - 1][j].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i - 1][j].setConnectionNull(m);
				}
			}
		}

		if (i < mWidth - 1) {
			Point[] pArray = mNodes[i + 1][j].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i + 1][j].setConnectionNull(m);
				}
			}
		}

		if (j > 0) {
			Point[] pArray = mNodes[i][j - 1].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i][j - 1].setConnectionNull(m);
				}
			}
		}

		if (j < mHeight - 1) {
			Point[] pArray = mNodes[i][j + 1].getConnections();
			for (int m = 0; m < pArray.length; m++) {
				if (pArray[m].equals(temp)) {
					mNodes[i][j + 1].setConnectionNull(m);
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
			if (timeStep > 1000) {
				timeStep = 0;
				Log.d("DEBUG", "Time Step...");

				Point[] pArray = mNodes[currentI][currentJ].getConnections();

				float tempX = mNodes[currentI][currentJ].getX();
				float tempY = mNodes[currentI][currentJ].getY();
				mSpark.setPosition(tempX, tempY);

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
							if (nextConnection.x != -1 && nextConnection.y != -1) {

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

				if (currentI == -1 && currentJ == -1) {
					sparkActive = false;
				}
				if (runTimes > 5) {
					sparkActive = false;
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
