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
	 * update based on the time step passed in
	 * 
	 * @param timeDelta
	 */
	public void update(float timeDelta) {
		
	}
}
