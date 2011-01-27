package jan.game;

import android.graphics.Point;
import android.util.Log;

public class Grid extends BaseObject {

	public Node[][] mNodes;
	public float nodeDimension;

	private int mHeight;
	private int mWidth;
	private float spacing = 100.0f;
	private float xSideBuffer, ySideBuffer;
	// private NodePath nodePath = new NodePath(mWidth * mHeight);
	private FixedSizeArray<Node> path;
	private int firstIndexI, firstIndexJ, originI, originJ;
	private boolean nothingPressed = true;

	public Grid(int width, int height, float nodeDim, float screenWidth, float screenHeight) {
		mNodes = new Node[width][height];
		mHeight = height;
		mWidth = width;
		nodeDimension = nodeDim;

		float center = (spacing * (width - 1)) + nodeDim;
		xSideBuffer = (screenWidth - (center)) / 2.0f;
		ySideBuffer = (screenHeight - ((height * nodeDim) + ((height - 1) * (spacing - nodeDim)))) / 2;
		path = new FixedSizeArray<Node>(mHeight * mWidth);
		float widthSum = xSideBuffer;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				mNodes[i][j] = new Node(i, j, new Vector2(xSideBuffer + (spacing * i) + (nodeDim / 2), ySideBuffer + (spacing * j)));
				Log.d("DEBUG", "Node placed at X: " + (xSideBuffer + (spacing * i)));
			}
		}
		widthSum += (spacing * (width - 1));
		widthSum += nodeDim;
		widthSum += xSideBuffer;
		Log.d("DEBUG", "widthSum: " + widthSum);
	}

	public void nodePressed(int i, int j) {
		if (nothingPressed) {
			originI = i;
			originJ = j;
			nothingPressed = false;
		}
		firstIndexI = i;
		firstIndexJ = j;
		Log.d("DEBUG", "You pressed node: (" + i + ", " + j + ")");

		if (!mNodes[i][j].active) {
			mNodes[i][j].activate();
		} else {
			mNodes[i][j].deactivate();
		}
	}

	public boolean isAdjacent(int x1, int y1, int x2, int y2) {
		if (((x1 + 1 == x2 || x1 - 1 == x2) && (y1 == y2)) || ((y1 + 1 == y2 || y1 - 1 == y2) && (x1 == x2))) {
			return true;
		} else {
			return false;
		}
	}

	public void displayPathList() {
		String pathString = "Path: ";
		boolean done = false;
		Point target = mNodes[originI][originJ].getTarget();
		while (!done) {
			if (target.x == -1) {
				done = true;
			} else {
				pathString = pathString + "(" + target.x + ", " + target.y + ")";
				target = mNodes[target.x][target.y].getTarget();
				if (target.equals(mNodes[originI][originJ].getTarget())) {
					Log.d("TEST", "You've got a loop");
					done = true;
				}
			}
		}
		Log.d("TEST", pathString);
	}

	public void nodeReleased(int i, int j) {
		if (isAdjacent(firstIndexI, firstIndexJ, i, j)) {
			if (!mNodes[firstIndexI][firstIndexJ].active) {
				mNodes[firstIndexI][firstIndexJ].activate();
				// path.add(mNodes[firstIndexI][firstIndexJ]);
			}
			mNodes[i][j].activate();
			mNodes[firstIndexI][firstIndexJ].setTarget(new Point(i, j));
			if (mNodes[i][j].getTarget().equals(new Point(firstIndexI, firstIndexJ))) {
				mNodes[i][j].setTargetNull();
			}
			// path.add(mNodes[i][j]);
			displayPathList();
		}

		if (firstIndexI == i && firstIndexJ == j) {
			deactivateNode(i, j);
		}
	}

	public void deactivateNode(int i, int j) {
		mNodes[i][j].deactivate();
		Point temp = new Point(i, j);

		if (i > 0) {
			if (mNodes[i - 1][j].getTarget().equals(temp)) {
				mNodes[i - 1][j].setTargetNull();
			}
		}

		if (i < mWidth - 1) {
			if (mNodes[i + 1][j].getTarget().equals(temp)) {
				mNodes[i + 1][j].setTargetNull();
			}
		}

		if (j > 0) {
			if (mNodes[i][j - 1].getTarget().equals(temp)) {
				mNodes[i][j - 1].setTargetNull();
			}
		}

		if (j < mHeight - 1) {
			if (mNodes[i][j + 1].getTarget().equals(temp)) {
				mNodes[i][j + 1].setTargetNull();
			}
		}

		mNodes[i][j].setTargetNull();
	}

	public void checkPath() {
		for (int i = 0; i < path.getCount(); i++) {

		}
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {

	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}
	
	public void checkNodePress_NEW(int x, int y) {
		//int xindex = (x - (nodeDim/2) - xsideBuffer) / spacing;
		
	}

	public void checkNodePress(int x, int y) {
		for (int i = 0; i < mNodes.length; i++) {
			int currentX = (int) mNodes[i][0].getX();
			if ((currentX < x) && (currentX + 32 > x)) {
				for (int j = 0; j < mNodes[i].length; j++) {
					int currentY = (int) mNodes[i][j].getY();
					if ((currentY < y) && (currentY + 32 > y)) {
						nodePressed(i, j);
					}
				}
			}
		}
	}

	public void checkNodeRelease(int x, int y) {
		for (int i = 0; i < mNodes.length; i++) {
			int currentX = (int) mNodes[i][0].getX();
			if ((currentX < x) && (currentX + 32 > x)) {
				for (int j = 0; j < mNodes[i].length; j++) {
					int currentY = (int) mNodes[i][j].getY();
					if ((currentY < y) && (currentY + 32 > y)) {
						nodeReleased(i, j);
					}
				}
			}
		}
	}
}
