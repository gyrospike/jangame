package org.alchemicstudio;

public class ParsedDataSet {
	
	public FixedSizeArray<NodeTemplate> specialNodes;
	
	public int mapNumber;
	public int mapWidth;
	public int mapHeight;
	public int mapSpacing;

	public void initializeNodes() {
		specialNodes = new FixedSizeArray<NodeTemplate>(mapWidth*mapHeight);
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
	
	public class NodeTemplate {
		public int type;
		public int speed;
		
		public int i;
		public int j;
		
		public boolean source;
		
		public NodeTemplate(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}
}