package org.alchemicstudio;

public class Marker extends BaseObject {
	
	public Sprite mSprite;
	
	public Marker(float x, float y) {
        ImagePack imagePack = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("marker");
		mSprite = new Sprite(imagePack, 0);
		mSprite.setPosition(x, y);
	}
	
	@Override
	public void update(long timeDelta) {
		RenderSystem system = sSystemRegistry.mRenderSystem;
		system.scheduleForDraw(mSprite);
	}
}
