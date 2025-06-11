package wameeee.framework.web;

import wameeee.framework.anno.RequestMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class ControllerMappingManager {

    private Map<String, ControllerMapping> controllerMappings = new HashMap<String, ControllerMapping>();

    public ControllerMappingManager(String webPackage) {
        init(webPackage);
    }

    public void init(String webPackage) {
        System.out.println("ControllerMappingManager 初始化.......");
        String rootPath = this.getClass().getResource("/").getPath();
        String[] packageNames = webPackage.split("\\.");
        for (int i = 0; i < packageNames.length; i++) {
            rootPath += packageNames[i] + "/";
        }
        File file = new File(rootPath);
        File[] files = file.listFiles();
        for (File tempFile : files) {
            String className = tempFile.getName().replaceAll(".class", "");
            try {
                Class<?> clazz = Class.forName(webPackage + "." + className);
                Method[] declaredMethods = clazz.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping declaredAnnotation = declaredMethod.getDeclaredAnnotation(RequestMapping.class);
                        ControllerMapping controllerMapping = new ControllerMapping();
                        controllerMapping.setClassName(webPackage + "." + className);
                        controllerMapping.setMethodName(declaredMethod.getName());
                        controllerMappings.put(declaredAnnotation.value(), controllerMapping);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println("map=============>" + controllerMappings);
    }

    /**
     * 根据映射信息查询对应的ControllerMapping实例
     *
     * @param path
     * @return ControllerMapping
     */
    public ControllerMapping getControllerMapping(String path) {
        return this.controllerMappings.get(path);
    }

    /**
     * 判断是否存在对应的映射信息
     *
     * @param path
     * @return
     */
    public boolean containsKey(String path) {
        return this.controllerMappings.containsKey(path);
    }

    /**
     * @return 返回所有Controller 映射信息
     */
    public Map<String, ControllerMapping> getControllerMappings() {
        return this.controllerMappings;
    }
}
