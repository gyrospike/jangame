package org.star.game;

import android.graphics.Color;
import android.util.Log;
import org.star.types.FixedSizeArray;

public class HUD extends BaseObject {

	public static final String NOT_UNIQUE_ELEMENT = "X";

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
			if(!uniqueID.equals(HUD.NOT_UNIQUE_ELEMENT)) {
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
