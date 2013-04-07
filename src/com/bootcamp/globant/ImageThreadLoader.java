package com.bootcamp.globant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageThreadLoader {

   //the simplest in-memory cache implementation. This should be replaced with something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
   /** The cache. */
   private HashMap<String, Bitmap> cache=new HashMap<String, Bitmap>();

   /** The cache dir. */
   private File cacheDir;

   /**
    * Instantiates a new image loader.
    *
    * @param context the context
    */
   public ImageThreadLoader(Context context){
       //Make the background thead low priority. This way it will not affect the UI performance
       photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);

       //Find the dir to save cached images
       if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
           cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"cache_dir_img");
       else
           cacheDir=context.getCacheDir();
       if(!cacheDir.exists())
           cacheDir.mkdirs();
   }
   //This is used for a stub when the user can not see the actual image..
   //this images will be seen
   final int stub_id =R.drawable.ic_launcher;


   /**
    * Display image.
    *
    * @param url the url
    * @param activity the activity
    * @param imageView the image view
    */
   public void displayImage(String url, Activity activity, ImageView imageView)
   {
       if(cache.containsKey(url))
           imageView.setImageBitmap(cache.get(url));
       else
       {
           queuePhoto(url, activity, imageView);
           imageView.setImageResource(stub_id);
       }    
   }

   /**
    * Queue photo.
    *
    * @param url the url
    * @param activity the activity
    * @param imageView the image view
    */
   private void queuePhoto(String url, Activity activity, ImageView imageView)
   {
       //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
       photosQueue.Clean(imageView);
       PhotoToLoad p=new PhotoToLoad(url, imageView);
       synchronized(photosQueue.photosToLoad){
           photosQueue.photosToLoad.push(p);
           photosQueue.photosToLoad.notifyAll();
       }

       //start thread if it's not started yet
       if(photoLoaderThread.getState()==Thread.State.NEW)
           photoLoaderThread.start();
   }

   /**
    * Gets the bitmap.
    *
    * @param url the url
    * @return the bitmap
    */
   private Bitmap getBitmap(String url) 
   {
       //I identify images by hashcode. Not a perfect solution, good for the demo.
       String filename=String.valueOf(url.hashCode());
       File f=new File(cacheDir, filename);

       //from SD cache
       Bitmap b = decodeFile(f);
       if(b!=null)
           return b;

       //from web
       try {
           Bitmap bitmap=null;
           InputStream is=new URL(url).openStream();
           OutputStream os = new FileOutputStream(f);
           copyStream(is, os);
           os.close();
           bitmap = decodeFile(f);
           return bitmap;
       } catch (Exception ex){
          ex.printStackTrace();
          return null;
       }
   }

   //decodes image and scales it to reduce memory consumption
   /**
    * Decode file.
    *
    * @param f the f
    * @return the bitmap
    */
   private Bitmap decodeFile(File f){
       try {
           //decode image size
           BitmapFactory.Options o = new BitmapFactory.Options();
           o.inJustDecodeBounds = true;
           BitmapFactory.decodeStream(new FileInputStream(f),null,o);

           //Find the correct scale value. It should be the power of 2.
           final int REQUIRED_SIZE=70;
           int width_tmp=o.outWidth, height_tmp=o.outHeight;
           int scale=1;
           while(true){
               if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                   break;
               width_tmp/=2;
               height_tmp/=2;
               scale++;
           }

           //decode with inSampleSize
           BitmapFactory.Options o2 = new BitmapFactory.Options();
           o2.inSampleSize=scale;
           return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
       } catch (FileNotFoundException e) {}
       return null;
   }

   //Task for the queue
   /**
    * The Class PhotoToLoad.
    */
   private class PhotoToLoad
   {

       /** The url. */
       public String url;

       /** The image view. */
       public ImageView imageView;

       /**
        * Instantiates a new photo to load.
        *
        * @param u the u
        * @param i the i
        */
       public PhotoToLoad(String u, ImageView i){
           url=u; 
           imageView=i;
       }
   }

   /** The photos queue. */
   PhotosQueue photosQueue=new PhotosQueue();

   /**
    * Stop thread.
    */
   public void stopThread()
   {
       photoLoaderThread.interrupt();
   }

   //stores list of photos to download
   /**
    * The Class PhotosQueue.
    */
   class PhotosQueue
   {

       /** The photos to load. */
       private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();

       //removes all instances of this ImageView
       /**
        * Clean.
        *
        * @param image the image
        */
       public void Clean(ImageView image)
       {
           for(int j=0 ;j<photosToLoad.size();){
               if(photosToLoad.get(j).imageView==image)
                   photosToLoad.remove(j);
               else
                   ++j;
           }
       }
   }

   /**
    * The Class PhotosLoader.
    */
   class PhotosLoader extends Thread {

       /* (non-Javadoc)
        * @see java.lang.Thread#run()
        */
       public void run() {
           try {
               while(true)
               {
                   //thread waits until there are any images to load in the queue
                   if(photosQueue.photosToLoad.size()==0)
                       synchronized(photosQueue.photosToLoad){
                           photosQueue.photosToLoad.wait();
                       }
                   if(photosQueue.photosToLoad.size()!=0)
                   {
                       PhotoToLoad photoToLoad;
                       synchronized(photosQueue.photosToLoad){
                           photoToLoad=photosQueue.photosToLoad.pop();
                       }
                       Bitmap bmp=getBitmap(photoToLoad.url);
                       cache.put(photoToLoad.url, bmp);
                       if(((String)photoToLoad.imageView.getTag()).equals(photoToLoad.url)){
                           BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad.imageView);
                           Activity a=(Activity)photoToLoad.imageView.getContext();
                           a.runOnUiThread(bd);
                       }
                   }
                   if(Thread.interrupted())
                       break;
               }
           } catch (InterruptedException e) {
               //allow thread to exit
           }
       }
   }

   /** The photo loader thread. */
   PhotosLoader photoLoaderThread=new PhotosLoader();

   //Used to display bitmap in the UI thread
   /**
    * The Class BitmapDisplayer.
    */
   class BitmapDisplayer implements Runnable
   {

       /** The bitmap. */
       Bitmap bitmap;

       /** The image view. */
       ImageView imageView;

       /**
        * Instantiates a new bitmap displayer.
        *
        * @param b the b
        * @param i the i
        */
       public BitmapDisplayer(Bitmap b, ImageView i){bitmap=b;imageView=i;}

       /* (non-Javadoc)
        * @see java.lang.Runnable#run()
        */
       public void run()
       {
           if(bitmap!=null)
               imageView.setImageBitmap(bitmap);
           else
             imageView.setImageResource(stub_id);
       }
   }

   /**
    * Clear cache.
    */
   public void clearCache() {
       //clear memory cache
       cache.clear();

       //clear SD cache
       File[] files=cacheDir.listFiles();
       for(File f:files)
           f.delete();
   }

    public static void copyStream(InputStream is, OutputStream os) {
           final int buffer_size=1024;
           try
           {
               byte[] bytes=new byte[buffer_size];
               for(;;)
               {
                 int count=is.read(bytes, 0, buffer_size);
                 if(count==-1)
                     break;
                 os.write(bytes, 0, count);
               }
           }
           catch(Exception ex){}
       }


}