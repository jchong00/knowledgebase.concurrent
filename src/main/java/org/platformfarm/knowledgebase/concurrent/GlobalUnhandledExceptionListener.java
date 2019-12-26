package org.platformfarm.knowledgebase.concurrent;

public interface GlobalUnhandledExceptionListener {

    void occurredException(Thread t, Throwable e);

}
