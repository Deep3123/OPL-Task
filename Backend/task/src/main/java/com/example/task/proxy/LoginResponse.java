package com.example.task.proxy;

import java.util.Date;

import com.example.task.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	private String username;
	private String token;
	private String role;
	private String name;
	private String email;
	private Date dob;
	private String address;
	private String contactNumber;
	private String pinCode;
	private Gender gender;
	private String password;
	private String profileImage;
}
