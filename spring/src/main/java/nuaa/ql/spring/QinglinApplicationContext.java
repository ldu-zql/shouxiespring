package nuaa.ql.spring;

import nuaa.ql.spring.ann.Component;
import nuaa.ql.spring.ann.ComponentScan;
import nuaa.ql.spring.ann.Scope;
import nuaa.ql.spring.bean.BeanDefinition;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QinglinApplicationContext {
    private Map<String,Object> sigontonObj=new ConcurrentHashMap<>();
    private Map<String,Object> beanDefin=new ConcurrentHashMap<>();
    private Class aClass;

    public QinglinApplicationContext(Class aClass) {
        this.aClass = aClass;
        //componentScan--扫描包
        scan(aClass);
        //根据beanDefinition创建单例bean
        for(String beanName:beanDefin.keySet()){
            BeanDefinition beanDefinition = (BeanDefinition) beanDefin.get(beanName);
            if(beanDefinition.getScope().equals("sigonton")){
                try {
                    sigontonObj.put(beanName,createBean(beanDefinition.getaClass()));
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
