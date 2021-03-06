package com.geekcommune.integ;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Use this to interact with multiple instances of a Java app that behave more or less as if they're
 * in their own VM.
 * 
 * Wraps a separate classloader (with the same classpath as it's constructed in).  This lets you
 * instantiate singletons etc. within the classloader proxy that don't interact with one another.
 * 
 * @see com.geekcommune.friendlybackup.integ.TestNode for an example
 * @author bobbym
 */
public class ClassloaderProxy {

    protected URLClassLoader cl;

    public ClassloaderProxy() {
        super();

        cl = new URLClassLoader(
                filterOut(((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs(), "log4j"),
                ClassLoader.getSystemClassLoader().getParent()
            );
    }

    private URL[] filterOut(URL[] urLs, String string) {
    	List<URL> retval = new ArrayList<URL>();
    	for(URL url : urLs) {
    		if(!url.toString().contains(string)) {
    			retval.add(url);
    		}
    	}
    	
		return retval.toArray(new URL[retval.size()]);
	}

	protected Object invokeMethod(Object targetObject, String methodName, String[] argumentClassNames,
            Object[] arguments) throws Exception {
                return invokeMethod(targetObject.getClass().getName(), targetObject, methodName, argumentClassNames, arguments);
            }

    protected Object invokeMethod(Object targetObject, String methodName, Class<?>[] argumentClasses,
            Object[] arguments) throws Exception {
                return invokeMethod(targetObject.getClass().getName(), targetObject, methodName, argumentClasses, arguments);
            }

    protected Object invokeStaticMethod(String targetClassName, String methodName, String[] argumentClassNames,
            Object[] arguments) throws Exception {
                return invokeMethod(targetClassName, null, methodName, argumentClassNames, arguments);
            }

    protected Object invokeStaticMethod(String targetClassName, String methodName, Class<?>[] argumentClasses,
            Object[] arguments) throws Exception {
                return invokeMethod(targetClassName, null, methodName, argumentClasses, arguments);
            }

    protected Object invokeMethod(String targetClassName, Object targetObject, String methodName, String[] argumentClassNames,
            Object[] arguments) throws Exception {
                return invokeMethod(targetClassName, targetObject, methodName, classesForClassnames(argumentClassNames), arguments);
            }

    protected Object invokeMethod(String targetClassName, Object targetObject, String methodName, Class<?>[] argClasses,
            Object[] arguments) throws Exception {
                Class<?> targetClass = cl.loadClass(targetClassName);
                
                Method m = targetClass.getMethod(methodName, argClasses);
                return m.invoke(targetObject, arguments);
            }

    protected Object invokeConstructor(String targetClassName, String[] argumentClassNames, Object[] arguments)
            throws Exception {
                Class<?> targetClass = cl.loadClass(targetClassName);
                
                Class<?>[] argClasses = classesForClassnames(argumentClassNames);
                
                Constructor<?> m = targetClass.getConstructor(argClasses);
                return m.newInstance(arguments);
            }

    private Class<?>[] classesForClassnames(String[] argumentClassNames)
            throws ClassNotFoundException {
                Class<?> argClasses[] = new Class<?>[argumentClassNames.length];
                for(int i = 0; i < argumentClassNames.length; ++i) {
                    argClasses[i] = cl.loadClass(argumentClassNames[i]);
                }
                return argClasses;
            }

}