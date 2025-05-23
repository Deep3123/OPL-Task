package com.example.task.domain;

import java.time.LocalDate;
import java.util.Date;

import com.example.task.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Date dob;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	@Column(nullable = false)
	private String address;

	@Column(name = "profile_image")
	private String profileImage;

	@Column(name = "contact_number", nullable = false)
	private String contactNumber;

	@Column(name = "pin_code", nullable = false)
	private String pinCode;

	@Column(name = "access_role", nullable = false)
	private String accessRole;
}
