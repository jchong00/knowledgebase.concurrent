package org.platformfarm.knowledgebase.concurrent.basic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class JavaSourceAnalyzer {

    @Test
    public void findJavaFilesByForkJoinPool() {
        ForkJoinPool pool = new ForkJoinPool();
        FolderProcessor system = new FolderProcessor("C:\\DevWorks\\Sources\\tomcat-9.0.27\\java\\javax", "java");
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

        //Map<String, String> map = new HashMap<>();

        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String filePath : results) {
            System.out.printf("%s, %s\n ", filePath, getFileType(filePath));
        }
    }

    private String getFileType(String filePath) {

        return findStringInFile(new File(filePath));
    }

    public String findStringInFile(File f) {

        String justName = f.getName();
        justName = justName.replace(".java", "");

        String result = "";
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(f));
            while(in.hasNextLine()) {

                String codeLine = in.nextLine();

                if (codeLine.contains("abstract class " + justName)) {
                    result = "AC";
                    break;
                } else if (codeLine.contains("class "+ justName)) {
                    result = "C";
                    break;
                } else if (codeLine.contains("@interface "+ justName)) {
                    result = "A";
                    break;
                } else if (codeLine.contains("interface "+ justName)) {
                    result = "I";
                    break;
                }

            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {

            try {
                in.close() ;
            } catch(Exception e) { /* ignore */ }
        }
        return result;
    }

}
