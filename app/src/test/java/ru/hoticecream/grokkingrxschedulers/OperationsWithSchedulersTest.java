package ru.hoticecream.grokkingrxschedulers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observers.TestSubscriber;

import static ru.hoticecream.grokkingrxschedulers.Logger.logThread;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class OperationsWithSchedulersTest {


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testTake() {
        Observable<String> obs = Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        logThread("Inside observable");
                        subscriber.onNext("asdf");
                        subscriber.onCompleted();
                    }
                })
                .take(1000, TimeUnit.MILLISECONDS)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        logThread("In doOnNext");
                    }
                });

        TestSubscriber<String> subscriber = new TestSubscriber<>(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                logThread("In onComplete");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                logThread("In onNext");
            }
        });
        obs.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
    }
}
