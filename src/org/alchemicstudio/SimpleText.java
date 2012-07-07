package org.alchemicstudio;

public class SimpleText extends BaseObject {

	/** the text box for the debug window */
	private TextBox mTextBox;
	
	public SimpleText(String text) {
		mTextBox = new TextBox(0.0f, 0.0f);
		mTextBox.setText(text);
	}
	
	public TextBox getTextBox() {
		return mTextBox;
	}
	
	public void setPosition(float posX, float posY) {
		mTextBox.setPosition(posX, posY);
	}
	
	public void setTextSize(int size) {
		mTextBox.setTextSize(size);
	}
	
	@Override
	public void update(long deltaTime) {
		sSystemRegistry.mRenderSystem.scheduleForWrite(mTextBox);
	}
	
}
