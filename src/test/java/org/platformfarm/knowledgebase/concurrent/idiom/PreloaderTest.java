package org.platformfarm.knowledgebase.concurrent.idiom;

import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.idiom.Preloader.ProductInfo;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class PreloaderTest {

    @Test
    public void get() {

        Preloader preloader = new Preloader();
        long start = System.nanoTime();
        preloader.start();
        ThreadUtil.sleep(5000);
        ProductInfo productInfo = preloader.get();
        long end = System.nanoTime();
        Assert.assertNotNull(productInfo);
        long sec = TimeUnit.SECONDS.convert(end-start, TimeUnit.NANOSECONDS);
        // 위에서 preloader.start(); 호출을 하지 않으면 처리 시간이 10초에 가깝게 걸리면서 아래 검증
        // 함수가 실패로 인해 이 테스트는 실패 할 것이다.
        Assert.assertTrue(sec < 10);
        System.out.println("result >>>> " + sec);
    }
}
