package com.example.task.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cage.Cage;
import com.github.cage.GCage;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
public class CaptchaController {

	private final Cage cage = new GCage();

	@GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
	public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
		String captchaToken = cage.getTokenGenerator().next();
		captchaToken = captchaToken.substring(0, 6);
		// Store CAPTCHA token in session
		session.setAttribute("captcha", captchaToken);
//		System.err.println(captchaToken);

		response.setContentType("image/png");
		cage.draw(captchaToken, response.getOutputStream());
	}

//	public boolean validateCaptcha(String sessionId, String userInput) {
//		if (sessionId == null || userInput == null)
//			return false;
//
//		String storedCaptcha = captchaStore.get(sessionId);
////		System.err.println(
////				"Validating - Session: " + sessionId + ", User Input: " + userInput + ", Stored: " + storedCaptcha);
//
//		if (storedCaptcha != null && storedCaptcha.equalsIgnoreCase(userInput)) {
//			captchaStore.remove(sessionId); // Use once only
//			return true;
//		}
//		return false;
//	}
}
