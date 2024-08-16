package nuaa.ql;

import nuaa.ql.service.UserService;
import nuaa.ql.service.UserServiceImpl;
import nuaa.ql.spring.QinglinApplicationContext;
import nuaa.ql.spring.config.AppConfig;

public class Test {
    public static void main(String[] args) {
        QinglinApplicationContext qinglinApplicationContext = new QinglinApplicationContext(AppConfig.class);

        UserService userService = (UserService) qinglinApplicationContext.getBean("userService");
        userService.test();
    }
}
