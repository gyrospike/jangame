package jan.game;

import jan.game.Sprite.PriorityComparator;

import android.util.Log;

public class RenderSystem extends BaseObject {

	private PriorityComparator priorityComparator = new PriorityComparator();
	private FixedSizeArray<Sprite> spriteList = new FixedSizeArray<Sprite>(80);

	public RenderSystem() {
		super();
		spriteList.setComparator(priorityComparator);
	}

	public void scheduleForDraw(Sprite sprite) {
		if (sprite != null) {
			spriteList.add(sprite);
		}
	}

	public void sendUpdates(GameRenderer renderer) {
		// ensures that the spriteArray contains sprites sorted by their
		// priorities, prevents background from being drawn over nodes
		spriteList.sort(false);

		final Object[] objectArray = spriteList.getArray();
		Sprite[] spriteArray = new Sprite[objectArray.length];

		int count = spriteArray.length;
		for (int i = 0; i < count; i++) {
			spriteArray[i] = (Sprite) objectArray[i];
		}

		renderer.setDrawQueue(spriteArray);
		clearQueue(spriteList);
	}

	private void clearQueue(FixedSizeArray<Sprite> objects) {
		final int count = objects.getCount();
		for (int i = count - 1; i >= 0; i--) {
			objects.removeLast();
		}
	}
}
