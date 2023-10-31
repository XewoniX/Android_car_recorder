/*
    BSD 3-Clause License
    Copyright (c) Wojciech Kuźbiński <wojkuzb@mat.umk.pl>, 2023

    See https://aleks-2.mat.umk.pl/pz2022/zesp10/#/project-info for see license text.
*/

package pl.umk.mat.odyn.cam;

import android.graphics.Bitmap;

/**
 * Jest to klasa zawierająca informacje z kamery.
 */
public class CamInfo {
	private float FOV = 0;
	private float width = 0;
	private float height = 0;
	private Bitmap BMP = null;

	public CamInfo(float FOV, float width, float height, Bitmap BMP) {
		this.FOV = FOV;
		this.width = width;
		this.height = height;
		this.BMP = BMP;
	}
	public CamInfo(float FOV, float width, float height) {
		this.FOV = FOV;
		this.width = width;
		this.height = height;
	}

	/**
	 * Jest to metoda odpowiedzialna za otrzymywanie informacji o polu widzenia.
	 */
	public float getFOV() {
		return FOV;
	}

	/**
	 * Jest to metoda odpowiedzialna za otrzymywanie informacji o szerokości.
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Jest to metoda odpowiedzialna za otrzymywanie informacji o wysokości.
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Jest to metoda odpowiedzialna za otrzymywanie informacji o wymiarach.
	 */
	public float[] getDimensions() {
		return new float[]{width, height};
	}

	/**
	 * Jest to metoda odpowiedzialna za otrzymywanie informacji o bitmapie.
	 */
	public Bitmap getBMP() {
		return BMP;
	}
}
