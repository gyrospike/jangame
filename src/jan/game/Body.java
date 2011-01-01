package jan.game;

public class Body extends BaseObject {

	public Sprite mSprite;
	
	public Body(int x, int y) {
		mSprite = new Sprite(0);
		mSprite.xOffset = x;
		mSprite.yOffset = y;
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		system.scheduleForDraw(mSprite);
	}
}
