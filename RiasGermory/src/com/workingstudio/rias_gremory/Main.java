package com.workingstudio.rias_gremory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.SurfaceHolder;

public class Main extends GLWallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MainEngine();
	}

	public class MainEngine extends GLEngine implements Renderer {

		private int mTextureDataHandle;

		private String fragment;

		private int mProgram;

		private float[] mImgSize = new float[2];

		private final String vertexShaderCode = "precision mediump float;"
				+ "attribute vec4 vPosition;" + "void main() {"
				+ "gl_Position = vPosition;" + "}";

		private final float squareCoords[] = { -1f, -1f, 0.0f, -1f, 1f, 0.0f,
				1f, 1f, 0.0f, 1f, -1f, 0.0f };

		private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

		private final FloatBuffer vertexBuffer;
		private final ShortBuffer indicesBuffer;

		private int mPositionHandle;

		private int mTimeHandle;

		private int mOffsetHandle;

		private long startTime;

		private float time;

		private float offset;

		private int mTextureUniformHandle;

		public MainEngine() {
			fragment = RawResourceReader.readTextFileFromRawResource(
					getApplicationContext(), R.raw.fragment);

			ByteBuffer buffer = ByteBuffer
					.allocateDirect(squareCoords.length * 4);
			buffer.order(ByteOrder.nativeOrder());
			vertexBuffer = buffer.asFloatBuffer();
			vertexBuffer.put(squareCoords);
			vertexBuffer.position(0);

			buffer = ByteBuffer.allocateDirect(drawOrder.length * 2);
			buffer.order(ByteOrder.nativeOrder());
			indicesBuffer = buffer.asShortBuffer();
			indicesBuffer.put(drawOrder);
			indicesBuffer.position(0);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			setEGLContextClientVersion(2);
			setRenderer(this);

		}

		@Override
		public void onDrawFrame(GL10 gl) {
			time = (float) (((System.currentTimeMillis() - startTime) * 0.001));
			GLES20.glUniform1f(mTimeHandle, time);
			GLES20.glUniform1f(mOffsetHandle, offset);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
					GLES20.GL_UNSIGNED_SHORT, indicesBuffer);
			GLES20.glUniform1i(mTextureUniformHandle, 0);
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			GLES20.glUniform2fv(
					GLES20.glGetUniformLocation(mProgram, "resolution"), 1,
					new float[] { width, height }, 0);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			mProgram = GLES20.glCreateProgram();

			int vertexShader = GLES20Utilty.loadShader(GLES20.GL_VERTEX_SHADER,
					vertexShaderCode);
			int fragmentShader = GLES20Utilty.loadShader(
					GLES20.GL_FRAGMENT_SHADER, fragment);

			mTextureDataHandle = GLES20Utilty.loadTexture(
					getApplicationContext(),
					com.workingstudio.rias_gremory.R.drawable.image);

			Drawable d = getResources().getDrawable(R.drawable.image);
			mImgSize[0] = d.getIntrinsicWidth();
			mImgSize[1] = d.getIntrinsicWidth();

			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);
			GLES20.glLinkProgram(mProgram);
			GLES20.glUseProgram(mProgram);
			GLES20.glUniform2fv(
					GLES20.glGetUniformLocation(mProgram, "img_size"), 1,
					mImgSize, 0);
			mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
			mTimeHandle = GLES20.glGetUniformLocation(mProgram, "time");
			mOffsetHandle = GLES20.glGetUniformLocation(mProgram, "offset");
			mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram,
					"u_Texture");
			GLES20.glEnableVertexAttribArray(mPositionHandle);
			GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
					false, 12, vertexBuffer);

			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

			startTime = System.currentTimeMillis();

			if (isPreview())
				offset = 0.5f;
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			this.offset = xOffset;
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
		}
	}
}

class GLES20Utilty {
	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == GLES20.GL_FALSE)
			Log.i("GLES20Utility", "Shader Didn't Compile");
		return shader;
	}

	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("GLES20Utilty", glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	public static int loadTexture(final Context context, final int resourceId) {
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), resourceId, options);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}
}
