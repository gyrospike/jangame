package org.star.game;

public class HUDStaticTextElement extends HUDElement {

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