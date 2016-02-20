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
import rx.functions.Action1;
import rx.functions.Func3;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static ru.hoticecream.grokkingrxschedulers.Logger.logThread;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class CombineLatestsTest {


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testWithoutSchedulers() {
        template(stringObservable -> stringObservable, null, null, null);
    }

    @Test
    public void testSubscribeOnAndObserveOn() {
        template(stringObservable -> stringObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread()), null, null, null);
    }

    @Test
    public void testParallel() {
        Observable.Transformer<String, String> ioTransformer = stringObservable -> stringObservable.subscribeOn(Schedulers.io());
        template(stringObservable -> stringObservable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation()), ioTransformer, ioTransformer, ioTransformer);
    }

    private void template(Observable.Transformer<String, String> transformer,
                          Observable.Transformer<String, String> firstObsTransformer,
                          Observable.Transformer<String, String> secondObsTransformer,
                          Observable.Transformer<String, String> thirdObsTransformer) {
        Observable<String> obs = Observable.combineLatest(createObservable("Observable1", firstObsTransformer),
                createObservable("Observable2", secondObsTransformer),
                createObservable("Observable3", thirdObsTransformer),
                (s, s2, s3) -> {
                    logThread("Inside combining result");
                    return s + s2 + s3;
                })
                .doOnNext(s -> logThread("Before transform"))
                .compose(transformer)
                .doOnNext(s -> logThread("After tranform"));

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

    private Observable<String> createObservable(final String name, Observable.Transformer<String, String> transformer) {
        Observable<String> result = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                logThread("Inside " + name);
                sleep(1000);
                subscriber.onNext(name);
                subscriber.onCompleted();
            }
        });

        if (transformer != null) {
            return result.compose(transformer);
        }
        return result;
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
