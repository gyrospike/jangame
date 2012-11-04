package org.alchemicstudio;

import android.graphics.Color;
import android.util.Log;

public class HUD extends BaseObject {

	public static final String NOT_UNIQUE_ELEMENT = "X";
	
	public static final String UNIQUE_ELEMENT_COMPLETE = "complete";
	
	public static final String UNIQUE_ELEMENT_SPARK_SPEED = "sparkspeed";
	
	public static final String UNIQUE_ELEMENT_PLAY_TIME = "playtime";

	private static final int MAX_HUD_ELEMENTS = 100; 

	private static HUD mInstance = null;

	private FixedSizeArray<HUDDrawableElement> mElements = new FixedSizeArray<HUDDrawableElement>(MAX_HUD_ELEMENTS);
	
	private FixedSizeArray<HUDTextElement> mTextElements = new FixedSizeArray<HUDTextElement>(MAX_HUD_ELEMENTS);
	
	private FixedSizeArray<HUDStaticTextElement> mStaticTextElements = new FixedSizeArray<HUDStaticTextElement>(MAX_HUD_ELEMENTS);

	public static HUD getInstance() {
		if(mInstance == null) {
			mInstance = new HUD();
		}
		return mInstance;
	}

	public void addElement(int elementType, ImagePack imagePack, float x, float y, double angle, int flashTimeMS, boolean visible, String uniqueID) {
		int elementsLen = mElements.getCount();
		if(elementsLen < MAX_HUD_ELEMENTS) {
			if(uniqueID != HUD.NOT_UNIQUE_ELEMENT) {
				for(int k = 0; k < elementsLen; k++) {
					// == checks for reference equality, .equals checks for value equality
					if(mElements.get(k).getUniqueID().equals(uniqueID)) {
						mElements.remove(k);
						break;
					}
				}
			}
			mElements.add(new HUDDrawableElement(elementType, flashTimeMS, x, y, angle, imagePack, 2, 0, visible, uniqueID));
		} else {
			Log.d("DEBUG", "reached hud element limit");
		}

	}
	
	public void showStaticTextElement(int elementType, int textId, float x, float y, boolean visible, String uniqueID) {
		int elementsLen = mStaticTextElements.getCount();
		if(elementsLen < MAX_HUD_ELEMENTS) {
			mStaticTextElements.add(new HUDStaticTextElement(uniqueID, x, y, textId));
		} else {
			Log.d("DEBUG", "reached hud static text reference limit");
		}
	}
	
	public void addTextElement(int elementType, String text, int textSize, int color, float x, float y, boolean visible, String uniqueID) {
		int elementsLen = mTextElements.getCount();
		if(elementsLen < MAX_HUD_ELEMENTS) {
			HUDTextElement newSText = new HUDTextElement(uniqueID);
			newSText.setText(text);
			newSText.setTextSize(textSize);
			newSText.setPosition(x, y);
			newSText.setColor(color);
			mTextElements.add(newSText);
		} else {
			Log.d("DEBUG", "reached hud text limit");
		}
	}
	
	public void modifyTextElement(String newText, String uniqueID) {
		final int lenText = mTextElements.getCount();
		for(int i = 0; i < lenText; i++) {
			if(mTextElements.get(i).getUniqueID().equals(uniqueID)) {
				mTextElements.get(i).setText(newText);
			}
		}
	}
	
	public void flushAll() {
		int drawElemLen = mElements.getCount();
		for(int i = 0; i < drawElemLen; i++) {
			mElements.removeLast();
		}
		int textElemLen = mTextElements.getCount();
		for(int j = 0; j < textElemLen; j++) {
			mTextElements.removeLast();
		}
		int sTextElemLen = mStaticTextElements.getCount();
		for(int j = 0; j < sTextElemLen; j++) {
			mStaticTextElements.removeLast();
		}
	}
	
	/**
	 * remove this unique element from the hud
	 * 
	 * @param uniqueID
	 */
	public void removeStaticTextElement(String uniqueID) {
		int elementsLen = mStaticTextElements.getCount();
		for(int k = 0; k < elementsLen; k++) {
			if(mStaticTextElements.get(k).getUniqueID().equals(uniqueID)) {
				mStaticTextElements.remove(k);
				break;
			}
		}
	}
	
	/**
	 * remove this unique element from the hud
	 * 
	 * @param uniqueID
	 */
	public void removeTextElement(String uniqueID) {
		int elementsLen = mTextElements.getCount();
		for(int k = 0; k < elementsLen; k++) {
			if(mTextElements.get(k).getUniqueID().equals(uniqueID)) {
				mTextElements.remove(k);
				break;
			}
		}
	}

	/**
	 * remove this unique element from the hud
	 * 
	 * @param uniqueID
	 */
	public void removeElement(String uniqueID) {
		int elementsLen = mElements.getCount();
		for(int k = 0; k < elementsLen; k++) {
			if(mElements.get(k).getUniqueID().equals(uniqueID)) {
				mElements.remove(k);
				break;
			}
		}
	}

	public void setElementsVisibility(int elementType, boolean active) {
		final int len = mElements.getCount();
		for(int i = 0; i < len; i++) {
			if(mElements.get(i).getType() == elementType) {
				mElements.get(i).setActive(active);
			}
		}
	}

	@Override
	public void update(long timeDelta) {
		final int len = mElements.getCount();
		for(int i = 0; i < len; i++) {
			mElements.get(i).update(timeDelta);
		}
		final int lenText = mTextElements.getCount();
		for(int i = 0; i < lenText; i++) {
			mTextElements.get(i).update(timeDelta);
		}
		final int lenSText = mStaticTextElements.getCount();
		for(int i = 0; i < lenSText; i++) {
			mStaticTextElements.get(i).update(timeDelta);
		}
	}

}

class StaticTextReference {
	
	int mId;
	
	float mX;
	
	float mY;
	
	public StaticTextReference(float x, float y, int id) {
		mId = id;
		mX = x;
		mY = y;
	}
	
	public float getX() {
		return mX;
	}
	
	public float getY() {
		return mY;
	}
	
	public int getId() {
		return mId;
	}
}

class HUDStaticTextElement extends HUDElement {
	
	private StaticTextReference mStaticTextReference;
	
	public HUDStaticTextElement(String uniqueID, float x, float y, int textID) {
		super(-1, uniqueID);
		mStaticTextReference = new StaticTextReference(x, y, textID);
	}
	
	@Override
	public void update(long deltaTime) {
		sSystemRegistry.mRenderSystem.scheduleForShow(mStaticTextReference);
	}
	
}

class HUDTextElement extends HUDElement {

	/** the text box for the debug window */
	private TextBox mTextBox;
	
	public HUDTextElement(String uniqueID) {
		super(-1, uniqueID);
		mTextBox = new TextBox(0.0f, 0.0f);		
	}
	
	public TextBox getTextBox() {
		return mTextBox;
	}
	
	public void setColor(int color) {
		mTextBox.setARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
	}
	
	public void setText(String newText) {
		mTextBox.setText(newText);
	}
	
	public void setPosition(float posX, float posY) {
		mTextBox.setPosition(posX, posY);
	}
	
	public void setTextId(int id) {
		mTextBox.setIndex(id);
	}
	
	public void setTextSize(int size) {
		mTextBox.setTextSize(size);
	}
	
	@Override
	public void update(long deltaTime) {
		sSystemRegistry.mRenderSystem.scheduleForWrite(mTextBox);
	}
}


class HUDDrawableElement extends HUDElement {

	private float mFlashTime = 0;
	
	private float mFlashTimeTotal = 0;

	private boolean mActive = true;

	private DrawableObject mDrawableObject = null;

	public HUDDrawableElement(int eType, int fTime, float x, float y, double rad, ImagePack imagePack, int dP, int mspf, boolean visible, String uID) {
		super(eType, uID);
		mFlashTime = fTime;
		mFlashTimeTotal = fTime;
		mDrawableObject = new DrawableObject(imagePack, dP);
		mDrawableObject.setPosition(x, y);
		mDrawableObject.mSprite.setRotation(rad);
		setActive(visible);
		
		if(mFlashTime > 0) {
			mDrawableObject.setBaseOpacityDeficit(0.5f);
		}
	}

	public void setActive(boolean active) {
		mActive = active;
	}

	@Override
	public void update(long timeDelta) {
		if(mActive) {
			if(mFlashTime > 0) {
				mFlashTime -= timeDelta;
				mDrawableObject.setFlashOpacity(mFlashTime/mFlashTimeTotal);
			}
			mDrawableObject.update(timeDelta);
		}
	}
}

class HUDElement extends BaseObject {

	private int mElementType = 0;

	private String mUniqueID = "";

	public HUDElement(int eType, String uID) {
		mElementType = eType;
		mUniqueID = uID;
	}

	public String getUniqueID() {
		return mUniqueID;
	}

	public int getType() {
		return mElementType;
	}

}
