package org.alchemicstudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Menu extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);

		Button Map1 = (Button) findViewById(R.id.Button01);
		Map1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map2 = (Button) findViewById(R.id.Button02);
		Map2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map3 = (Button) findViewById(R.id.Button03);
		Map3.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map4 = (Button) findViewById(R.id.Button04);
		Map4.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
	}

}
