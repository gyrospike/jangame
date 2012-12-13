package org.star.circuit;

import org.star.common.game.BaseObject;
import org.star.common.game.ImagePack;
import org.star.common.game.RenderSystem;
import org.star.common.game.Sprite;

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
