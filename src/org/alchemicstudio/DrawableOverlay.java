package org.alchemicstudio;

public class DrawableOverlay extends BaseObject {

	/** max number of particles we can have on screen */
	private final static int MAX_PARTICLE_ARRAY_SIZE = 30;

	/** current index for particle array */
	private int mParticleIndex = 0;

	/** array of particles */
	private Particle[] mParticleArray;

	public DrawableOverlay() {
		mParticleArray = new Particle[MAX_PARTICLE_ARRAY_SIZE];
		for (int i = 0; i < MAX_PARTICLE_ARRAY_SIZE; i++) {
			mParticleArray[i] = new Particle();
		}
	}

	/**
	 * Creates a shower of spark particles at the passed on position 
	 *
	 * @param x		x position
	 * @param y		y position
	 * @param num	how many particles to make
	 */
	public void createParticle(int x, int y, int num) {
		if (mParticleArray != null) {
			for (int i = 0; i < num; i++) {
				mParticleArray[mParticleIndex].createParticle(x, y);
				mParticleIndex++;
				if (mParticleIndex > mParticleArray.length - 1)
					mParticleIndex = 0;
			}
		}
	}

	@Override
	public void update(long timeDelta) {	
		for(int r = 0; r < mParticleArray.length; r++) {
			mParticleArray[r].update(timeDelta);
		}
	}
}
