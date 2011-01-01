package jan.game;


import android.util.Log;

public class ObjectManager extends BaseObject {

	private FixedSizeArray<BaseObject> mObjects = new FixedSizeArray<BaseObject>(
			80);

	public ObjectManager() {
		super();
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

	public void add(BaseObject object) {
		mObjects.add(object);
		Log.d("DEBUG", "Object added: " + mObjects.getCount());
	}
}
