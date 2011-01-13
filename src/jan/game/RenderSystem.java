package jan.game;

import jan.game.Sprite.PriorityComparator;

import android.util.Log;

public class RenderSystem extends BaseObject {

	private final static int DRAW_QUEUE_COUNT = 2;
	private final static int MAX_DRAWABLE_ELEMENTS = 400;
	
	private PriorityComparator priorityComparator = new PriorityComparator();
	private FixedSizeArray<Sprite> spriteList[] = new FixedSizeArray[DRAW_QUEUE_COUNT];
	private int currentBufferIndex;

	public RenderSystem() {
		super();
		for(int i = 0; i < DRAW_QUEUE_COUNT; i++) {
			spriteList[i] = new FixedSizeArray<Sprite>(MAX_DRAWABLE_ELEMENTS);
			spriteList[i].setComparator(priorityComparator);
		}
		currentBufferIndex = 0;
	}

	public void scheduleForDraw(Sprite sprite) {
		if (sprite != null) {
			spriteList[currentBufferIndex].add(sprite);
		}
	}

	public void sendUpdates(GameRenderer renderer) {
		// ensures that the spriteArray contains sprites sorted by their
		// priorities, prevents background from being drawn over nodes
		spriteList[currentBufferIndex].sort(false);

		renderer.setDrawQueue(spriteList[currentBufferIndex]);
		
		final int lastQueue = (currentBufferIndex == 0) ? DRAW_QUEUE_COUNT - 1 : currentBufferIndex - 1;
		
		clearQueue(spriteList[lastQueue]);
		
		currentBufferIndex = (currentBufferIndex + 1) % DRAW_QUEUE_COUNT;
	}

	private void clearQueue(FixedSizeArray<Sprite> objects) {
		final int count = objects.getCount();
		for (int i = count - 1; i >= 0; i--) {
			objects.removeLast();
		}
	}
}
