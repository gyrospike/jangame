package org.alchemicstudio;

public class Marker extends BaseObject {
	
	public Sprite mSprite;
	
	public Marker(float x, float y) {
		mSprite = new Sprite(0, 1);
		mSprite.setPosition(x, y);
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		system.scheduleForDraw(mSprite);
	}
}
