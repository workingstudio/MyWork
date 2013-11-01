package com.workingstudio.rias_gremory;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;



public abstract class GLWallpaperService extends WallpaperService {

	public class GLEngine extends Engine {
		class WallpaperGLSurfaceView extends GLSurfaceView {
			private static final String TAG = "WallpaperGLSurfaceView";
			private final boolean debug = false;

			WallpaperGLSurfaceView(Context context) {
				super(context);
			}

			@Override
			public SurfaceHolder getHolder() {
				if (debug) {
					Log.d(TAG, "getHolder(): returning " + getSurfaceHolder());
				}

				return getSurfaceHolder();
			}

			public void onDestroy() {
				if (debug) {
					Log.d(TAG, "onDestroy()");
				}

				super.onDetachedFromWindow();
			}
		}

		private static final String TAG = "GLEngine";

		private WallpaperGLSurfaceView glSurfaceView;
		private boolean rendererHasBeenSet;		
		private final boolean debug = false;
		
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			if (debug) {
				Log.d(TAG, "onCreate(" + surfaceHolder + ")");
			}

			super.onCreate(surfaceHolder);

			glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (debug) {
				Log.d(TAG, "onVisibilityChanged(" + visible + ")");
			}

			super.onVisibilityChanged(visible);

			if (rendererHasBeenSet) {
				if (visible) {
					glSurfaceView.onResume();
				} else {
					if (!isPreview()) {
						glSurfaceView.onPause();
					}
				}
			}
		}		

		@Override
		public void onDestroy() {
			if (debug) {
				Log.d(TAG, "onDestroy()");
			}

			super.onDestroy();
			glSurfaceView.onDestroy();
		}
		
		protected void setRenderer(Renderer renderer) {
			if (debug) {
				Log.d(TAG, "setRenderer(" + renderer + ")");
			}

			glSurfaceView.setRenderer(renderer);
			rendererHasBeenSet = true;
		}

		protected void setEGLContextClientVersion(int version) {
			if (debug) {
				Log.d(TAG, "setEGLContextClientVersion(" + version + ")");
			}

			glSurfaceView.setEGLContextClientVersion(version);
		}
	}
}
