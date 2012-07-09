package org.alchemicstudio;

import android.graphics.Color;
import android.util.Log;

public class HUD extends BaseObject {

	public static final String NOT_UNIQUE_ELEMENT = "X";

	private static final int MAX_HUD_ELEMENTS = 100; 

	private static HUD mInstance = null;

	private FixedSizeArray<HUDElement> mElements = new FixedSizeArray<HUDElement>(MAX_HUD_ELEMENTS);
	
	private FixedSizeArray<HUDTextElement> mTextElements = new FixedSizeArray<HUDTextElement>(MAX_HUD_ELEMENTS);

	public static HUD getInstance() {
		if(mInstance == null) {
			mInstance = new HUD();
		}
		return mInstance;
	}

	public void addElement(int elementType, Texture texture, float x, float y, double angle, int fadeTimeMS, boolean visible, String uniqueID) {
		Texture[] temp = {texture};
		addElement(elementType, temp, x, y, angle, fadeTimeMS, visible, uniqueID);
	}

	public void addElement(int elementType, Texture[] textures, float x, float y, double angle, int flashTimeMS, boolean visible, String uniqueID) {
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
			mElements.add(new HUDElement(elementType, flashTimeMS, x, y, angle, textures, 2, 0, visible, uniqueID));
		} else {
			Log.d("DEBUG", "reached hud element limit");
		}

	}
	
	public void addTextElement(int elementType, String text, int textSize, int color, float x, float y, boolean visible, String uniqueID) {
		int elementsLen = mTextElements.getCount();
		if(elementsLen < MAX_HUD_ELEMENTS) {
			HUDTextElement newSText = new HUDTextElement(text, uniqueID);
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
	}

}

class HUDTextElement extends BaseObject {

	private String mUniqueID = "";

	/** the text box for the debug window */
	private TextBox mTextBox;
	
	public HUDTextElement(String text, String uniqueID) {
		mTextBox = new TextBox(0.0f, 0.0f);
		mTextBox.setText(text);
		
		mUniqueID = uniqueID;
	}
	
	public String getUniqueID() {
		return mUniqueID;
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
	
	public void setTextSize(int size) {
		mTextBox.setTextSize(size);
	}
	
	@Override
	public void update(long deltaTime) {
		sSystemRegistry.mRenderSystem.scheduleForWrite(mTextBox);
	}
}


class HUDElement extends BaseObject {

	private int mElementType = 0;

	private float mFlashTime = 0;
	
	private float mFlashTimeTotal = 0;

	private String mUniqueID = "";

	private boolean mActive = true;

	private DrawableObject mDrawableObject = null;

	public HUDElement(int eType, int fTime, float x, float y, double rad, Texture[] tArray, int dP, int mspf, boolean visible, String uID) {
		mElementType = eType;
		mFlashTime = fTime;
		mFlashTimeTotal = fTime;
		mUniqueID = uID;
		mDrawableObject = new DrawableObject(tArray, dP, mspf);
		mDrawableObject.setPosition(x, y);
		mDrawableObject.mSprite.setRotation(rad);
		setActive(visible);
		
		if(mFlashTime > 0) {
			mDrawableObject.setBaseOpacityDeficit(0.5f);
		}
	}

	public String getUniqueID() {
		return mUniqueID;
	}

	public int getType() {
		return mElementType;
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
