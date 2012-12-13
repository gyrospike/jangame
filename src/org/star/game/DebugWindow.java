package org.star.game;

import org.star.game.BaseObject;
import org.star.game.TextBox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DebugWindow extends BaseObject {

	/** the main text box for the debug window */
	private TextBox mDebugTextBox;
	
	/** hashmap object that maps field keys to status values for debugging */
	private HashMap<String, String> mDebugTextMap;
	
	public DebugWindow() {
		mDebugTextMap = new HashMap<String, String>();
		mDebugTextBox = new TextBox(10, 200);
		mDebugTextBox.setARGB(255, 255, 255, 255);
	}
	
	/**
	 * concatenate all the key value pairs in the debug text and output
	 * 
	 * @return
	 */
	private String formatDebugText() {
		String result = "";
		Iterator<Entry<String, String>> it = mDebugTextMap.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
	        result += pairs.getKey() + " : " + pairs.getValue() + "\n";
		}
		return result;
	}
	
	/**
	 * the debug text is stored in hash map, call this function to add
	 * new debug output
	 * 
	 * @param key		the key of the hash map
	 * @param value		the value for the key
	 */
	public void updateTextBlock(String key, String value) {
		mDebugTextMap.put(key, value);
		mDebugTextBox.setText(formatDebugText());
	}
	
	@Override
	public void update(long timeDelta) {
		sSystemRegistry.mRenderSystem.scheduleForWrite(mDebugTextBox);
	}
}
