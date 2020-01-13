package org.platformfarm.knowledgebase.concurrent.threadpool;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class HowToUseForkJoinPoolTest {

    private static final String USER_HOME_DIR = System.getProperty("user.home");
    private static final String FILE_EXTENSION = ".docx";

    /**
     * 별도의 thread 생성 없이 메인 스레드에서 디렉터리를 순회하는 리커시브를 호출하여 최악의 테스트 결과를 뽑낸다.
     *
     */
    static int fileCount = 0;
    static long worstTakeTime = 0;
    @BeforeClass
    public static void setupClass() {
        Date startDate = new Date();
        SingleThreadFolderProcessor p = new SingleThreadFolderProcessor(USER_HOME_DIR, FILE_EXTENSION);
        List<String> results = p.compute();
        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
        Date endDate = new Date();
        worstTakeTime = endDate.getTime() - startDate.getTime();
        fileCount = results.size();

        System.out.println("=[BeforeClass]==========================================================");
        System.out.println(String.format("Worst take time is %fs, find file count is %d", (float)worstTakeTime /  1000
                , fileCount));
        System.out.println("========================================================================");

    }

    /**
     *  ForkJoinPool 을 사용하는 class 인 HowToUseForkJoinPool 를 테스트 한다.
     */
    @Test
    public void findFilesByForkJoinPool() {
        Date startDate = new Date();
        HowToUseForkJoinPool testObject = new HowToUseForkJoinPool();
        List<String> results = testObject.findFilesByForkJoinPool(USER_HOME_DIR, FILE_EXTENSION);
        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
        Date endDate = new Date();
        long takeTime = endDate.getTime() - startDate.getTime();
        Assert.assertTrue(takeTime < worstTakeTime);
        Assert.assertEquals(fileCount, results.size());
    }

    /**
     *  ForkJoinPool 의 비교 테스트 용 시작 디렉터리 하위에 첫 번째 자식 디렉터리를 고정 thread-pool 에 넣는다.
     *  만약 어떤 자식 폴더 하위에 무지하게 큰 폴더를 구성하고 있다면 worst case 이다.
     */
    @Test
    public void findFilesByLinearThreadPool() {
        Date startDate = new Date();
        HowToUseForkJoinPool testObject = new HowToUseForkJoinPool();
        List<String> results = testObject.findFilesByLinearThreadPool(USER_HOME_DIR, FILE_EXTENSION);
        System.out.printf("Total target extension Files: %d \n", results.size());
        for(String logFile : results) {
            System.out.printf("%s\n", logFile);
        }
        Date endDate = new Date();
        long takeTime = endDate.getTime() - startDate.getTime();
        Assert.assertTrue(takeTime < worstTakeTime);
        Assert.assertEquals(fileCount, results.size());

    }


}
