package org.platformfarm.knowledgebase.concurrent;

import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.OuterClass.StaticInnerClass;

public class InnerClassTest {

    @Test
    public void createStaticInnerClass () {
        OuterClass.StaticInnerClass staticInnerClass = new StaticInnerClass();
        staticInnerClass.method();
    }

    @Test
    public void createInnerClass () {
        //안되는 일이다. 컴파일도 안된다.
        //OuterClass.InnerClass innerClass = new OuterClass.InnerClass();
        //먼저 외부객체를 생산해야 한다.
        OuterClass outer = new OuterClass();
        //정말 이상하게 이런 문법을 써서 생성한다. 인스턴스 없이 생성할 수 없다.
        OuterClass.InnerClass innerClass = outer.new InnerClass();
        innerClass.method();
    }
}

class OuterClass {

    static class StaticInnerClass {
        void method() {
        }
    }

    int classField = 0;

    class InnerClass {
        void method() {
        }
    }

    void outerMethod() {
        final int methodLocalVar = 0;
        class methodInnerClass {
            void method() {
                OuterClass.this.classField = 222;
                int test =  methodLocalVar;
                //methodLocalVar = 1; // 이건 않됨 !!!
            }
        }
    }
}
