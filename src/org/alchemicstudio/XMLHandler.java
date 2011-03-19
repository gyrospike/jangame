package org.alchemicstudio;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XMLHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================

	private boolean in_map = false;
	private boolean in_node = false;
	private boolean in_stats = false;
	
	private int currentI;
	private int currentJ;

	private ParsedDataSet myParsedExampleDataSet = new ParsedDataSet();

	public ParsedDataSet getParsedData() {
		return myParsedExampleDataSet;
	}

	@Override
	public void startDocument() throws SAXException {
		myParsedExampleDataSet = new ParsedDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		Log.d("DEBUG", "startElement called, localName: " + localName);
		if (localName.equals("MAP")) {
			in_map = true;
			String numberString = atts.getValue("NUMBER");
			String widthString = atts.getValue("WIDTH");
			String heightString = atts.getValue("HEIGHT");
			String spacingString = atts.getValue("SPACING");
			
			myParsedExampleDataSet.setNumber(Integer.parseInt(numberString));
			myParsedExampleDataSet.setWidth(Integer.parseInt(widthString));
			myParsedExampleDataSet.setHeight(Integer.parseInt(heightString));
			myParsedExampleDataSet.setSpacing(Integer.parseInt(spacingString));
			myParsedExampleDataSet.initializeNodes();
			
		} else if (localName.equals("NODE")) {
			in_node = true;
			
			String iString = atts.getValue("I");
			String jString = atts.getValue("J");
			
			currentI = Integer.parseInt(iString);
			currentJ = Integer.parseInt(jString);
			
			myParsedExampleDataSet.addSpecialNode(currentI, currentJ);
			
			
		} else if (localName.equals("STATS")) {
			in_stats = true;
			
			String typeString = atts.getValue("TYPE");
			String linkString = atts.getValue("LINK");
			String minSpeedString = atts.getValue("MIN_SPEED");
			String maxSpeedString = atts.getValue("MAX_SPEED");
			String sourceString = atts.getValue("SOURCE");
			
			myParsedExampleDataSet.specialNodes.get(myParsedExampleDataSet.specialNodes.getCount()-1).type = Integer.parseInt(typeString);
			myParsedExampleDataSet.specialNodes.get(myParsedExampleDataSet.specialNodes.getCount()-1).link = Integer.parseInt(linkString);
			myParsedExampleDataSet.specialNodes.get(myParsedExampleDataSet.specialNodes.getCount()-1).minSpeed = Integer.parseInt(minSpeedString);
			myParsedExampleDataSet.specialNodes.get(myParsedExampleDataSet.specialNodes.getCount()-1).maxSpeed = Integer.parseInt(maxSpeedString);
			myParsedExampleDataSet.specialNodes.get(myParsedExampleDataSet.specialNodes.getCount()-1).source = Boolean.parseBoolean(sourceString);
		} 
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		Log.d("DEBUG", "endElement called, localName: " + localName);
		if (localName.equals("MAP")) {
			in_map = false;
		} else if (localName.equals("NODE")) {
			in_node = false;
		} else if (localName.equals("STATS")) {
			in_stats = false;
		} 
	}

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