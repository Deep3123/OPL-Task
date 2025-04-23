package com.example.task.proxy;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

	@NotBlank(message = "Email cannot be blank")
	private String email;

	@NotBlank(message = "Captcha Response cannot be blank")
	private String captchaResponse;
}
