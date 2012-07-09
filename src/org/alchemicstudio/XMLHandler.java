package org.alchemicstudio;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XMLHandler extends DefaultHandler {

	/** the most recent node index i to be parsed */
	private int mCurrentI;
	
	/** the most recent node index j to be parsed */
	private int mCurrentJ;
	
	/** the most recent node index k to be parsed */
	private int mCurrentK;
	
	/** the index of the most recent node to be parsed */
	private int mCurrentNodeIndex;

	/** the entire data set of what we are parsing */
	private ParsedDataSet mParsedDataSet = new ParsedDataSet();

	/**
	 * getter
	 * @return	get the parsed data for this level
	 */
	public ParsedDataSet getParsedData() {
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
		Log.d("DEBUG", "Parsing : " + mParsedDataSet.mNodes.getCount());
		mParsedDataSet.mNodes.get(mCurrentNodeIndex).type = atts.getValue("TYPE");
		if(atts.getValue("MIN_SPEED") != null) {
			mParsedDataSet.mNodes.get(mCurrentNodeIndex).minSpeed = Integer.parseInt(atts.getValue("MIN_SPEED"));
		}
		if(atts.getValue("MAX_SPEED") != null) {
			mParsedDataSet.mNodes.get(mCurrentNodeIndex).maxSpeed = Integer.parseInt(atts.getValue("MAX_SPEED"));
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
	 * get the node template parsed data set index based on it's i, j, and k index
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	private int getCurrentNodeIndex(int i, int j, int k) {
		int result = -1;
		int totalNodeCount =  mParsedDataSet.mNodes.getCount();
		for(int index = 0; index < totalNodeCount; index++) {
			if(mParsedDataSet.mNodes.get(index).i == i && mParsedDataSet.mNodes.get(index).j == j && mParsedDataSet.mNodes.get(index).k == k) {
				result = index;
			}
		}
		return result;
	}


	@Override
	public void startDocument() throws SAXException {
		mParsedDataSet = new ParsedDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("DEBUG", "enddocument called");
		//mParsedDataSet.setStartAndEndIndices();
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		Log.d("DEBUG", "startElement called, localName: " + localName);
		if (localName.equals("MAP")) {
			parseMap(atts);
		} else if (localName.equals("NODE")) {
			parseNode(atts);
		} else if (localName.equals("STATS")) {
			parseStats(atts);
		} else if (localName.equals("PRECONNECTION")) {
			parsePreconnection(atts);
		} 
	}

	//TODO - need this still?
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		Log.d("DEBUG", "endElement called, localName: " + localName);
		/*
		if (localName.equals("MAP")) {
			in_map = false;
		} else if (localName.equals("NODE")) {
			in_node = false;
		} else if (localName.equals("STATS")) {
			in_stats = false;
		}
		*/
	}

	//TODO - need this still?
	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		//Log.d("DEBUG", "characters called: " + ch.length);
		/*
		if (in_mytag) {
			Log.d("DEBUG", "string gettting extracted!");
			myParsedExampleDataSet.setExtractedString(new String(ch, start, length));
			Log.d("DEBUG", "string: " + myParsedExampleDataSet.getExtractedString());
		}
		*/
	}
}