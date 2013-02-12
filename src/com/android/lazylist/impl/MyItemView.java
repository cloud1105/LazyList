package com.android.lazylist.impl;

import com.android.lazylist.ImageProcessingCallback;
import com.android.lazylist.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyItemView extends LinearLayout implements ImageProcessingCallback {

	private TextView textView;
	private ImageView imageView;
	private int position;
	
	public MyItemView(Context context) {
		super(context);
		init();
	}
	
	public MyItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	


	private void init() {
		inflate(getContext(), R.layout.item, this);
		textView = (TextView) findViewById(R.id.text);
		imageView = (ImageView) findViewById(R.id.image);
		
		
	}
	
	public void setPosition(int pos) {
		this.position = pos;
	}

	@Override
	public void onImagePreProcessing() {
		textView.setText(getContext().getString(R.string.loading_image));
		imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.wait));
		this.invalidate();
	}

	@Override
	public void onImageProcessing(Bitmap bitmap) {
		textView.setText(getContext().getString(R.string.item, position));
		imageView.setImageBitmap(bitmap);
		this.invalidate();
	}
	
	
}
