package org.alchemicstudio;

public class ParsedDataSet {

	/** the nodes that make up the game board */
	public FixedSizeArray<NodeTemplate> mNodes;
	
	/** the nodes that make up the border around the game board */
	public FixedSizeArray<NodeTemplate> mBorderNodes;

	/** map number we are currently processing */
	public int mMapNumber;
	
	/** width of map in nodes */
	public int mMapWidth;
	
	/** height of map in nodes */
	public int mMapHeight;
	
	/** space in pixels? between the nodes */
	public int mMapSpacing;
	
	/** is this where the cart starts? */
	public int mBorderStartIndex;
	
	/** is this where the cart ends? */
	public int mBorderEndIndex;
	
	/**
	 * create the array of nodes, no special attributes have been set yet
	 */
	public void initializeNodes() {
		mNodes = new FixedSizeArray<NodeTemplate>(mMapWidth * mMapHeight);
		for(int i = 0; i < mMapWidth; i++) {
			for(int j = 0; j < mMapHeight; j++) {
				mNodes.add(new NodeTemplate(i, j));
			}
		}
	}
	
	/**
	 * create the array of border nodes, no special attributes have been set yet
	 */
	public void initializeBorderNodes() {
		int totalBorderLength = 2*mMapWidth + 2*mMapHeight;
		mBorderNodes = new FixedSizeArray<NodeTemplate>(totalBorderLength);
		for(int i = 0; i < totalBorderLength; i++) {
			mBorderNodes.add(new NodeTemplate(-1, -1));
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
	 * designate two border nodes to be the start and end node
	 */
	public void setStartAndEndIndices() {
		for (int i = 0; i < mBorderNodes.getCount(); i++) {
			if (mBorderNodes.get(i).order == Node.BORDER_TYPE_START) {
				mBorderStartIndex = i;
			} else if(mBorderNodes.get(i).order == Node.BORDER_TYPE_START) {
				mBorderEndIndex = i;
			}
		}
	}

	/**
	 * Template class for nodes used in parsing node/map data
	 * 
	 * @author Joe
	 */
	public class NodeTemplate {
		public int type = 0;
		public int link = 0;
		public int minSpeed = 0;
		public int maxSpeed = 0;
		public int i;
		public int j;
		public int borderIndex;
		public int order;

		public FixedSizeArray<NodeTemplate> pretargets = new FixedSizeArray<NodeTemplate>(Node.CONNECTION_LIMIT_DEFAULT);
		
		/**
		 * template for a node, used for data parsing
		 * 
		 * @param i
		 * @param j
		 */
		public NodeTemplate(int i, int j) {
			this.i = i;
			this.j = j;
		}
		
		/**
		 * add a reference in a node template to another node template that we want the first node template to be
		 * connected to when the game starts
		 * 
		 * @param index
		 * @param order
		 */
		public void addPreTarget(int index, int order) {
			NodeTemplate newPreTarget = new NodeTemplate(-1, -1);
			newPreTarget.borderIndex = index;
			newPreTarget.order = order;
			pretargets.add(newPreTarget);
		}
	}
}