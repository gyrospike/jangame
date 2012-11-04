package org.alchemicstudio;

import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: joe
 * Date: 11/3/12
 * Time: 8:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePackXMLHandler extends DefaultHandler {

    /** the image pack mapping */
    private HashMap<String, ImagePack> mImagePacks;

    /** the current image pack being parsed out of the xml */
    private ImagePack mCurrentImagePack;

    /** the current image pack id being parsed out of the xml */
    private String mCurrentImagePackId;

    /** the current image pack usage code being parsed out of the xml*/
    private int mCurrentImagePackUsageCode;

    /** the current image id being parsed out of the xml */
    private String mCurrentImageId;

    /** the current frames time in miliseconds being parsed out of the xml */
    private int mCurrentFramesMS;

    /** the current array of resource variable names being parsed out of the xml */
    private String[] mCurrentIdArray;

    /** the contents of the tags being parsed out of the xml */
    private StringBuffer mCurrentCharValue = new StringBuffer(1024);

    public HashMap<String, ImagePack> getImagePacks() {
        return mImagePacks;
    }

    /**
     * Add another image into the current image pack
     */
    private void appendImage() {
        Class resourceIdClass = R.drawable.class;
        int len = mCurrentIdArray.length;
        int[] textureIds = new int[len];
        int i = 0;
        while(i < len) {
            String stringResource = mCurrentIdArray[i];
            try {
                textureIds[i] = (Integer)resourceIdClass.getField(stringResource).get(resourceIdClass);
            } catch (Exception e) {
                Log.e("DEBUG", "Failure to get drawable id.", e);
            }
            i++;
        }
        mCurrentImagePack.addEntry(mCurrentImageId, mCurrentImagePackUsageCode, textureIds, mCurrentFramesMS);
    }

    /**
     * Add another image pack into an image pack mapping by name
     */
    private void appendImagePack() {
        mImagePacks.put(mCurrentImagePackId, mCurrentImagePack);
    }

    /**
     * helper parsing function for the ImagePack tag
     * @param atts
     */
    private void parseImagePack(Attributes atts) {
        mCurrentImagePack = new ImagePack();
        mCurrentImagePackId = atts.getValue("id");
        mCurrentImagePackUsageCode = Integer.parseInt(atts.getValue("usage"));
    }

    /**
     * helper parsing function for the Image tag
     * @param atts
     */
    private void parseImage(Attributes atts) {
        mCurrentImageId = atts.getValue("id");
        if(atts.getValue("framesMS") != null) {
            mCurrentFramesMS = Integer.parseInt(atts.getValue("framesMS"));
        } else {
            mCurrentFramesMS = 0;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        mImagePacks = new HashMap<String, ImagePack>(64);
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d("DEBUG", "enddocument called");
    }

    @Override
    public void characters (char ch[], int start, int length) throws SAXException {
        mCurrentCharValue.append(ch, start, length);
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        Log.d("DEBUG", "startElement called, localName: " + localName);
        if (localName.equals("ImagePack")) {
            parseImagePack(atts);
        } else if (localName.equals("Image")) {
            parseImage(atts);
        } else if (localName.equals("Textures")) {
            int len = mCurrentCharValue.length();
            mCurrentCharValue.delete(0, len);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (localName.equals("ImagePack")) {
            appendImagePack();
        } else if (localName.equals("Image")) {
            appendImage();
        } else if (localName.equals("Textures")) {
            String textureString = mCurrentCharValue.toString();
            mCurrentIdArray =  textureString.split(",");
        }
    }

}