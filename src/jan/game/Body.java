package jan.game;

public class Body extends BaseObject {

	public Sprite mSprite;
	
	public Body() {
		mSprite = new Sprite(0);
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		mSprite.textureIndex = 0;
		system.scheduleForDraw(mSprite);
	}
}
