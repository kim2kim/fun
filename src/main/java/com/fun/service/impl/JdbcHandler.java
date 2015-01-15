package com.fun.service.impl;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fun.service.IJdbcHandler;
import com.mysql.jdbc.Connection;


public class JdbcHandler implements IJdbcHandler {
	
	private String driver;
	private String url;
	private String username;
	private String password;
	
	public JdbcHandler(){}

	@Autowired
	public JdbcHandler(String driver, String url, String username, String password){
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public boolean isAlive(){
		try {
			Class.forName(driver);
			Connection connection = (Connection) DriverManager.getConnection(url,username,password);
			if(connection == null){
				return false;
			}else{
				connection.close();
				return true;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
