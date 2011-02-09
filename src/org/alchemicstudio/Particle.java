package org.alchemicstudio;

import java.util.Random;

import android.util.Log;

public class Particle extends BaseObject {

	//need to find a cleaner solution to dealing with acceleration, tiny little values to combat high update speed? lame.
	private static final float GRAVITY = 0.0001f;

	public Sprite mSprite;

	private float lifeSpan;
	private float lifeRemaining;
	private float glowIntensity;

	private float position_X;
	private float position_Y;

	private float velocity_X;
	private float velocity_Y;

	public Particle() {
		mSprite = new Sprite(0);
	}

	public void createParticle(int x, int y) {

		position_X = x;
		position_Y = y;

		Random r = new Random();
		lifeSpan = r.nextFloat() * 8000.0f;
		lifeRemaining = lifeSpan;

		velocity_X = (r.nextFloat() - 0.5f) * 0.1f;
		velocity_Y = (r.nextFloat() - 0.5f) * 0.1f;

		Log.d("DEBUG", "Particle created at: (" + x + ", " + y + ") with velocity: (" + velocity_X + ", " + velocity_Y + ")");
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		RenderSystem system = sSystemRegistry.renderSystem;
		if (lifeRemaining > 0) {

			glowIntensity = lifeRemaining / lifeSpan;
			lifeRemaining -= timeDelta;

			velocity_Y += timeDelta * GRAVITY;
			
			position_X += timeDelta * velocity_X;
			position_Y += timeDelta * velocity_Y;
			// Log.d("DEBUG", "Particle now at: (" + position_X + ", " +
			// position_Y + ") with velocity: (" + velocity_X + ", " +
			// velocity_Y + ")");

			mSprite.setPosition(position_X, position_Y);
			mSprite.setOpacity(glowIntensity);
			system.scheduleForDraw(mSprite);
		}
	}
}
