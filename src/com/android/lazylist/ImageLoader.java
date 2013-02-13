package com.android.lazylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;


public class ImageLoader {

	private MemoryCache memoryCache = new MemoryCache();
	private FileCache fileCache;
	private Map<ImageProcessingCallback, String> callbacks = Collections.synchronizedMap(new WeakHashMap<ImageProcessingCallback, String>());
	private ExecutorService executorService;
	private Handler handler = new Handler();// handler to display images in UI thread

	private ImageLoader() { }
	 
    private static class SingletonHolder { 
            public static final ImageLoader instance = new ImageLoader();
    }

    public static ImageLoader getInstance() {
            return SingletonHolder.instance;
    }

    public void init(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}
    
    public void init(Context context, int numberOfThreads) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
    
	public void init(Context context, String directory) {
		fileCache = new FileCache(context, directory);
		executorService = Executors.newFixedThreadPool(5);
	}
	
	
	public void init(Context context, String directory, int numberOfThreads) {
		fileCache = new FileCache(context, directory);
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
    
	
	public void displayImage(String url, ImageProcessingCallback imageProcessingCallback) {
		imageProcessingCallback.onImagePreProcessing();
		callbacks.put(imageProcessingCallback, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			imageProcessingCallback.onImageProcessing(bitmap);
		}else {
			queuePhoto(url, imageProcessingCallback);
		}
	}

	private void queuePhoto(String url, ImageProcessingCallback imageProcessingCallback) {
		PhotoToLoad p = new PhotoToLoad(url, imageProcessingCallback);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
				return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

//			 decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = 1;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageProcessingCallback imageProcessingCallback;
		
		public PhotoToLoad(String u, ImageProcessingCallback i) {
			url = u;
			imageProcessingCallback = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (viewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				memoryCache.put(photoToLoad.url, bmp);
				if (viewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean viewReused(PhotoToLoad photoToLoad) {
		String tag = callbacks.get(photoToLoad.imageProcessingCallback);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (viewReused(photoToLoad))
				return;
			if (bitmap != null) {
				photoToLoad.imageProcessingCallback.onImageProcessing(bitmap);
			}
		}
		
		public void recycleBitmap() {
			if(!bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
	}
	


	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}
	

	public void clearMemoryCache() {
		memoryCache.clear();
	}
	

	public void clearFileCache() {
		fileCache.clear();
	}


	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

}
