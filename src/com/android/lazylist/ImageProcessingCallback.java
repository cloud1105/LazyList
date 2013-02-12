package com.android.lazylist;

import android.graphics.Bitmap;


public interface ImageProcessingCallback {

	void onImagePreProcessing();
	
	void onImageProcessing(Bitmap bitmap);
}
