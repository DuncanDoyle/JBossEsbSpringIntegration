package org.jboss.soa.esb.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JBoss ESB ContextLoader based on Spring's Web <code>ContextLoaderListener</code> and <code>ContextLoader</code>.
 * <p>
 * One configures the <code>contextConfigLocation</code> and optionally the <code>locatorFactorySelector</code> and
 * <code>parentContextKey</code> to load a parent context.
 * <p>
 * Contains JBoss MBean lifecycle methods {@link #create()}, {@link #start()}, {@link #stop()} and {@link #destroy()}.
 * 
 * 
 * @author <a href='duncan.doyle@redhat.com'>Duncan Doyle</a>
 * 
 */
public class EsbContextLoader {

	/**
	 * The Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(EsbContextLoader.class);

	/**
	 * BeanRefContext resource that contains the {@link BeanFactory} Spring bean definition, from which we load the parent context.
	 */
	private String locatorFactorySelector;

	/**
	 * Bean-id of the parent context.
	 */
	private String parentContextKey;

	/**
	 * Location of the <code>ApplicationContext</code> file to be loaded.
	 */
	private String contextConfigLocation;

	/**
	 * Holds BeanFactoryReference when loading parent factory via ContextSingletonBeanFactoryLocator.
	 */
	private BeanFactoryReference parentContextRef;

	/**
	 * Map from (thread context) ClassLoader to corresponding 'current' WebApplicationContext.
	 * 
	 * TODO: This is how the Web ContextLoader keeps track of application contexts and how a webresource can retrieve its application
	 * context. Can we use the same mechanism in the ESB? This only works when the thread's Context Classloader that starts this instance is
	 * different for each ESB archive. We can probably not guarantee that in this context. So, how can we make sure that we can retrieve the
	 * correct context?
	 * 
	 * @param context
	 * @return
	 */
	private static final Map<ClassLoader, ApplicationContext> currentContextPerThread = new ConcurrentHashMap<ClassLoader, ApplicationContext>(1);

	public EsbContextLoader() {
		ApplicationContext parentContext = loadParentContext();
		// Need a refernce to a refreshable app context.
		AbstractXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
		appContext.setConfigLocation(contextConfigLocation);
		appContext.setParent(parentContext);
		// Reload the Application Context.
		appContext.refresh();
		// Application Context has now been loaded.
		/*
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		if (ccl == EsbContextLoader.class.getClassLoader()) {
			currentContext = this.context;
		}
		else if (ccl != null) {
			currentContextPerThread.put(ccl, this.context);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Published root WebApplicationContext as ServletContext attribute with name [" +
					WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
		}
		if (LOGGER.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			LOGGER.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
		}
		*/
		

	}

	/**
	 * Template method with default implementation (which may be overridden by a subclass), to load or obtain an ApplicationContext instance
	 * which will be used as the parent context of the root WebApplicationContext. If the return value from the method is null, no parent
	 * context is set.
	 * <p>
	 * The main reason to load a parent context here is to allow multiple root web application contexts to all be children of a shared EAR
	 * context, or alternately to also share the same parent context that is visible to EJBs. For pure web applications, there is usually no
	 * need to worry about having a parent context to the root web application context.
	 * <p>
	 * The default implementation uses {@link org.springframework.context.access.ContextSingletonBeanFactoryLocator}, configured via
	 * {@link #LOCATOR_FACTORY_SELECTOR_PARAM} and {@link #LOCATOR_FACTORY_KEY_PARAM}, to load a parent context which will be shared by all
	 * other users of ContextsingletonBeanFactoryLocator which also use the same configuration parameters.
	 * 
	 * @return the parent application context, or <code>null</code> if none
	 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
	 */
	protected ApplicationContext loadParentContext() {
		ApplicationContext parentContext = null;

		if (parentContextKey != null) {
			// locatorFactorySelector may be null, indicating the default "classpath*:beanRefContext.xml"
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Getting parent context definition: using parent context key of '" + parentContextKey
						+ "' with BeanFactoryLocator");
			}
			this.parentContextRef = locator.useBeanFactory(parentContextKey);
			parentContext = (ApplicationContext) this.parentContextRef.getFactory();
		}

		return parentContext;
	}

	/*
	 * Methods create, start, stop and destroy integrate with the JB
	 */

	public void create() throws Exception {
	}

	public void start() {
	}

	public void stop() {
	}

	public void destroy() {

	}
}
