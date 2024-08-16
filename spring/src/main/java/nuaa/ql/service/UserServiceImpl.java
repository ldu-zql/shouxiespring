package nuaa.ql.service;

import nuaa.ql.spring.ann.AutoWire;
import nuaa.ql.spring.ann.Component;
import nuaa.ql.spring.ann.Scope;
import nuaa.ql.spring.bean.BeanNameAware;
import nuaa.ql.spring.bean.InitializePropertiesSet;

@Component("userService")
public class UserServiceImpl implements UserService{

    @AutoWire
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println("原函数的逻辑");
    }


    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println(beanName);
    }


    public void afterPropertiesSet() {
        System.out.println("初始化");
    }
}
