package jan.game;

import android.graphics.Point;

public class Spark extends BaseObject {

public Sprite mSprite;
	
	public Spark() {
		mSprite = new Sprite(2);
	}
	
	public void hide() {
		mSprite.setPosition(-32.0f, -32.0f);
	}
	
	public void setPosition(float x, float y) {
		mSprite.setPosition(x, y);
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		system.scheduleForDraw(mSprite);
	}
}
