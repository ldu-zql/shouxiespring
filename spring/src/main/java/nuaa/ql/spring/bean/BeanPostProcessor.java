package nuaa.ql.spring.bean;

public interface BeanPostProcessor {
    public Object beforeInit(Object bean,String beanName);
    public Object afterInit(Object bean,String beanName);
}
