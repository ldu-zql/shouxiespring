package nuaa.ql;

import nuaa.ql.spring.QinglinApplicationContext;
import nuaa.ql.spring.config.AppConfig;

public class Test {
    public static void main(String[] args) {
        QinglinApplicationContext qinglinApplicationContext = new QinglinApplicationContext(AppConfig.class);

        System.out.println(qinglinApplicationContext.getBean("userService"));
        System.out.println(qinglinApplicationContext.getBean("userService"));
        System.out.println(qinglinApplicationContext.getBean("userService"));
    }
}
