package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class HowToUseForkJoinPool {

    public List<String> findFilesByForkJoinPool(String rootDirectory, String fileExtension) {
        ForkJoinPool pool = new ForkJoinPool();
        DirectoryTraversalTask system = new DirectoryTraversalTask(rootDirectory, fileExtension);
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
        return results;
    }

    public List<String> findFilesByLinearThreadPool(String rootDirectory, String fileExtension) {
        List<String> results = new ArrayList<String>();
        ExecutorService es = Executors.newFixedThreadPool(4);
        File file = new File(rootDirectory);
        File[] content = file.listFiles();
        CopyOnWriteArrayList<Future<?>> futures = new CopyOnWriteArrayList<>();

        if (content != null) {
            for (File value : content) {
                if (value.isDirectory()) {
                    Future<?> future = es.submit(() -> {
                        SingleThreadFolderProcessor p
                                = new SingleThreadFolderProcessor(value.getAbsolutePath(), fileExtension);
                        List<String> subResults = p.compute();
                        results.addAll(subResults);
                    });
                    futures.add(future);
                }
                else {
                    if (checkFile(value.getName(), fileExtension)) {
                        results.add(value.getAbsolutePath());
                    }
                }
            }
        }

        es.shutdown();

        while (!allFuturesIsDone(futures)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return results;
    }


    private boolean allFuturesIsDone(CopyOnWriteArrayList<Future<?>> futures) {
        for(Future<?> f : futures) {
            if (!f.isDone()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkFile(String name, String extension)
    {
        return name.endsWith(extension);
    }

}
