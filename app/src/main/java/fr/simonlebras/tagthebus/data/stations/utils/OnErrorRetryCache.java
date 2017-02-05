package fr.simonlebras.tagthebus.data.stations.utils;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class OnErrorRetryCache<T> {
    private final AtomicReference<Observable<T>> cache = new AtomicReference<>();

    private volatile Disposable disposable;

    private final Observable<T> result;

    public OnErrorRetryCache(Observable<T> source) {
        result = Observable.defer(() -> {
            while (true) {
                final Observable<T> connection = cache.get();
                if (connection != null) {
                    return connection;
                }

                final Observable<T> next = source
                        .doOnError(e -> cache.set(null))
                        .replay(1)
                        .autoConnect(1, d -> disposable = d);

                if (cache.compareAndSet(null, next)) {
                    return next;
                }
            }
        });
    }

    public Observable<T> get() {
        return result;
    }

    public void clear() {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
