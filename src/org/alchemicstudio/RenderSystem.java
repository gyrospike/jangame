package org.alchemicstudio;

import org.alchemicstudio.Sprite.PriorityComparator;

import android.util.Log;

public class RenderSystem extends BaseObject {

	private final static int DRAW_QUEUE_COUNT = 2;
	private final static int MAX_DRAWABLE_ELEMENTS = 400;
	
	private PriorityComparator priorityComparator = new PriorityComparator();
	private FixedSizeArray<Sprite> spriteList[] = new FixedSizeArray[DRAW_QUEUE_COUNT];
	private FixedSizeArray<TextBox> textBoxList[] = new FixedSizeArray[DRAW_QUEUE_COUNT];
	private int currentBufferIndex;

	public RenderSystem() {
		super();
		for(int i = 0; i < DRAW_QUEUE_COUNT; i++) {
			spriteList[i] = new FixedSizeArray<Sprite>(MAX_DRAWABLE_ELEMENTS);
			textBoxList[i] = new FixedSizeArray<TextBox>(MAX_DRAWABLE_ELEMENTS);
			spriteList[i].setComparator(priorityComparator);
		}
		currentBufferIndex = 0;
	}
	
	public void scheduleForWrite(TextBox tBox) {
		if (tBox != null) {
			textBoxList[currentBufferIndex].add(tBox);
		}
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

		renderer.setDrawQuadQueue(spriteList[currentBufferIndex]);
		renderer.setTextBoxQueue(textBoxList[currentBufferIndex]);
		
		final int lastQueue = (currentBufferIndex == 0) ? DRAW_QUEUE_COUNT - 1 : currentBufferIndex - 1;
		
		clearQueue(spriteList[lastQueue]);
		clearQueue(textBoxList[lastQueue]);
		
		currentBufferIndex = (currentBufferIndex + 1) % DRAW_QUEUE_COUNT;
	}

	private void clearQueue(FixedSizeArray objects) {
		final int count = objects.getCount();
		for (int i = count - 1; i >= 0; i--) {
			objects.removeLast();
		}
	}
	
	/* Empties all draw queues and disconnects the game thread from the renderer. */
    public void emptyQueues(GameRenderer renderer) {
        renderer.setDrawQuadQueue(null); 
        for (int x = 0; x < DRAW_QUEUE_COUNT; x++) {
            //mRenderQueues[x].commitUpdates();
            FixedSizeArray<Sprite> objects = spriteList[x];
            clearQueue(objects);
        }
    }
}
