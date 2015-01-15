package com.fun.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fun.dao.UserDao;
import com.fun.model.SessionDTO;
import com.fun.model.User;

public interface UserManager extends GenericManager<User, Long> {
	
	SessionDTO authenticate(String username, String password);
	
	public User getUserByUsername(final String username) throws UsernameNotFoundException;
	
    /**
     * Convenience method for testing - allows you to mock the DAO and set it on an interface.
     * @param userDao the UserDao implementation to use
     */
    void setUserDao(UserDao userDao);

    /**
     * Convenience method for testing - allows you to mock the PasswordEncoder and set it on an interface.
     * @param passwordEncoder the PasswordEncoder implementation to use
     */
    void setPasswordEncoder(PasswordEncoder passwordEncoder);
    
    public List<User> findByGenderGroupByAge(String gender, String groupBy, int start);

}
