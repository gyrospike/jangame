package org.alchemicstudio;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MapXMLHandler extends DefaultHandler {

	/** the most recent node index i to be parsed */
	private int mCurrentI;
	
	/** the most recent node index j to be parsed */
	private int mCurrentJ;
	
	/** the most recent node index k to be parsed */
	private int mCurrentK;
	
	/** the index of the most recent node to be parsed */
	private int mCurrentNodeIndex;

	/** the entire data set of what we are parsing */
	private ParsedMapData mParsedDataSet = new ParsedMapData();

	/**
	 * getter
	 * @return	get the parsed data for this level
	 */
	public ParsedMapData getParsedData() {
		return mParsedDataSet;
	}
	
	/**
	 * helper parsing function for the MAP tag
	 * @param atts
	 */
	private void parseMap(Attributes atts) {
		mParsedDataSet.setNumber(Integer.parseInt(atts.getValue("NUMBER")));
		mParsedDataSet.setWidth(Integer.parseInt(atts.getValue("WIDTH")));
		mParsedDataSet.setHeight(Integer.parseInt(atts.getValue("HEIGHT")));
		mParsedDataSet.setSpacing(Integer.parseInt(atts.getValue("SPACING")));
		mParsedDataSet.initializeNodes();
	}
	
	/**
	 * helper parsing function for the NODE tag
	 * @param atts
	 */
	private void parseNode(Attributes atts) {
		mCurrentI = Integer.parseInt(atts.getValue("I"));
		mCurrentJ = Integer.parseInt(atts.getValue("J"));
		mCurrentK = Integer.parseInt(atts.getValue("K"));
		mCurrentNodeIndex = getCurrentNodeIndex(mCurrentI, mCurrentJ, mCurrentK);
	}
	
	/**
	 * helper parsing function for the STATS tag
	 * @param atts
	 */
	private void parseStats(Attributes atts) {
		Log.d("joelog", "Parsing : " + mCurrentI + ", " + mCurrentJ + ", " + mCurrentK);
		mParsedDataSet.mNodes.get(mCurrentNodeIndex).type = atts.getValue("TYPE");
		if(atts.getValue("MIN_SPEED") != null) {
			mParsedDataSet.mNodes.get(mCurrentNodeIndex).minSpeed = Integer.parseInt(atts.getValue("MIN_SPEED"));
		}
		if(atts.getValue("MAX_SPEED") != null) {
			mParsedDataSet.mNodes.get(mCurrentNodeIndex).maxSpeed = Integer.parseInt(atts.getValue("MAX_SPEED"));
		}
		if(atts.getValue("KEY_ID") != null) {
			mParsedDataSet.mNodes.get(mCurrentNodeIndex).keyId = Integer.parseInt(atts.getValue("KEY_ID"));
		}
	}
	
	/**
	 * helper parsing function for the PRETARGET tag
	 * @param atts
	 */
	private void parsePreconnection(Attributes atts) {
		Log.d("DEBUG", "Parsing : " + mParsedDataSet.mNodes.getCount());
		mParsedDataSet.mNodes.get(mCurrentNodeIndex).addPreconnection(
				Integer.parseInt(atts.getValue("I")),
				Integer.parseInt(atts.getValue("J")),
				Integer.parseInt(atts.getValue("K")));
	}

    /**
     * begin adding badges to the map data
     */
    private void parseBadges() {
        mParsedDataSet.initBadges();
    }

    /**
     * parse a new badge
     * @param atts
     */
    private void parseBadge(Attributes atts) {
        mParsedDataSet.addBadge(Integer.parseInt(atts.getValue("type")));

    }

    /**
     * parse a requirements node
     * @param atts  the type and value for the requirement
     */
    private void parseRequirement(Attributes atts) {
        mParsedDataSet.addRequirementToCurrentBadge(atts.getValue("type"), Integer.parseInt(atts.getValue("value")));
    }

	/**
	 * get the node template parsed data set index based on it's i, j, and k index
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	private int getCurrentNodeIndex(int i, int j, int k) {
		int result = -1;
		for(int index = 0; index < mParsedDataSet.mTotalNumNodes; index++) {
			if(mParsedDataSet.mNodes.get(index).i == i && mParsedDataSet.mNodes.get(index).j == j && mParsedDataSet.mNodes.get(index).k == k) {
				result = index;
			}
		}
		return result;
	}


	@Override
	public void startDocument() throws SAXException {
		mParsedDataSet = new ParsedMapData();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("joelog", "enddocument called");
		//mParsedDataSet.setStartAndEndIndices();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		Log.d("joelog", "startElement called, localName: " + localName);
		if (localName.equals("MAP")) {
			parseMap(atts);
		} else if (localName.equals("NODE")) {
			parseNode(atts);
		} else if (localName.equals("STATS")) {
			parseStats(atts);
		} else if (localName.equals("PRECONNECTION")) {
			parsePreconnection(atts);
		} else if (localName.equals("badges")) {
            parseBadges();
        } else if (localName.equals("badge")) {
            parseBadge(atts);
        } else if (localName.equals("requirement")) {
            parseRequirement(atts);
        }
	}
}