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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class FlatMapTest {


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testFlatMap() {
        Observable<String> obs = createIntGenerator().take(3)
                .flatMap(id -> createStringGenerator(id).take(5))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
        TestSubscriber<String> subs = new TestSubscriber<>(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                Logger.logThread("onNext " + o);
            }
        });
        obs.subscribe(subs);

    }


    private Observable<Integer> createIntGenerator() {
        return Observable.interval(1, TimeUnit.SECONDS)
                .map(Long::intValue)
                .doOnNext(integer -> Logger.logThread("int Observable"));
    }

    private Observable<String> createStringGenerator(int id) {
        return Observable.interval(400, TimeUnit.MILLISECONDS)
                .map(l -> "Message " + l + "x" + id)
                .doOnNext(integer -> Logger.logThread("str Observable"));
    }
}
