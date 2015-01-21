package com.runze.yourheroes.net;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.runze.yourheroes.R;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Eloi Jr on 02/01/2015.
 */
public class ImageLoader {

    private final String LOG_TAG = ImageLoader.class.getSimpleName();

    private HashMap<String, Bitmap> imageMap = new HashMap<String, Bitmap>();
    private File cacheDir;
    private ImageQueue imageQueue = new ImageQueue();
    private Thread imageLoaderThread = new Thread(new ImageQueueManager());

    public ImageLoader(Context context) {

        imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            cacheDir = new File(sdDir, "data/images");
        } else
            cacheDir = context.getCacheDir();

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

    }

    public void displayImage(String url, ImageView imageView, ProgressBar progress) {
        if ((imageMap.containsKey(url)) || (url.equals(""))) {
            imageView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
            if (url.equals("")) { // if person does not have a image
                imageView.setImageResource(R.drawable.image_not_available);
            } else {
                imageView.setImageBitmap(imageMap.get(url));
            }
        } else {
            imageView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            queueImage(url, imageView, progress);
            //imageView.setImageResource(null);
        }
    }

    private void queueImage(String url, ImageView imageView, ProgressBar progress) {
        // Clear the queue before starting
        imageQueue.clean(imageView);

        ImageRef p = new ImageRef(url, imageView, progress);

        synchronized (imageQueue.imageRefs) {
            imageQueue.imageRefs.push(p);
            imageQueue.imageRefs.notifyAll();
        }

        if (imageLoaderThread.getState() == Thread.State.NEW)
            imageLoaderThread.start();
    }

    public Bitmap getBitmap(String url) {
        String fileName = String.valueOf(url.hashCode());
        File f = new File(cacheDir, fileName);

        // Bitmap in cache!
        Bitmap bitMap = BitmapFactory.decodeFile(f.getPath());
        if (bitMap != null) {
            Log.d(LOG_TAG, "Pegando do cache "+f.getPath());
            return bitMap;
        }

        // Bitmap is not in cache!
        Log.d(LOG_TAG, "Buscando na net "+f.getPath());
        Log.d(LOG_TAG, "URL: "+url.toString());
        try {
            bitMap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());

            // save to cache for later use
            writeFile(bitMap, f);

            return bitMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeFile(Bitmap bmp, File f) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {}
        }
    }

    private class ImageRef {

        public String url;
        public ImageView imageView;
        public ProgressBar progress;

        public ImageRef(String url, ImageView imageView, ProgressBar progress) {
            this.url = url;
            this.imageView = imageView;
            this.progress = progress;
        }

    }

    private class ImageQueue {
        private Stack<ImageRef> imageRefs = new Stack<ImageRef>();

        public void clean(ImageView imageView) {
            for (int i = 0; i < imageRefs.size();) {
                if (imageRefs.get(i).imageView == imageView)
                    imageRefs.remove(i);
                else
                    ++i;
            }
        }

    }

    private class ImageQueueManager implements Runnable {

        @Override
        public void run() {
            try {
                while(true) {
                    if (imageQueue.imageRefs.size() == 0) {
                        synchronized (imageQueue.imageRefs) {
                            imageQueue.imageRefs.wait();
                        }
                    }

                    if (imageQueue.imageRefs.size() != 0) {
                        ImageRef imageToLoad;

                        synchronized (imageQueue.imageRefs) {
                            imageToLoad = imageQueue.imageRefs.pop();
                        }
                        Bitmap bmp = getBitmap(imageToLoad.url);
                        imageMap.put(imageToLoad.url, bmp);

                        Object tag = imageToLoad.imageView.getTag();
                        if (tag != null && ((String) tag).equals(imageToLoad.url)) {
                            BitmapDisplayer bitmapDisplayer = new BitmapDisplayer(bmp, imageToLoad.imageView, imageToLoad.progress);
                            Activity activity = (Activity) imageToLoad.imageView.getContext();
                            activity.runOnUiThread(bitmapDisplayer);
                        }

                    }

                    if (Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {

            }
        }
    }

    private class BitmapDisplayer implements Runnable {

        Bitmap bitmap;
        ImageView imageView;
        ProgressBar progress;

        public BitmapDisplayer(Bitmap bitmap, ImageView imageView, ProgressBar progress) {
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.progress = progress;
        }

        @Override
        public void run() {
            if (bitmap != null) {
                imageView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            }
        }
    }
}
