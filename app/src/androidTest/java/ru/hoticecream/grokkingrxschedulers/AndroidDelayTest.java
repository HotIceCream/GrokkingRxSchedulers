package ru.hoticecream.grokkingrxschedulers;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static ru.hoticecream.grokkingrxschedulers.Logger.logThread;

@RunWith(AndroidJUnit4.class)
public class AndroidDelayTest {

    @Test
    public void testDelay() {
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        Observable.just(1).delay(1, TimeUnit.SECONDS, Schedulers.immediate()).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        Logger.d("LastSeenThread: " + subscriber.getLastSeenThread().getName());
    }

    @Test
    public void testDelayWithZip() {
        Observable<Integer> obs = Observable.combineLatest(
                createIntDelayedObservable(1),
                createIntDelayedObservable(2),
                createIntDelayedObservable(3),
                (integer, integer2, integer3) -> integer + integer2 + integer3
        ).subscribeOn(Schedulers.io());

        TestSubscriber<Integer> subscriber = new TestSubscriber<>(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer o) {
                logThread("OnNext");
            }
        });

        long timeBefore = System.currentTimeMillis();
        obs.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        Logger.d("Time for execute = " + (System.currentTimeMillis() - timeBefore));
    }

    private Observable<Integer> createIntDelayedObservable(int id) {
        return Observable.just(id)
                .delay(1, TimeUnit.SECONDS)
                .doOnNext(i -> logThread("doOnNext" + i));
    }

}
