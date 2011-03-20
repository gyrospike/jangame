package org.alchemicstudio;

import android.util.Log;

public class ParsedDataSet {

	public FixedSizeArray<NodeTemplate> specialNodes;

	public int mapNumber;
	public int mapWidth;
	public int mapHeight;
	public int mapSpacing;
	public int sourceAX;
	public int sourceAY;
	public int sourceBX;
	public int sourceBY;
	
	private boolean done = false;

	public void initializeNodes() {
		specialNodes = new FixedSizeArray<NodeTemplate>(mapWidth * mapHeight);
	}

	public void setNumber(int num) {
		mapNumber = num;
	}

	public void setWidth(int w) {
		mapWidth = w;
	}

	public void setHeight(int h) {
		mapHeight = h;
	}

	public void setSpacing(int s) {
		mapSpacing = s;
	}

	public void addSpecialNode(int i, int j) {
		specialNodes.add(new NodeTemplate(i, j));
	}

	public void setSource() {
		for (int i = 0; i < specialNodes.getCount(); i++) {
			if (specialNodes.get(i).source) {
				if (!done) {
					sourceAX = specialNodes.get(i).i;
					sourceAY = specialNodes.get(i).j;
					Log.d("DEBUG", "source A assigned");
					done = true;
				} else {
					sourceBX = specialNodes.get(i).i;
					sourceBY = specialNodes.get(i).j;
					Log.d("DEBUG", "source B assigned: " + sourceBX + ", " + sourceBY);
				}
			}
		}
	}

	public class NodeTemplate {
		public int type;
		public int link;
		public int minSpeed;
		public int maxSpeed;

		public int i;
		public int j;

		public boolean source;

		public NodeTemplate(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}
}