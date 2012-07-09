package org.alchemicstudio;

public class ParsedDataSet {

	/** the nodes that make up the game board */
	public FixedSizeArray<NodeTemplate> mNodes;

	/** map number we are currently processing */
	public int mMapNumber;
	
	/** width of map in nodes */
	public int mMapWidth;
	
	/** height of map in nodes */
	public int mMapHeight;
	
	/** space in pixels? between the nodes */
	public int mMapSpacing;
	
	/**
	 * create the array of nodes, no special attributes have been set yet
	 */
	public void initializeNodes() {
		int totalBorderLength = 2*mMapWidth + 2*mMapHeight;
		mNodes = new FixedSizeArray<NodeTemplate>((mMapWidth * mMapHeight) + totalBorderLength);
		for(int i = 0; i < mMapWidth; i++) {
			for(int j = 0; j < mMapHeight; j++) {
				mNodes.add(new NodeTemplate(i, j, -1));
			}
		}
		for(int i = 0; i < totalBorderLength; i++) {
			mNodes.add(new NodeTemplate(-1, -1, i));
		}
	}

	/**
	 * setter
	 * @param num
	 */
	public void setNumber(int num) {
		mMapNumber = num;
	}

	/**
	 * setter
	 * @param w
	 */
	public void setWidth(int w) {
		mMapWidth = w;
	}

	/**
	 * setter
	 * @param h
	 */
	public void setHeight(int h) {
		mMapHeight = h;
	}

	/**
	 * setter
	 * @param s
	 */
	public void setSpacing(int s) {
		mMapSpacing = s;
	}

	/**
	 * Template class for nodes used in parsing node/map data
	 * 
	 * @author Joe
	 */
	public class NodeTemplate {
		public String type = Node.NODE_TYPE_STANDARD;
		public int minSpeed = 0;
		public int maxSpeed = 0;
		public int i;
		public int j;
		public int k;

		public FixedSizeArray<NodeTemplate> mPreconnections = new FixedSizeArray<NodeTemplate>(Node.CONNECTION_LIMIT_DEFAULT);
		
		/**
		 * template for a node, used for data parsing
		 * 
		 * @param i
		 * @param j
		 */
		public NodeTemplate(int i, int j, int k) {
			this.i = i;
			this.j = j;
			this.k = k;
		}
		
		/**
		 * add a reference in a node template to another node template that we want the first node template to be
		 * connected to when the game starts
		 * 
		 * @param index
		 * @param order
		 */
		public void addPreconnection(int i, int j, int k) {
			NodeTemplate newPreTarget = new NodeTemplate(i, j, k);
			mPreconnections.add(newPreTarget);
		}
	}
}