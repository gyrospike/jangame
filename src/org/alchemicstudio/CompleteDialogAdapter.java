package org.alchemicstudio;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CompleteDialogAdapter extends BaseAdapter {

    private int mCount = 0;

    private String[] mTextData = null;

    private int[] mImageData = null;

    private static LayoutInflater inflater=null;

    public CompleteDialogAdapter(Activity act, String[] textData, int[] imageData) {
        mTextData=textData;
        mImageData = imageData;

        if(mTextData != null) {
            mCount = mTextData.length;
        }
        inflater = (LayoutInflater)act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mCount;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean areAllItemsEnabled()
    {
        return false;
    }
    public boolean isEnabled(int position)
    {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;

        if(convertView==null)   {
            vi = inflater.inflate(R.layout.complete_dialog_list_item, null);
        }

        if(mTextData != null) {
            TextView text=(TextView)vi.findViewById(R.id.text);;
            text.setText(mTextData[position]);
        }

        if(mImageData != null) {
            ImageView image=(ImageView)vi.findViewById(R.id.image);
            image.setImageResource(mImageData[position]);
        }

        return vi;
    }
}