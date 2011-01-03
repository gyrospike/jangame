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
		mSprite.setPosition(x, y);
	}
	
	public void setRotationOrigin(Vector2 origin) {
		originRotate = true;
		radius = origin.distance(mSprite.getPosition());
		rotationOrigin = origin;
		Log.d("DEBUG", "radius: " + radius);
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		mSprite.rotation += rotationInc;
		if(originRotate) {
			angle += 0.1;	
			mSprite.setPosition(rotationOrigin.x + (float)(radius * Math.cos(angle)), rotationOrigin.y + (float)(radius * Math.sin(angle)));
		}
		system.scheduleForDraw(mSprite);
	}
}
