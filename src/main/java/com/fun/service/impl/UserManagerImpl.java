package com.fun.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fun.dao.UserDao;
import com.fun.model.SessionDTO;
import com.fun.model.User;
import com.fun.service.UserManager;


@Transactional(rollbackFor=Exception.class, value = "funTransactionManager")
@Service
public class UserManagerImpl extends GenericManagerImpl<User, Long> implements UserManager {
    private PasswordEncoder passwordEncoder;
    private UserDao userDao;
    
    @Autowired
    @Qualifier("passwordEncoder")
    public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Autowired
    public void setUserDao(final UserDao userDao) {
        this.dao = userDao;
        this.userDao = userDao;
    }

    /**
     * {@inheritDoc}
     *
     * @param username the login name of the human
     * @return User the populated user object
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException thrown when username not found
     */
    @Override
    public User getUserByUsername(final String username) throws UsernameNotFoundException {
        return (User) userDao.loadUserByUsername(username);
    }

    @Override
    public List<User> findByGenderGroupByAge(String gender, String groupBy, int start){
    	return userDao.findByGenderGroupBy(gender, groupBy, start);
    }
    
	@Override
	public SessionDTO authenticate(String username, String password) {
		
		User user = getUserByUsername(username);
		
		System.out.println("password " + password);
		System.out.println("user.password " + user.getPassword());
		
		// if passwords do not match, throw Invalid Credential Exception
		if (user == null || passwordEncoder.matches(password, user.getPassword()) == false) {
			return null;
		}
		
		return new SessionDTO( user.getId(),  user.getUsername());
	}

}
