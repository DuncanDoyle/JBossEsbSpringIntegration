package org.jboss.soa.esb.spring.actions;

import org.apache.log4j.Logger;
import org.jboss.soa.esb.configure.ConfigProperty;
import org.jboss.soa.esb.configure.ConfigProperty.Use;
import org.jboss.soa.esb.lifecycle.annotation.Destroy;
import org.jboss.soa.esb.lifecycle.annotation.Initialize;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Abstract class to be extended by JBoss ESB actions that require autowiring of Spring beans into the action instance.
 * <p>
 * Actions that extend from this class should be configured with a <code>context-key</code> and optionally a
 * <code>locator-factory-selector</code>.
 * <p>
 * The <code>locator-factory-selector</code> defines the {@link BeanFactoryLocator} from which the Spring {@link BeanFactory} is obtained.
 * This defaults to <code>classpath*:beanRefContext.xml</code>.
 * <p>
 * The <code>context-key</code> defines the beanId of the {@link BeanFactory} to be loaded from the {@link BeanFactoryLocator}. This class
 * requires a {@link ApplicationContext} to be used as it needs to retrieve an {@link AutowireCapableBeanFactory}.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public abstract class AbstractAutowiredSpringAction {

	/**
	 * The logger instance.
	 */
	private final static Logger LOGGER = Logger.getLogger(AbstractAutowiredSpringAction.class);

	/**
	 * Reference to the {@link BeanFactory}. This enables us to release the reference when this action is destroyed, which enables Spring to
	 * shutdown the {@link BeanFactory} when it is no longer used by any action.
	 */
	private BeanFactoryReference beanFactoryRef;

	/**
	 * Specifies the 'selector' used in the {@link ContextSingletonBeanFactoryLocator#getInstance(String selector)} method call, which is
	 * used to obtain the BeanFactoryLocator instance from which the context is obtained.
	 * <p>
	 * The default is <code>classpath*:beanRefContext.xml</code>, matching the default applied for the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance()} method. Supplying the "contextKey" parameter is sufficient in this case.
	 */
	@ConfigProperty(name = "locator-factory-selector", use = Use.OPTIONAL)
	private String locatorFactorySelector;

	/**
	 * Specifies the 'factoryKey' used in the {@link BeanFactoryLocator#useBeanFactory(String factoryKey)} method call, obtaining the
	 * application context from the BeanFactoryLocator instance.
	 * <p>
	 * Supplying this "contextKey" parameter is sufficient when relying on the default <code>classpath*:beanRefContext.xml</code> selector
	 * for candidate factory references.
	 */
	@ConfigProperty(name = "context-key", use = Use.REQUIRED)
	private String contextKey;

	/**
	 * Retrieves a reference to the Spring {@link AutowireCapableBeanFactory} in order to have Spring autowire this JBoss ESB action.
	 * <p>
	 * Calls {@link #doDestroy()} to execute any additional initialization logic.
	 */
	@Initialize
	public final void initialize() {
		/*
		 * locatorFactorySelector is allowed to be 'null'. In that case, Spring will use the default location
		 * 'classpath*:beanRefContext.xml'.
		 */
		BeanFactoryLocator bfl = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
		beanFactoryRef = bfl.useBeanFactory(contextKey);
		BeanFactory beanFactory = beanFactoryRef.getFactory();
		// We only support ApplicationContexts, as we need to retrieve an AutowireCapableBeanFactory.
		if (beanFactory instanceof ApplicationContext) {

			if (LOGGER.isDebugEnabled()) {
				String locatorFactorySelectorLogValue = locatorFactorySelector;
				if (locatorFactorySelectorLogValue == null) {
					locatorFactorySelectorLogValue = "classpath*:beanRefContext.xml";
				}
				LOGGER.debug("Autowiring ESB action with Spring context '" + contextKey
						+ "' loaded with BeanFactoryLocator defined by the 'locator-factory-selector: '" + locatorFactorySelectorLogValue
						+ "'.");
			}
			/*
			 * We're going to autowire ourself. Although this instance is instantiated by the JBoss ESB ActionProcessingPipeline and is not
			 * defined as a Spring Bean, Spring will still autowire any field that is annotated with @Autowire.
			 */
			((ApplicationContext) beanFactory).getAutowireCapableBeanFactory().autowireBean(this);

		} else {
			throw new IllegalStateException("Only AutowireCapableBeanFactories are support.");
		}
		// Execute additional initialization logic.
		doInitialize();
	}

	/**
	 * Method to be overridden by subclass when additional initialization functionality is required.
	 */
	protected void doInitialize() {
	}

	/**
	 * Releases the reference to the Spring {@link BeanFactory}. This is required so Spring is able to destroy the {@link BeanFactory} when
	 * it is not being used anymore.
	 * <p>
	 * Calls {@link #doDestroy()} to execute any additional destroy logic.
	 */
	@Destroy
	public final void destroy() {
		beanFactoryRef.release();
		doDestroy();
	}

	/**
	 * Method to be overridden by subclass when additional destroy functionality is required.
	 */
	protected void doDestroy() {
	}
}
