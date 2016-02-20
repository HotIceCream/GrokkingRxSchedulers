package ru.hoticecream.grokkingrxschedulers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static ru.hoticecream.grokkingrxschedulers.Logger.logThread;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class SimpleSchedulersTest {


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }


    @Test
    public void testObserveOn() {
        testSchedulersTemplate(stringObservable -> stringObservable.observeOn(Schedulers.io()));
    }

    @Test
    public void testWithoutSchedulers() {
        testSchedulersTemplate(stringObservable -> stringObservable);
    }

    @Test
    public void testSubscribeOn() {
        testSchedulersTemplate(stringObservable -> stringObservable.subscribeOn(Schedulers.io()));
    }

    @Test
    public void testObserveOnAndSubscribeOn() {
        testSchedulersTemplate(stringObservable -> stringObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io()));
    }

    @Test
    public void testMultipleObserveOn() {
        testSchedulersTemplate(stringObservable -> stringObservable
                .observeOn(Schedulers.computation())
                .doOnNext(str -> logThread("Between two observeOn"))
                .observeOn(Schedulers.io()));
    }

    @Test
    public void testMultipleSubsribeOn() {
        testSchedulersTemplate(stringObservable -> stringObservable
                .subscribeOn(Schedulers.computation())
                .doOnNext(str -> logThread("Between two observeOn"))
                .lift((Observable.Operator<String, String>) subscriber -> {
                    logThread("Inside lift");
                    return subscriber;
                })
                .subscribeOn(Schedulers.io()));
    }

    @Test
    public void testWithFlatMap() {
        testSchedulersTemplate(stringObservable -> stringObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .lift(new Observable.Operator<String, String>() {
                    @Override
                    public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
                        logThread("Inside lift");
                        return subscriber;
                    }
                })
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    logThread("Inside flatMap");
                    return Observable
                            .create(new Observable.OnSubscribe<String>() {
                                @Override
                                public void call(Subscriber<? super String> subscriber) {
                                    logThread("Inside observable in flatMap");
                                    subscriber.onNext(s);
                                    subscriber.onCompleted();
                                }
                            })
                            .observeOn(Schedulers.io())
                            .subscribeOn(Schedulers.newThread());
                }).subscribeOn(AndroidSchedulers.mainThread()));
    }

    private Func1<String, String> getThreadPrintFunc(final String prefix) {
        return s -> {
            logThread(prefix);
            return s;
        };
    }

    private void testSchedulersTemplate(Observable.Transformer<String, String> transformer) {
        Observable<String> obs = Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        logThread("Inside observable");
                        subscriber.onNext("Hello from observable");
                        subscriber.onCompleted();
                    }
                })
                .map(getThreadPrintFunc("Before transform"))
                .compose(transformer)
                .map(getThreadPrintFunc("After transform"))
                .doOnNext(s -> logThread("Inside doOnNext"));
        TestSubscriber<String> subscriber = new TestSubscriber<>(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                getThreadPrintFunc("In onComplete").call("");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                getThreadPrintFunc("In onNext").call("");
            }
        });
        obs.subscribe(subscriber);
        subscriber.awaitTerminalEvent();
    }
}
