package com.fun.model;

import java.io.Serializable;

public class SessionDTO implements Serializable {

	private static final long serialVersionUID = -1727891670947001475L;

	private Long userId;

	private String username;

	public SessionDTO() {
	}

	public SessionDTO(Long userId, String email) {
		this.userId = userId;
		this.username = email;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
