package ru.hoticecream.grokkingrxschedulers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class SubjectsTest {

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }
    BehaviorSubject<String> subject;
    @Test
    public void testSubjectOnNewThread() throws InterruptedException {

        TestSubscriber<String> sbr = new TestSubscriber<>(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                Logger.logThread(o);
            }
        });

        new Thread() {
            @Override
            public void run() {
                subject = BehaviorSubject.create();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subject.onNext("from new thread");
            }
        }.start();

        Thread.sleep(1000);
        subject.take(5).observeOn(Schedulers.io()).subscribe(sbr);
        subject.onNext("str");
        subject.onNext("str");
        subject.onNext("str");
        subject.onNext("str");
        sbr.awaitTerminalEvent();
    }
}
