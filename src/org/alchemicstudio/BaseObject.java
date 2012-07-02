package org.alchemicstudio;

public class BaseObject {
	
	/** global objects reference */
	static ObjectRegistry sSystemRegistry = new ObjectRegistry();
	
	/**
	 * The base object that all 'update-needing' objects inherit from
	 * 
	 */
	public BaseObject() {
		
	}
	
	/**
	 * initialize global system objects
	 */
	public static void conditionallyInitializeBaseObjects() {
		if(BaseObject.sSystemRegistry.mRenderSystem == null) {
			RenderSystem renderer = new RenderSystem();
			BaseObject.sSystemRegistry.mRenderSystem = renderer;
		}
		if(BaseObject.sSystemRegistry.mAssetLibrary == null) {
			AssetLibrary assetLibrary = new AssetLibrary();
			BaseObject.sSystemRegistry.mAssetLibrary = assetLibrary;
		}		
	}
	
	/**
	 * update based on the time step passed in
	 * 
	 * @param timeDelta
	 */
	public void update(float timeDelta) {
		
	}
}
