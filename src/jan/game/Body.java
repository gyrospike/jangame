package jan.game;

public class Body extends BaseObject {

	public Sprite mSprite;
	
	private float rotationInc;
	
	public Body(int x, int y, float rot) {
		mSprite = new Sprite(0);
		rotationInc = rot;
		mSprite.xOffset = x;
		mSprite.yOffset = y;
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		mSprite.rotation += rotationInc;
		system.scheduleForDraw(mSprite);
	}
}
