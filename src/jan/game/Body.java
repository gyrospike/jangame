package jan.game;

import android.util.Log;

public class Body extends BaseObject {

	public Sprite mSprite;
	
	private Vector2 rotationOrigin;
	private float rotationInc;
	private float angle;
	private float radius;
	private boolean originRotate;
	
	public Body(int x, int y, float rot) {
		mSprite = new Sprite(0);
		originRotate = false;
		rotationInc = rot;
		mSprite.xOffset = x;
		mSprite.yOffset = y;
	}
	
	public void setRotationOrigin(Vector2 origin) {
		originRotate = true;
		radius = origin.distance2(new Vector2(mSprite.xOffset, mSprite.yOffset));
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		mSprite.rotation += rotationInc;
		if(originRotate) {
			angle += 0.005;
			mSprite.xOffset = (float)(radius * Math.cos(angle));
			mSprite.yOffset = (float)(radius * Math.sin(angle));
			Log.d("DEBUG", "cos: " + (float)(radius * Math.cos(angle)));
		}
		system.scheduleForDraw(mSprite);
	}
}
