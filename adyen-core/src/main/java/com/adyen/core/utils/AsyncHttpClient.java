package com.adyen.core.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.internals.HttpClient;

import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Utility class for handling Asynchronous HTTP requests.
 *
 */
public final class AsyncHttpClient {

    private static final String TAG = AsyncHttpClient.class.getSimpleName();

    public static void post(@NonNull final String url, final Map<String, String> headers,
                            @NonNull final String data, @NonNull final HttpResponseCallback httpResponseCallback) {
        Log.d(TAG, "POST request for url: " + url);
        Log.d(TAG, "POST data: " + data);
        final HttpClient httpClient = new HttpClient();

        Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(@NonNull Subscriber<? super byte[]> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }
                byte[] response = null;
                try {
                    response = httpClient.post(url, headers, data);
                } catch (Exception e) {
                    // TODO: In general, catching a generic Exception is not a good practice.
                    subscriber.onError(e);
                }
                subscriber.onNext(response);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpResponseCallback.onFailure(e);
                    }

                    @Override
                    public void onNext(byte[] response) {
                        httpResponseCallback.onSuccess(response);
                    }
                });
    }

    private AsyncHttpClient() {
        // Private Constructor
    }
}
