package org.alchemicstudio;

import android.graphics.Point;
import android.util.Log;

public class Track {

	private int maxBestPathLength;
	private boolean nodePressed;
	private boolean sparkActive;
	private boolean sparkReset;
	private boolean chooseRandom;
	private boolean circuitCalcDone;
	private TextBox sampleTextBox;
	public Spark mSpark;
	public float currentSparkVelocity;
	
	private int currentI;
	private int currentJ;
	private int lastI;
	private int lastJ;
	
	private FixedSizeArray<Node> circuitList = new FixedSizeArray<Node>(25);
	private FixedSizeArray<FixedSizeArray<Point>> finalList = new FixedSizeArray<FixedSizeArray<Point>>(512);
	
	/*
	public Track() {
		maxBestPathLength = (mHeight * mWidth) - 1;
		mSpark = new Spark();
		mSpark.hide();
		sampleTextBox = new TextBox(0, 0, "Hello");
		
				currentI = sourceAX;
		currentJ = sourceAY;
		lastI = sourceAX;
		lastJ = sourceAY;
		
				sparkActive = false;
		nodePressed = false;
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
	
	
	private void clearPaths() {
		circuitList.clear();
		for (int j = 0; j < finalList.getCount(); j++) {
			finalList.get(j).clear();
		}
		finalList.clear();
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
	
	private void update() {
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
	/*
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


	public void completeSpark() {
		Log.d("DEBUG", "Spark Retired!");
		sparkActive = false;
		mSpark.hide();
		// clearPaths();
		// Log.d("DEBUG", "clearPaths called from completeSpark");
	}
	*/
}
