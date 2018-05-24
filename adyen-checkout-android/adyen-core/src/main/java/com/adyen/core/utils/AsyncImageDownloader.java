package com.adyen.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.ImageView;

import com.adyen.core.internals.TLSSocketFactory;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Utility class for downloading images and assigning them to ImageViews.
 * This class uses {@link LruCache} and the shared preferences for caching images in order to increase
 * responsiveness of UI. It is possible to specify a fallback image in case the given URL is invalid.
 * This class is intended to be used with payment method icons only as images are cached.
 */
public final class AsyncImageDownloader {
    private static final String TAG = AsyncImageDownloader.class.getSimpleName();
    private static final int MAX_NUMBER_OF_IMAGES_TO_BE_CACHED = 50;
    @NonNull private static final LruCache<String, Bitmap> BITMAP_LRU_CACHE
            = new LruCache<>(MAX_NUMBER_OF_IMAGES_TO_BE_CACHED);

    private static final SSLSocketFactory SSL_SOCKET_FACTORY;

    static {
        try {
            SSL_SOCKET_FACTORY = new TLSSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public interface ImageListener {
        void onImage(Bitmap bitmap, String url);
    }
    /**
     * Downloads the image from the given url and sets in the given ImageView.
     * If the given URL is not valid, it uses the fallback image (if specified)
     *
     * @param imageView The imageView where the bitmap will be set.
     * @param url The URL for downloading the image.
     * @param fallbackImage The fallback image for the scenario where the given URL is invalid.
     */
    public static void downloadImage(final Context context, @NonNull final ImageView imageView,
                                     @NonNull final String url, @Nullable final Bitmap fallbackImage) {
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
                              @Override
                              public void subscribe(@NonNull ObservableEmitter<Bitmap> subscriber) {
                                  if (subscriber.isDisposed()) {
                                      return;
                                  }
                                  Bitmap icon = retrieveImage(context, url, fallbackImage);
                                  if (icon != null) {
                                      subscriber.onNext(icon);
                                  }
                                  subscriber.onComplete();
                              }
                          }

        ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                               @Override
                               public void onSubscribe(@io.reactivex.annotations.NonNull final Disposable d) {
                                   // Do nothing
                               }

                               @Override
                               public void onComplete() {
                                   // Do nothing
                               }

                               @Override
                               public void onError(Throwable e) {
                                   // Do nothing
                               }

                               @Override
                               public void onNext(Bitmap response) {
                                   imageView.setImageBitmap(response);
                               }
                           }

                );
    }

    //TODO: Following method is copy pasted from above. Reuse the code and refactor!
    /**
     * Downloads the image from the given url and calls back the given listener with the bitmap.
     * If the given URL is not valid, it uses the fallback image (if specified)
     *
     * @param imageListener Callback listener
     * @param url The URL for downloading the image.
     * @param fallbackImage The fallback image for the scenario where the given URL is invalid.
     */
    public static void downloadImage(final Context context, @NonNull final ImageListener imageListener,
                                     @NonNull final String url, @Nullable final Bitmap fallbackImage) {
        Observable.create(new ObservableOnSubscribe<Pair<Bitmap, String>>() {
                              @Override
                              public void subscribe(@NonNull ObservableEmitter<Pair<Bitmap, String>> subscriber) {
                                  if (subscriber.isDisposed()) {
                                      return;
                                  }
                                  Bitmap icon = retrieveImage(context, url, fallbackImage);
                                  if (icon != null) {
                                      subscriber.onNext(new Pair<>(icon, url));
                                  }
                                  subscriber.onComplete();
                              }
                          }

        ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<Bitmap, String>>() {

                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull final Disposable d) {
                        // Do nothing
                    }

                    @Override
                    public void onComplete() {
                        // Do nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Do nothing
                    }

                    @Override
                    public void onNext(Pair<Bitmap, String> bitmapStringPair) {
                        imageListener.onImage(bitmapStringPair.first, bitmapStringPair.second);
                    }
                });
    }

    /**
     * Method to retrieve an image from a url.
     * This method will first try to get the image from the cache (in static variable).
     * If the image does not exist in the cache it will retrieve it from the sharedpreferences instead.
     * If it also does not exist there, an http request will be issued to get the image over the web.
     * @param context Application context
     * @param url Url of the icon to be loaded
     * @param fallbackImage fallback in case no image is found at the given url
     * @return Bitmap found at the given url
     */
    @Nullable
    private static Bitmap retrieveImage(Context context, String url, Bitmap fallbackImage) {
        Bitmap icon = BITMAP_LRU_CACHE.get(url);
        if (icon == null) {
            icon = IconStorage.getIcon(context, url);
            if (icon == null) {
                icon = downloadImage(context, url, fallbackImage);
            }
        }
        return icon;
    }

    /**
     * This method issues an http request to retrieve the image at the given url.
     * If successful, the image will be saved in the local cache (static variable) and
     * also in the sharedpreferences.
     * @param context Application context
     * @param url Url of the icon to be loaded
     * @param fallbackImage fallback in case no image is found at the given url
     * @return Bitmap found at the given url
     */
    private static Bitmap downloadImage(Context context, String url, Bitmap fallbackImage) {
        Bitmap icon = null;
        HttpsURLConnection urlConnection = null;
        try {
            Log.d(TAG, "Downloading image from: " + url);
            urlConnection = (HttpsURLConnection) new URL(url).openConnection();
            urlConnection.setSSLSocketFactory(SSL_SOCKET_FACTORY);
            urlConnection.connect();

            icon = BitmapFactory.decodeStream(urlConnection.getInputStream());
            BITMAP_LRU_CACHE.put(url, icon);
            IconStorage.storeIcon(context, icon, url);
        } catch (@NonNull final FileNotFoundException fileNotFoundException) {
            Log.v(TAG, "This URL is invalid: " + url);
            if (fallbackImage == null) {
                Log.d(TAG, "Create an empty bitmap for this URL in the cache");
                icon = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            } else {
                Log.d(TAG, "Use default bitmap for this URL");
                icon = fallbackImage;
            }
            BITMAP_LRU_CACHE.put(url, icon);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return icon;
    }

    private AsyncImageDownloader() {
        // Private constructor
    }
}
