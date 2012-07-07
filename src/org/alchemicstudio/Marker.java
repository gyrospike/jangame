package org.alchemicstudio;

public class Marker extends BaseObject {
	
	public Sprite mSprite;
	
	public Marker(float x, float y) {
		Texture texture = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.white_box);
		mSprite = new Sprite(texture, 0);
		mSprite.setPosition(x, y);
	}
	
	@Override
	public void update(long timeDelta) {
		RenderSystem system = sSystemRegistry.mRenderSystem;
		system.scheduleForDraw(mSprite);
	}
}
