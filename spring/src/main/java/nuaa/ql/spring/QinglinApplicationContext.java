package nuaa.ql.spring;

import nuaa.ql.spring.ann.AutoWire;
import nuaa.ql.spring.ann.Component;
import nuaa.ql.spring.ann.ComponentScan;
import nuaa.ql.spring.ann.Scope;
import nuaa.ql.spring.bean.BeanDefinition;
import nuaa.ql.spring.bean.BeanNameAware;
import nuaa.ql.spring.bean.BeanPostProcessor;
import nuaa.ql.spring.bean.InitializePropertiesSet;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QinglinApplicationContext {
    private Map<String,Object> sigontonObj=new ConcurrentHashMap<>();
    private Map<String,Object> beanDefin=new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList=new ArrayList<>();
    private Class aClass;

    public QinglinApplicationContext(Class aClass) {
        this.aClass = aClass;
        //componentScan--扫描包
        scan(aClass);
        //根据beanDefinition创建单例bean
        createSigonton();
        //依赖注入
        fieldProcess();


    }

    private void fieldProcess() {
        Set<String> beanNames = sigontonObj.keySet();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = (BeanDefinition) beanDefin.get(beanName);
            Class aClass = beanDefinition.getaClass();
            Object bean = sigontonObj.get(beanName);
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {

                //依赖注入
                if(field.isAnnotationPresent(AutoWire.class)){
                    AutoWire autoWire = field.getDeclaredAnnotation(AutoWire.class);
                    String name = autoWire.value();
                    if("".equals(name)){
                        name = field.getName();
                    }
                    Object autoWireBean = getBean(name);
                    field.setAccessible(true);
                    try {
                        field.set(bean,autoWireBean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            //回调
            if(bean instanceof BeanNameAware){
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.beforeInit(bean,beanName);
            }
            //初始化
            if(bean instanceof InitializePropertiesSet){
                ((InitializePropertiesSet) bean).afterPropertiesSet();
            }
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                Object obj = beanPostProcessor.afterInit(bean, beanName);
                sigontonObj.put(beanName,obj);
            }
        }
    }

    private void createSigonton(){
        for(String beanName:beanDefin.keySet()){
            BeanDefinition beanDefinition = (BeanDefinition) beanDefin.get(beanName);
            if(beanDefinition.getScope().equals("sigonton")){
                try {
                    Object bean = createBean(beanDefinition.getaClass());
                    if(bean instanceof BeanPostProcessor){
                        beanPostProcessorList.add((BeanPostProcessor) bean);
                    }
                    sigontonObj.put(beanName,bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Object createBean(Class aClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return aClass.getDeclaredConstructor().newInstance();
    }
    public Object getBean(String beanName){
        BeanDefinition beanDefinition = (BeanDefinition) beanDefin.get(beanName);
        String scope = beanDefinition.getScope();
        if(scope.equals("sigonton")){
            return sigontonObj.get(beanName);
        }
        try {
            return createBean(beanDefinition.getaClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void scan(Class aClass){
        ComponentScan componentScan = (ComponentScan) aClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScan.value();
        //扫描
        String resourcePath = path.replace(".","/");
        ClassLoader classLoader = QinglinApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(resourcePath);
        File file =new File(resource.getFile());
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                String fp = f.getAbsolutePath();
                if (fp.endsWith(".class")) {
                    String cpname = fp.substring(fp.indexOf("nuaa"), fp.indexOf(".")).replace("\\", ".");
                    try {
                        Class<?> ac = classLoader.loadClass(cpname);
                        if (ac.isAnnotationPresent(Component.class)) {
                            Component component = ac.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();
                            if("".equals(beanName)){
                                beanName = ac.getSimpleName();
                                beanName = beanName.replace(beanName.charAt(0), (char) (beanName.charAt(0)-'A'+'a'));
                            }
                            //解析bean
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setaClass(ac);
                            if(ac.isAnnotationPresent(Scope.class)) {
                                Scope scope = ac.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());
                            }else{
                                beanDefinition.setScope("sigonton");
                            }
                            beanDefin.put(beanName,beanDefinition);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
