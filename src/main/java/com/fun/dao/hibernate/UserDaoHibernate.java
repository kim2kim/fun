package com.fun.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.fun.dao.UserDao;
import com.fun.model.User;
import com.fun.service.ISpyMemcachedHandler;
import com.google.code.ssm.api.InvalidateSingleCache;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;

@Repository("userDao")
public class UserDaoHibernate extends GenericDaoHibernate<User, Long> implements UserDao {

	@Autowired
	private ISpyMemcachedHandler memcachedHandler;

	/**
	 * Constructor that sets the entity to User.class.
	 */
	public UserDaoHibernate() {
		super(User.class);
	}

	@Autowired
	@Required
	public void setSessionFactory(@Qualifier("funSessionFactory") final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		Session session = getHibernateTemplate().getSessionFactory()
				.getCurrentSession();
		Query qry = session
				.createQuery("from User u order by upper(u.username)");
		return qry.list();
	}

	/**
	 * {@inheritDoc}
	 */
	public User saveUser(User user) {
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		if (log.isDebugEnabled()) {
			log.debug("user's id: " + user.getId());
		}
		session.saveOrUpdate(user);
		// necessary to throw a DataIntegrityViolation and catch it in
		// UserManager
		session.flush();
		return user;
	}

	/**
	 * Overridden simply to call the saveUser method. This is happening because
	 * saveUser flushes the session and saveObject of BaseDaoHibernate does not.
	 * 
	 * @param user
	 *            the user to save
	 * @return the modified user (with a primary key set if they're new)
	 */
	@Override
	@InvalidateSingleCache(namespace = "User")
	public User save(User b) {
		return this.saveUser(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@ReadThroughSingleCache(namespace = "User", expiration = 3600)
	public User loadUserByUsername(
			@ParameterValueKeyProvider String username)
			throws UsernameNotFoundException {
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		List users = session.createCriteria(User.class).add(Restrictions.eq("username", username)).list();
		if (users == null || users.isEmpty()) {
			throw new UsernameNotFoundException("user '" + username
					+ "' not found...");
		} else {
			return (User)users.get(0);
		}
	}

	@Override
	@ReadThroughSingleCache(namespace = "User", expiration = 3600)
	public User get(@ParameterValueKeyProvider Long id) {
		return super.get(id);
	}

	@Override
	@InvalidateSingleCache(namespace = "User")
	public void remove(Long id) {
		super.remove(id);
	}

	@Override
	@InvalidateSingleCache(namespace = "User")
	public void remove(User obj) {
		super.remove(obj);
	}

	@Override
	public List<User> findByGenderGroupBy(String gender, String groupBy, int start) {
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Criteria criteria = session.createCriteria(User.class).add(Restrictions.eq("gender", gender));
		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty(groupBy).as("age"))
				.add(Projections.property("id").as("id"))
				.add(Projections.property("username").as("username"))
				.add(Projections.property("gender").as("gender"))
				.add(Projections.property("version").as("version"))
				.add(Projections.property("password").as("password")));
		criteria.setResultTransformer(Transformers.aliasToBean(User.class));
		criteria.setMaxResults(start + 50);
		criteria.setFirstResult(start * 50);
		return criteria.list();
	}

}
