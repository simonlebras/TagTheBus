package fr.simonlebras.tagthebus.data.stations.utils;

import android.support.v4.util.Pair;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class RetryPolicy implements Function<Observable<? extends Throwable>, Observable<?>> {
    private static final int UNCHECKED_ERROR_TYPE_CODE = -100;

    private final int initialDelay;
    private final TimeUnit delayUnit;
    private final int retryCount;
    private final List<Class<? extends Throwable>> errorTypes;

    public RetryPolicy(int initialDelay, TimeUnit delayUnit, int retryCount, List<Class<? extends Throwable>> errorsTypes) {
        this.initialDelay = initialDelay;
        this.delayUnit = delayUnit;
        this.retryCount = retryCount;
        this.errorTypes = errorsTypes;
    }

    @Override
    public Observable<?> apply(Observable<? extends Throwable> errors) throws Exception {
        return errors
                .zipWith(Observable.range(1, retryCount + 1), (error, retryAttempt) -> {
                    if (retryAttempt == retryCount + 1) {
                        return new Pair<>(error, UNCHECKED_ERROR_TYPE_CODE);
                    }

                    if (errorTypes != null) {
                        for (int i = 0, size = errorTypes.size(); i < size; i++) {
                            if (errorTypes.get(i).isInstance(error)) {
                                return new Pair<>(error, retryAttempt);
                            }
                        }
                    }

                    return new Pair<>(error, UNCHECKED_ERROR_TYPE_CODE);
                })
                .flatMap(pair -> {
                    final int retryAttempt = pair.second;

                    if (retryAttempt == UNCHECKED_ERROR_TYPE_CODE) {
                        return Observable.error(pair.first);
                    }

                    final long delay = (long) Math.pow(initialDelay, retryAttempt);
                    return Observable.timer(delay, delayUnit);
                });
    }
}
