package com.adyen.core.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.internals.HttpClient;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

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

        Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<byte[]> subscriber) {
                if (subscriber.isDisposed()) {
                    return;
                }
                byte[] response = null;
                try {
                    response = httpClient.post(url, headers, data);
                } catch (Exception e) {
                    // TODO: In general, catching a generic Exception is not a good practice.
                    subscriber.onError(e);
                }
                if (response != null) {
                    subscriber.onNext(response);
                }
                subscriber.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<byte[]>() {

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
