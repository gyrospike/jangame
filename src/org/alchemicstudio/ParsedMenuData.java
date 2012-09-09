
package org.alchemicstudio;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

public class ParsedMenuData {

    public static final String MAP_NUMBER_KEY = "mapNum";

    public static final String MAP_RESOURCE_KEY = "mapRes";

    private static final String PREFIX_KEY = "map";

    private int mCurrentRow = -1;

    private int mCurrentColumn = -1;

    private int mMaxNumColumns = 0;

    /** hash map of menu row, column to level data */
    HashMap<String, MenuObject> mMenuHash = new HashMap<String, MenuObject>();

    public void appendNewColumn(int mapNumber, String mapSourceFile, String buttonId) {
        mCurrentColumn++;
        mMenuHash.put(PREFIX_KEY+mCurrentRow+mCurrentColumn, new MenuObject(mapNumber, mapSourceFile, buttonId));
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

    public int getMapNumber(int row, int col) {
        return mMenuHash.get(PREFIX_KEY+row+col).getMapNumber();
    }

    public String getButtonName(int row, int col) {
        return mMenuHash.get(PREFIX_KEY+row+col).getButtonId();
    }

    public String getMapSourceFileName(int row, int col) {
        return mMenuHash.get(PREFIX_KEY+row+col).getSourceFile();
    }

}

class MenuObject {

    private String mMapSourceFile;

    private String mButtonId;

    private int mMapNumber;

    public MenuObject(int mapNum, String srcFile, String buttonId) {
        mMapNumber = mapNum;
        mMapSourceFile = srcFile;
        mButtonId = buttonId;
    }

    public String getButtonId() {
        return mButtonId;
    }

    public String getSourceFile() {
        return mMapSourceFile;
    }

    public int getMapNumber() {
        return mMapNumber;
    }
}