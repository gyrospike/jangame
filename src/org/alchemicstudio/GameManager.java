package org.alchemicstudio;

import android.util.Log;

public class GameManager extends BaseObject {

	public Grid gameGrid;
	
	private FixedSizeArray<BaseObject> mObjects = new FixedSizeArray<BaseObject>(420);
	private Particle[] particleArray;
	private int particleIndex;

	public GameManager() {
		super();
		TextBox tBox = new TextBox();
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		final int count = mObjects.getCount();
		if (count > 0) {
			final Object[] objectArray = mObjects.getArray();
			for (int i = 0; i < count; i++) {
				BaseObject object = (BaseObject) objectArray[i];
				object.update(timeDelta, this);
			}
		}
	}

	public void setParticleArray(Particle[] pArray) {
		particleArray = pArray;
		for (int i = 0; i < particleArray.length; i++) {
			mObjects.add(particleArray[i]);
		}
		gameGrid.setParticles(particleArray);
	}

	public void createParticle(int x, int y) {
		if (particleArray != null) {
			particleArray[particleIndex].createParticle(x, y);
			particleIndex++;
			if (particleIndex > particleArray.length - 1)
				particleIndex = 0;
		}
	}

	public void add(BaseObject object) {
		mObjects.add(object);
		Log.d("DEBUG", "Object added: " + mObjects.getCount());
	}

	public void addGrid(BaseObject object) {
		mObjects.add(object);
		gameGrid = (Grid) object;
	}
}
