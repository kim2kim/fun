package com.fun.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.code.ssm.api.CacheKeyMethod;

@Entity
@Table(name = "users")
public class User extends BaseObject {
	private static final long serialVersionUID = 3832626162173359411L;

	private Long id;
	private String username; // required
	private String password; // required
	private String gender;
	private Integer age;
	private Integer version;

	/**
	 * Default constructor - creates a new instance with no values set.
	 */
	public User() {
	}

	public User(Long id) {
		this.id = id;
	}

	/**
	 * Create a new instance and set the username.
	 * 
	 * @param username
	 *            login name for user.
	 */
	public User(final String username) {
		this.username = username;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@CacheKeyMethod
	public Long getId() {
		return id;
	}

	@Column(name = "age")
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Column(name = "gender")
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Column(name = "username", nullable = false, length = 50, unique = true)
	public String getUsername() {
		return username;
	}

	@Column(name = "password", nullable = false)
	public String getPassword() {
		return password;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof User)) {
			return false;
		}

		final User user = (User) o;

		return !(username != null ? !username.equals(user.getUsername()) : user
				.getUsername() != null);

	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return (username != null ? username.hashCode() : 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		ToStringBuilder sb = new ToStringBuilder(this,
				ToStringStyle.DEFAULT_STYLE).append("username", this.username);
		return sb.toString();
	}
}
