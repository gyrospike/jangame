package org.alchemicstudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class Menu extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
		
		HorizontalScrollView sView = (HorizontalScrollView)findViewById(R.id.ScrollView01);
		//Hide the Scollbar
        sView.setVerticalScrollBarEnabled(false);
        sView.setHorizontalScrollBarEnabled(false);

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
		
		Button Map5 = (Button) findViewById(R.id.Button05);
		Map5.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map6 = (Button) findViewById(R.id.Button06);
		Map6.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map7 = (Button) findViewById(R.id.Button07);
		Map7.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map8 = (Button) findViewById(R.id.Button08);
		Map8.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map9 = (Button) findViewById(R.id.Button09);
		Map9.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});
		
		Button Map10 = (Button) findViewById(R.id.Button10);
		Map10.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
       
		Log.d("DEBUG", "Menu paused");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
        Log.d("DEBUG", "Menu resumed");
	}

}
