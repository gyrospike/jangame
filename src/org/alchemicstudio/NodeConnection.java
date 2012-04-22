package org.alchemicstudio;

public class NodeConnection {
	
	private int mI = -1;
	
	private int mJ = -1;
	
	private int mK = -1;
	
	private int mTrackID = -1;
	
	public NodeConnection(int i, int j, int k) {
		mI = i;
		mJ = j;
		mK = k;
	}
	
	public int getI() {
		return mI;
	}
	
	public int getJ() {
		return mJ;
	}
	
	public int getK() {
		return mK;
	}
	
	public void setTrackID(int id) {
		mTrackID = id;
	}
	
	public int getTrackID() {
		return mTrackID;
	}
	
	public boolean hasValueOf(int i, int j, int k) {
		boolean result = false;
		if(mI == i && mJ == j && mK == k) {
			result = true;
		}
		return result;
	}
	
	public boolean isEmptyNodeConnection() {
		boolean result = false;
		if(mI == -1 && mJ == -1 && mK == -1) {
			result = true;
		}
		return result;
	}
}
