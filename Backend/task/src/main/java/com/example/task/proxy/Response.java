package com.example.task.proxy;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	@NotBlank(message = "Response message should not be null.")
	private String message;

	@NotBlank(message = "Status code should not be null.")
	private String status_code;
}
