package com.example.task.proxy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Date;

import com.example.task.enums.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProxy {
	private Long id;

	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be in the past")
	private Date dob;

	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Username is required")
	@Column(unique = true)
	private String username;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;

	@NotNull(message = "Gender is required")
	@Enumerated(EnumType.STRING)
	private Gender gender;

	@NotBlank(message = "Address is required")
	private String address;

	@Lob
	@Column(name = "profile_image", columnDefinition = "BLOB")
	private String profileImage;

	@NotBlank(message = "Contact number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
	private String contactNumber;

	@NotBlank(message = "Pin code is required")
	@Pattern(regexp = "^[0-9]{5,}$", message = "Pin code must be at least 5 digits")
	private String pinCode;

	@NotBlank(message = "Access role is required")
	private String accessRole;
}