package org.platformfarm.knowledgebase.concurrent.basic;

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

    @Test
    public void findLogFilesByForkJoinPool() {
        ForkJoinPool pool = new ForkJoinPool();
        FolderProcessor system = new FolderProcessor("C:\\", "log");
        pool.submit(system);

        do {
            System.out.printf("******************************************\n");
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
            System.out.printf("******************************************\n");
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
        SingleThreadFolderProcessor p = new SingleThreadFolderProcessor("C:\\", "log");
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
        File file = new File("C:\\");
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
