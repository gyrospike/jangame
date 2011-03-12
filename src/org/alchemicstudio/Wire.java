package org.alchemicstudio;

import android.graphics.Point;

public class Wire extends BaseObject {

	public Sprite mSprite;
	public Point targetNode, originNode;
	public boolean active;
	public boolean permanent;

	private RenderSystem system = sSystemRegistry.renderSystem;
	
	public Wire(){
		active = false;
		mSprite = new Sprite(0, 1);
		targetNode = new Point(-1, -1);
		originNode = new Point(-1, -1);
	}
	
	public void setTarget(int i, int j) {
		targetNode.x = i;
		targetNode.y = j;
	}
	
	public void setOrigin(int i, int j) {
		originNode.x = i;
		originNode.y = j;
	}
	
	public Point getTarget() {
		return targetNode;
	}
	
	public Point getOrigin() {
		return originNode;
	}
	
	@Override
	public void update(float timeDelta, BaseObject parent) {
		system.scheduleForDraw(mSprite);	
	}
}
