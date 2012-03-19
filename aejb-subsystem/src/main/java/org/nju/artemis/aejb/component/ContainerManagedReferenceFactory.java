package org.nju.artemis.aejb.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.ValueManagedReference;
import org.jboss.logging.Logger;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.Value;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class ContainerManagedReferenceFactory implements ManagedReferenceFactory {

	Logger log = Logger.getLogger(ContainerManagedReferenceFactory.class);

	private final String appName;
    private final String moduleName;
    private final String distinctName;
    private final String beanName;
    private final String viewClass;
    private final boolean stateful;
    private final Value<ClassLoader> viewClassLoader;
    private final AcContainer container;
    
    public ContainerManagedReferenceFactory(final String appName, final String moduleName, final String distinctName, final String beanName, final String viewClass, boolean stateful, AcContainer container) {
        this(appName, moduleName, distinctName, beanName, viewClass, null, stateful, container);
    }
    
    public ContainerManagedReferenceFactory(final String appName, final String moduleName, final String distinctName, final String beanName, final String viewClass, final Value<ClassLoader> viewClassLoader, boolean stateful, AcContainer container) {
        this.appName = appName == null ? "" : appName;
        this.moduleName = moduleName;
        this.distinctName = distinctName;
        this.beanName = beanName;
        this.viewClass = viewClass;
        this.viewClassLoader = viewClassLoader;
        this.stateful = stateful;
        this.container = container;
        this.container.addOriginDepndencies(moduleName + '/' + beanName);
    }
    
	@Override
	public ManagedReference getReference() {
		Class<?> viewClass;
		try {
            viewClass = Class.forName(this.viewClass, false, getContextClassLoader());
        } catch (ClassNotFoundException e) {
            if(viewClassLoader == null) {
                throw new RuntimeException("Could not load view class for aejb " + beanName);
            }
            try {
                viewClass = Class.forName(this.viewClass, false, viewClassLoader.getValue());
            } catch (ClassNotFoundException ce) {
                throw new RuntimeException("Could not load view class for aejb " + beanName);
            }
        }
		
		Class<?> proxyClass = Proxy.getProxyClass(viewClass.getClassLoader(), viewClass).asSubclass(viewClass);
		Constructor<?> proxyConstructor = null;
		Object proxy = null;
		
		try {
            proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError("No valid constructor found on proxy class");
        }
		
		try {
			proxy = proxyConstructor.newInstance(new ContainerInvocationHandler(appName, moduleName, distinctName, beanName, viewClass, stateful, container));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return new ValueManagedReference(new ImmediateValue<Object>(proxy));
	}

	/**
     * Gets context classloader.
     *
     * @return the current context classloader
     */
    public static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        }
    }
}
