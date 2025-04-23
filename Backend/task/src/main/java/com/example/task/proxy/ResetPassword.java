package com.example.task.proxy;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPassword {
	@NotBlank(message = "Password cannot be blank.")
	private String password;

	@NotBlank(message = "Confirm Password cannot be blank.")
	private String confirmPassword;
}
