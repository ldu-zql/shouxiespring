package nuaa.ql.service;

import nuaa.ql.spring.ann.Component;
import nuaa.ql.spring.bean.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class QinglinBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object beforeInit(Object bean, String beanName) {
        if(beanName.equals("userService")){
            System.out.println("这是userService的初始化方法前的调用");
        }else {
            System.out.println("其他初始化前");
        }
        return bean;
    }

    @Override
    public Object afterInit(Object bean, String beanName) {
        if(beanName.equals("userService")){
            System.out.println("这是userService的初始化方法后的调用");
            Object o = Proxy.newProxyInstance(QinglinBeanPostProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(), (proxy, method, args) -> {
                        System.out.println(method.getName() + "的代理逻辑");
                        return method.invoke(bean, args);
                    });
            return o;
        }
        return bean;
    }
}
