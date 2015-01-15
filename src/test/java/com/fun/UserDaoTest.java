package com.fun;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fun.dao.UserDao;
import com.fun.model.User;

public class UserDaoTest extends BaseDaoTestCase {

	@Autowired
	private UserDao dao;
	
	@Test
	public void testFemaleUsers(){
		List<User> users = dao.findByGenderGroupBy("F", "age", 0);
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		User user1 = users.get(0);
		User user2 = users.get(1);
		System.out.println(user1.getAge());
		System.out.println(user2.getAge());
		Assert.assertTrue(user1.getAge() < user2.getAge());
	}
	
	@Test
	public void testMaleUsers(){
		List<User> users = dao.findByGenderGroupBy("M", "age", 0);
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
	}
}
