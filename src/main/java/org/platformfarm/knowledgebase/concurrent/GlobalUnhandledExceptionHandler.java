package org.platformfarm.knowledgebase.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalUnhandledExceptionHandler implements UncaughtExceptionHandler {

    private List<GlobalUnhandledExceptionListener> globalUnhandledExceptionListeners
        = new CopyOnWriteArrayList<>();

    public GlobalUnhandledExceptionHandler(GlobalUnhandledExceptionListener listener) {
        this.globalUnhandledExceptionListeners.add(listener);
    }


    /**
     * Method invoked when the given thread terminates due to the given uncaught exception.
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     *
     * @param t the thread
     * @param e the exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        fireGlobalUnhandledException(t, e);
    }

    private void fireGlobalUnhandledException(Thread t, Throwable e) {
        for(GlobalUnhandledExceptionListener listener : globalUnhandledExceptionListeners) {
            listener.occurredException(t, e);
        }
    }


}
