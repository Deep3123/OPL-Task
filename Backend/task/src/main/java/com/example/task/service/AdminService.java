package com.example.task.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.task.domain.User;
import com.example.task.enums.Gender;
import com.example.task.proxy.LoginRequest;
import com.example.task.proxy.LoginResponse;
import com.example.task.proxy.ResetPassword;
import com.example.task.proxy.UserProxy;

public interface AdminService {
	public LoginResponse login(LoginRequest req);

	public List<UserProxy> getAllusers();

	public User getUserByUsername(String username);

	public String updateUser(UserProxy userProxy, String username);

	public String deleteUser(String username);

	public String forgotPassword(String email);

	public String resetPassword(String username, String timestamp, String token, ResetPassword proxy);

	public Page<UserProxy> getAllUsersPageWise(Pageable pageable);

	String registerUser(String name, String email, Date dob, String username, String password, Gender gender,
			String address, String contactNumber, String pinCode, String accessRole, MultipartFile profileImage);

//	public String generateFakeUsers();

}
