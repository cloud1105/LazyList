# LazyList

A simple library to display images in Android. Images are being downloaded asynchronously in the background. Images are being cached on SD card and in memory. Can also be used for GridView and ListView just to display images into an ImageView or any other View, even custom ones.

<img src="http://img718.imageshack.us/img718/9149/screen1sx.png" />

Originally published <a href="http://stackoverflow.com/questions/541966/android-how-do-i-do-a-lazy-load-of-images-in-listview/3068012#3068012">here</a>.

## Basic Usage

We recommend to create your view and implements the ImageProcessingCallback there, specially for ListView and BaseAdapter. Check the ".impl" package.

``` java
ImageLoader.getInstance().init(getApplicationContext(), "MyExternalFolder");
...
MyView extends RelativeLayout implements ImageProcessingCallback {
...
//Do what you need in methods onImagePreProcessing() and onImageProcessing(Bitmap bitmap)
...
ImageLoader.getInstance().displayImage(data[position], myView);
```
Don't forget to add the following permissions to your AndroidManifest.xml:

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

Now the ImageLoader is a Singleton, so you do not need to check for only one instance. But remember to call the init() method.

Check the ".impl" package to learn how to implement the library, it is very easy.

## License

LazyList is released under the <a href="https://github.com/thest1/LazyList/blob/master/LICENSE">MIT license</a>.