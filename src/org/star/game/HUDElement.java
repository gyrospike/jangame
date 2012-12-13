package org.star.game;

import android.graphics.Color;

public class HUDElement extends BaseObject {

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