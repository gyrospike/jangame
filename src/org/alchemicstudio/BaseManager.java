package org.alchemicstudio;

public class BaseManager {

	/** has the manager been initialized */
	protected boolean mInitialized = false;
	
	
	public void init() {
		mInitialized = true;
	}
	
	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(long timeDelta) {
	}
	
	/**
	 * 
	 * @return	true if initialized
	 */
	public boolean getInitialized() {
		return mInitialized;
	}
	
}
