package com.fun;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration( 
        locations = {"classpath*:**/applicationContext-resources.xml",
                "classpath*:**/applicationContext-dao.xml",
                "classpath*:**/applicationContext-service.xml"})
public abstract class BaseDaoTestCase extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired  
    private SessionFactory sessionFactory;
   
    /** 
     * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
     */
    protected final Log log = LogFactory.getLog(getClass());


    /**
     * Create a HibernateTemplate from the SessionFactory and call flush() and clear() on it.
     * Designed to be used after "save" methods in tests: http://issues.appfuse.org/browse/APF-178.
     *
     * @throws org.springframework.beans.BeansException
     *          when can't find 'sessionFactory' bean
     */
    protected void flush() throws BeansException {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.flush();
    }

}
