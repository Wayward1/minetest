package net.minetest.minetest;

import android.app.NativeActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.view.View;

public class MtNativeActivity extends NativeActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		m_MessagReturnCode = -1;
		m_MessageReturnValue = "";
		// System UI flags have to be set in onCreate, because
		// onWindowFocusChanged apparently runs after the native
		// code has retreived the window dimensions, and therefore
		// everything on the screen is offset when
		// LAYOUT_HIDE_NAVIGATION is set.
		setImmersive();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void copyAssets() {
		Intent intent = new Intent(this, MinetestAssetCopy.class);
		startActivity(intent);
	}

	public void showDialog(String acceptButton, String hint, String current,
			int editType) {

		Intent intent = new Intent(this, MinetestTextEntry.class);
		Bundle params = new Bundle();
		params.putString("acceptButton", acceptButton);
		params.putString("hint", hint);
		params.putString("current", current);
		params.putInt("editType", editType);
		intent.putExtras(params);
		startActivityForResult(intent, 101);
		m_MessageReturnValue = "";
		m_MessagReturnCode   = -1;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
			setImmersive();
	}

	private void setImmersive() {
		if (Build.VERSION.SDK_INT < 16)
			return;

		// Hide status bar and other inessential UI items
		int visibility = View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		if (Build.VERSION.SDK_INT >= 19) {
			// HIDE_NAVIGATION is available since API 14, but the
			// navigation re-appears on any touch events when not
			// paired with IMMERSIVE (API 19), making it essentially
			// useless alone.
			visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		getWindow().getDecorView().setSystemUiVisibility(visibility);
	}	
	
	public static native void putMessageBoxResult(String text);

	/* ugly code to workaround putMessageBoxResult not beeing found */
	public int getDialogState() {
		return m_MessagReturnCode;
	}

	public String getDialogValue() {
		m_MessagReturnCode = -1;
		return m_MessageReturnValue;
	}

	public float getDensity() {
		return getResources().getDisplayMetrics().density;
	}

	public int getDisplayWidth() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	public int getDisplayHeight() {
		return getResources().getDisplayMetrics().heightPixels;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == 101) {
			if (resultCode == RESULT_OK) {
				String text = data.getStringExtra("text");
				m_MessagReturnCode = 0;
				m_MessageReturnValue = text;
			}
			else {
				m_MessagReturnCode = 1;
			}
		}
	}

	static {
		System.loadLibrary("openal");
		System.loadLibrary("ogg");
		System.loadLibrary("vorbis");
		System.loadLibrary("ssl");
		System.loadLibrary("crypto");
		System.loadLibrary("gmp");
		System.loadLibrary("iconv");

		// We don't have to load libminetest.so ourselves,
		// but if we do, we get nicer logcat errors when
		// loading fails.
		System.loadLibrary("minetest");
	}

	private int m_MessagReturnCode;
	private String m_MessageReturnValue;
}
