package com.example.task.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.task.domain.User;
import com.example.task.repo.UserRepo;

@Component
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepo repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Optional<User> user = repo.findByUsername(username);

		if (user.isEmpty())
			throw new UsernameNotFoundException("User not found with this username");

		return new CustomUserDetails(user.get());
	}

}
