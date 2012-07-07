package org.alchemicstudio;

import android.util.Log;

public class HUD extends BaseObject {
	
	public static final String NOT_UNIQUE_ELEMENT = "X";

	private static final int MAX_HUD_ELEMENTS = 100; 
	
	private static HUD mInstance = null;
	
	private FixedSizeArray<HUDElement> mElements = new FixedSizeArray<HUDElement>(MAX_HUD_ELEMENTS);
	
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
	
	public void addElement(int elementType, Texture[] textures, float x, float y, double angle, int fadeTimeMS, boolean visible, String uniqueID) {
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
			mElements.add(new HUDElement(elementType, fadeTimeMS, x, y, angle, textures, 2, 0, visible, uniqueID));
		} else {
			Log.d("DEBUG", "reached hud element limit");
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
	}
	
}

class HUDElement extends BaseObject {
	
	private int mElementType = 0;
	
	private int mfadeTime = 0;
	
	private String mUniqueID = "";
	
	private boolean mActive = true;
	
	private DrawableObject mDrawableObject = null;
	
	public HUDElement(int eType, int fTime, float x, float y, double rad, Texture[] tArray, int dP, int mspf, boolean visible, String uID) {
		mElementType = eType;
		mfadeTime = fTime;
		mUniqueID = uID;
		mDrawableObject = new DrawableObject(tArray, dP, mspf);
		mDrawableObject.setPosition(x, y);
		mDrawableObject.mSprite.setRotation(rad);
		setActive(visible);
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
			mDrawableObject.update(timeDelta);
		}
	}
}
