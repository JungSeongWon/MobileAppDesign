package com.scjeon.costom;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by USER on 2018-05-09.
 */

public class HttpVolley {
    private static HttpVolley volley;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private final LruCache<String,Bitmap> cache = new LruCache<String,Bitmap>(20);

    private HttpVolley(final Context context){
        requestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });

    }

    public static HttpVolley getInstance(Context context){
        if(volley == null){
            volley = new HttpVolley(context);
        }
        return volley;
    }

    public RequestQueue getRequestQueue(){
        return requestQueue;
    }

    public ImageLoader getImageLoader(){
        return imageLoader;
    }
}
