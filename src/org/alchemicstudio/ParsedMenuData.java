
package org.alchemicstudio;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

public class ParsedMenuData {

    public static final String MAP_SAVE_ID_KEY = "saveId";

    public static final String MAP_NAME_RESOURCE_KEY = "mapNameRes";

    public static final String MAP_RESOURCE_KEY = "mapRes";

    private int mCurrentRow = -1;

    private int mCurrentColumn = -1;

    private int mMaxNumColumns = 0;

    /** hash map of menu row, column to level data */
    HashMap<String, MenuObject> mMenuHash = new HashMap<String, MenuObject>();

    public void appendNewColumn(String mapName, String mapSourceFile, String buttonId, int saveId) {
        mCurrentColumn++;
        mMenuHash.put(buttonId, new MenuObject(mapName, mapSourceFile, buttonId, saveId));
        if(mCurrentColumn > mMaxNumColumns) {
            mMaxNumColumns = mCurrentColumn;
        }
    }

    public void appendNewRow() {
        mCurrentRow++;
        mCurrentColumn = -1;
    }

    public int getNumRows() {
        return mCurrentRow+1;
    }

    public int getNumColumns() {
        return mMaxNumColumns+1;
    }

    public int getSaveId(String buttonId) {
        return mMenuHash.get(buttonId).getSaveId();
    }

    public String getMapName(String buttonId) {
        return mMenuHash.get(buttonId).getMapName();
    }

    public String getMapSourceFileName(String buttonId) {
        return mMenuHash.get(buttonId).getSourceFile();
    }

    /**
     * I know, I know... terrible
     * @param row
     * @param col
     * @return
     */
    public String getButtonName(int row, int col) {
        String result = "";
        if(mMenuHash.get("Button"+row+col) != null) {
            result = mMenuHash.get("Button"+row+col).getButtonId();
        }
        return result;
    }



}

class MenuObject {

    private String mMapSourceFile;

    private String mButtonId;

    private String mMapName;

    private int mSaveId;

    public MenuObject(String mapName, String srcFile, String buttonId, int saveId) {
        mMapName = mapName;
        mMapSourceFile = srcFile;
        mButtonId = buttonId;
        mSaveId = saveId;
    }

    public String getButtonId() {
        return mButtonId;
    }

    public String getSourceFile() {
        return mMapSourceFile;
    }

    public String getMapName() {
        return mMapName;
    }

    public int getSaveId() {
        return mSaveId;
    }
}