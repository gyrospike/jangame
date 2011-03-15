package org.alchemicstudio;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;

public class Menu extends Activity {

	LayerDrawable layers = null;

	Drawable[] drawA = new Drawable[3];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//sets badges on level icons, yeah, this took a long time to get right
		Resources res = this.getResources();
		Drawable blueBadgeDrawable = res.getDrawable(R.drawable.blue_badge);
		Drawable redBadgeDrawable = res.getDrawable(R.drawable.red_badge);
		
		LayerDrawable layerDrawable = (LayerDrawable) res.getDrawable(R.layout.level01_button);
		layerDrawable.setDrawableByLayerId(R.id.red_badge_id, redBadgeDrawable);
		layerDrawable.setDrawableByLayerId(R.id.blue_badge_id, blueBadgeDrawable);

		setContentView(R.layout.main);

		HorizontalScrollView sView1 = (HorizontalScrollView) findViewById(R.id.ScrollView01);
		// Hide the Scollbar
		sView1.setVerticalScrollBarEnabled(false);
		sView1.setHorizontalScrollBarEnabled(false);

		HorizontalScrollView sView2 = (HorizontalScrollView) findViewById(R.id.ScrollView02);
		sView2.setVerticalScrollBarEnabled(false);
		sView2.setHorizontalScrollBarEnabled(false);

		HorizontalScrollView sView3 = (HorizontalScrollView) findViewById(R.id.ScrollView03);
		sView3.setVerticalScrollBarEnabled(false);
		sView3.setHorizontalScrollBarEnabled(false);


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
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});

		Button Map10 = (Button) findViewById(R.id.Button10);
		Map10.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});

		Button Map11 = (Button) findViewById(R.id.Button11);
		Map11.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map2);
				startActivity(StartGameIntent);
			}
		});

		Button Map12 = (Button) findViewById(R.id.Button12);
		Map12.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
				StartGameIntent.putExtra("mapNumber", R.raw.map1);
				startActivity(StartGameIntent);
			}
		});
	}

	public class CustomDrawableView extends View {
		private ShapeDrawable mDrawable;

		public CustomDrawableView(Context context) {
			super(context);
			mDrawable = new ShapeDrawable(new OvalShape());
			mDrawable.getPaint().setColor(0xff74AC23);
			mDrawable.setBounds(0, 0, 100, 100);
		}

		protected void onDraw(Canvas canvas) {
			mDrawable.draw(canvas);
		}
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
