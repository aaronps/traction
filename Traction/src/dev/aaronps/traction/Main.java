package dev.aaronps.traction;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
//		                      WindowManager.LayoutParams.FLAG_FULLSCREEN );
		
		setContentView(new GameView(this));
	}

}
