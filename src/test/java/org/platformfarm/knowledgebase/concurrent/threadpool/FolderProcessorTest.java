package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class FolderProcessorTest {

    private final String USER_HOME_DIR = "C:\\DevWorks\\Sources\\tomcat-9.0.27\\java\\javax"; //System.getProperty("user.home");

    /**
     *  ForkJoinPool 을 사용하는 class 인 FolderProcessor 를 테스트 한다.
     *
     *
     *
     */
    @Test
    public void findLogFilesByForkJoinPool() {
        ForkJoinPool pool = new ForkJoinPool();
        DirectoryTraversalTask system = new DirectoryTraversalTask(USER_HOME_DIR, "java");
        pool.submit(system);

        do {
            // Thread pool 의 상태를 모니터링 함
            System.out.print("****************************************************** \n");
            System.out.printf("병렬처리 (최대)수 : %d\n", pool.getParallelism());
            System.out.printf("활성화된 스레드 수: %d\n", pool.getActiveThreadCount());
            System.out.printf("큐내의 작업 수: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Pool 전체에서 빼앗긴 작업 수: %d\n", pool.getStealCount());
            System.out.print("****************************************************** \n");

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (!system.isDone());

        pool.shutdown();
        List<String> results;
        results = system.join();

        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
    }


    @Test
    public void findLogFilesBySingleThread() {
        SingleThreadFolderProcessor p = new SingleThreadFolderProcessor(USER_HOME_DIR, "log");
        List<String> results = p.compute();

        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
    }

    @Test
    public void findLogFilesByFixedSizeThread() {

        List<String> results = new ArrayList<String>();
        ExecutorService es = Executors.newFixedThreadPool(4);
        File file = new File("USER_HOME_DIR");
        File[] content = file.listFiles();
        Future<?> future = null;
        if (content != null) {
            for (File value : content) {
                if (value.isDirectory()) {
                    future = es.submit(() -> {
                        SingleThreadFolderProcessor p
                            = new SingleThreadFolderProcessor(value.getAbsolutePath(), "log");
                        List<String> subResults = p.compute();
                        results.addAll(subResults);
                    });
                }
                else {
                    if (checkFile(value.getName(), "log")) {
                        results.add(value.getAbsolutePath());
                    }
                }
            }
        }

        es.shutdown();

        if ( future != null ) {
            while (!future.isDone()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
    }

    private boolean checkFile(String name, String extension)
    {
        return name.endsWith(extension);
    }

}
