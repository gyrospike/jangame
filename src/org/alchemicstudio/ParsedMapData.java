package org.alchemicstudio;


public class ParsedMapData {

	/** the nodes that make up the game board */
	public FixedSizeArray<NodeTemplate> mNodes;

    /** the badges that can be earned on this level */
    public FixedSizeArray<BadgeTemplate> mBadges;
	
	/** the total number of nodes that make up the game board, including the border nodes */
	public int mTotalNumNodes;

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
		mTotalNumNodes = (mMapWidth * mMapHeight) + totalBorderLength;
		mNodes = new FixedSizeArray<NodeTemplate>(mTotalNumNodes);
		for(int i = 0; i < mMapWidth; i++) {
			for(int j = 0; j < mMapHeight; j++) {
				mNodes.add(new NodeTemplate(i, j, -1));
			}
		}
		for(int i = 0; i < totalBorderLength; i++) {
			int[] borderCoords = getBorderIJFromK(i);
			mNodes.add(new NodeTemplate(borderCoords[0], borderCoords[1], i));
			//Log.d("DEBUG", "i, j, k: " + borderCoords[0] + ", " + borderCoords[1] + ", " + i);
		}
	}
	
	/**
	 * infer the I and J coordinates of a border node based on it's K index
	 * 
	 * @param k
	 * @return
	 */
	private int[] getBorderIJFromK(int k) {
		int[] result = new int[2];
		if(k < mMapWidth) {
			result[0] = k;
			result[1] = -1;
		} else if (k < mMapWidth+mMapHeight) {
			result[0] = mMapWidth;
			result[1] = k-mMapWidth;
		} else if (k < (2*mMapWidth)+mMapHeight) {
			result[0] = (2*mMapWidth)+mMapHeight - (k+1);
			result[1] = mMapHeight;
		} else {
			result[0] = -1;
			result[1] = (2*mMapWidth)+(2*mMapHeight) - (k+1);
		}
		return result;
	}

    /**
     * getter
     * @return
     */
    public int getNumber() {
        return mMapNumber;
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

    public void initBadges() {
        mBadges = new FixedSizeArray<BadgeTemplate>(3);
    }

    public void addBadge(int type) {
        mBadges.add(new BadgeTemplate(type));
    }

    public void addRequirementToCurrentBadge(String type, int value) {
        mBadges.getLast().addRequirement(type, value);
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
		public int keyId = 0;
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
         * @param i
         * @param j
         * @param k
         */
		public void addPreconnection(int i, int j, int k) {
			NodeTemplate newPreTarget = new NodeTemplate(i, j, k);
			mPreconnections.add(newPreTarget);
		}
	}

    public class BadgeTemplate {

        private int mType;

        public FixedSizeArray<RequirementTemplate> mRequirements = new FixedSizeArray<RequirementTemplate>(3);

        public BadgeTemplate(int type) {
            mType = type;
        }

        public void addRequirement(String type, int value) {
            mRequirements.add(new RequirementTemplate(type, value));
        }

        public int getType() {
            return mType;
        }
    }

    public class RequirementTemplate {

        private String mType;

        private int mValue;

        public RequirementTemplate(String type, int value) {
            mType = type;
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public String getType() {
            return mType;
        }
    }
}