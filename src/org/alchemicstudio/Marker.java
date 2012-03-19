package org.alchemicstudio;

public class Marker extends BaseObject {
	
	public Sprite mSprite;
	
	public Marker(float x, float y) {
		int[] textureArray = {R.drawable.white_box};
		mSprite = new Sprite(textureArray, 0, 4.0f, 4.0f, 1, 0);
		mSprite.setPosition(x, y);
	}
	
	@Override
	public void update(float timeDelta) {
		RenderSystem system = sSystemRegistry.mRenderSystem;
		system.scheduleForDraw(mSprite);
	}
}
