package org.alchemicstudio;

public class MenuManager {
	
	private DrawableObject roboSprite;
	private DrawableObject backgroundHeadSprite;
	private DrawableObject backgroundBaseSprite;
	private DrawableObject redGearSprite;
	private DrawableObject yellowGearSprite;
	private DrawableObject pinkGearSprite;
	private DrawableObject craneSprite;
	private DrawableObject menuPipeSprite1;
	private DrawableObject menuPipeSprite2;
	private DrawableObject menuPipeSprite3;
	
	private float angle = 0.0f;

	public MenuManager() {
		int[] spriteArray = {R.drawable.gold1, R.drawable.gold2, R.drawable.gold3, R.drawable.gold4};
		roboSprite = new DrawableObject(spriteArray, 0, 50.0f, 64.0f, 300);
		
		int[] spriteArray2 = {R.drawable.bg_head};
		backgroundHeadSprite = new DrawableObject(spriteArray2, 0, 480.0f, 480.0f, 0);
		
		int[] spriteArray3 = {R.drawable.bg_base};
		backgroundBaseSprite = new DrawableObject(spriteArray3, 0, 480.0f, 480.0f, 0);
		
		int[] spriteArray4 = {R.drawable.menu_pipe};
		menuPipeSprite1 = new DrawableObject(spriteArray4, 0, 32.0f, 32.0f, 0);
		menuPipeSprite1.mSprite.setScale(15, 1);
		menuPipeSprite1.mSprite.modTex(6.0f);
		
		menuPipeSprite2 = new DrawableObject(spriteArray4, 0, 32.0f, 32.0f, 0);
		menuPipeSprite2.mSprite.setScale(15, 1);
		menuPipeSprite2.mSprite.modTex(6.0f);
		
		menuPipeSprite3 = new DrawableObject(spriteArray4, 0, 32.0f, 32.0f, 0);
		menuPipeSprite3.mSprite.setScale(15, 1);
		menuPipeSprite3.mSprite.modTex(6.0f);
		
		int[] spriteArray5 = {R.drawable.red_gear};
		redGearSprite = new DrawableObject(spriteArray5, 0, 52.0f, 52.0f, 0);
		
		int[] spriteArray6 = {R.drawable.yellow_gear};
		yellowGearSprite = new DrawableObject(spriteArray6, 0, 32.0f, 32.0f, 0);
		
		int[] spriteArray7 = {R.drawable.pink_gear};
		pinkGearSprite = new DrawableObject(spriteArray7, 0, 58.0f, 58.0f, 0);
		
		int[] spriteArray8 = {R.drawable.crane};
		craneSprite = new DrawableObject(spriteArray8, 0, 100.0f, 120.0f, 0);
	}

	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(float timeDelta) {
		menuPipeSprite1.update(timeDelta);
		menuPipeSprite1.setPositionAndAngle(0, 0, -264);
		
		menuPipeSprite2.update(timeDelta);
		menuPipeSprite2.setPositionAndAngle( 0, 0, -422);
		
		menuPipeSprite3.update(timeDelta);
		menuPipeSprite3.setPositionAndAngle( 0, 0, -578);
		
		backgroundHeadSprite.update(timeDelta);
		backgroundHeadSprite.setPositionAndAngle( 0, 0, -480);
		
		backgroundBaseSprite.update(timeDelta);
		backgroundBaseSprite.setPositionAndAngle( 0, 0, -854);
		
		roboSprite.update(timeDelta);
		roboSprite.setPositionAndAngle( 0, 392, -168);
		
		redGearSprite.update(timeDelta);
		redGearSprite.setPositionAndAngle(-angle/3, 25, -830);
		
		yellowGearSprite.update(timeDelta);
		yellowGearSprite.setPositionAndAngle(angle, 8, -792);
		
		pinkGearSprite.update(timeDelta);
		pinkGearSprite.setPositionAndAngle(angle/2, 410, -830);
		
		craneSprite.update(timeDelta);
		craneSprite.setPositionAndAngle(0, 340, -843);
		
		angle++;
	}

}
