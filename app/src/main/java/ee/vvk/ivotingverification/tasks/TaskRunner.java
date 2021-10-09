
package ee.vvk.ivotingverification.tasks;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ee.vvk.ivotingverification.util.Util;

public class TaskRunner {

    private static final String TAG = TaskRunner.class.getSimpleName();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Executor executor = Executors.newCachedThreadPool();


    public <R> void executeAsync(CustomCallable<R> callable) {
        try {
            callable.setUiForLoading();
            executor.execute(new RunnableTask<>(handler, callable));
        } catch (Exception e) {
            Util.logException(TAG, e);
        }
    }

    public static class RunnableTask<R> implements Runnable{
        private final Handler handler;
        private final CustomCallable<R> callable;

        public RunnableTask(Handler handler, CustomCallable<R> callable) {
            this.handler = handler;
            this.callable = callable;
        }

        @Override
        public void run() {

            R result;
            try {
                result = callable.call();
            } catch (Exception e) {
                result = null;
                Util.logException(TAG, e);
            }

            try {
                handler.post(new RunnableTaskForHandler<>(callable, result));
            } catch (Exception e) {
                Util.logException(TAG, e);
            }
        }
    }

    public static class RunnableTaskForHandler<R> implements Runnable{

        final private CustomCallable<R> callable;
        final private R result;

        public RunnableTaskForHandler(CustomCallable<R> callable, R result) {
            this.callable = callable;
            this.result = result;
        }

        @Override
        public void run() {
            callable.setDataAfterLoading(result);
        }
    }
}
