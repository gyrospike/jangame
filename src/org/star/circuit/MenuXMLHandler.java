
package org.star.circuit;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MenuXMLHandler extends DefaultHandler {

    /** the entire data set of what we are parsing */
    private ParsedMenuData mParsedDataSet = new ParsedMenuData();

    /**
     * getter
     * @return	get the parsed data for this level
     */
    public ParsedMenuData getParsedData() {
        return mParsedDataSet;
    }

    /**
     * helper parsing function for the MAP tag
     * @param atts
     */
    private void parseRow(Attributes atts) {
        mParsedDataSet.appendNewRow();
    }

    /**
     * helper parsing function for the NODE tag
     * @param atts
     */
    private void parseEntry(Attributes atts) {
        String sourceFile = atts.getValue("sourceFile");
        String mapName = atts.getValue("mapName");
        String buttonId = atts.getValue("buttonId");
        int saveId = Integer.parseInt(atts.getValue("saveID"));
        mParsedDataSet.appendNewColumn(mapName, sourceFile, buttonId, saveId);
    }

    @Override
    public void startDocument() throws SAXException {
        mParsedDataSet = new ParsedMenuData();
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d("DEBUG", "enddocument called");
        //mParsedDataSet.setStartAndEndIndices();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        Log.d("DEBUG", "startElement called, localName: " + localName);
        if (localName.equals("row")) {
            parseRow(atts);
        } else if (localName.equals("entry")) {
            parseEntry(atts);
        }
    }
}