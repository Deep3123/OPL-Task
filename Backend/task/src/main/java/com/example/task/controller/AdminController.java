package com.example.task.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.task.domain.User;
import com.example.task.enums.Gender;
import com.example.task.proxy.ForgotPasswordRequest;
import com.example.task.proxy.LoginRequest;
import com.example.task.proxy.LoginResponse;
import com.example.task.proxy.ResetPassword;
import com.example.task.proxy.Response;
import com.example.task.proxy.UserProxy;
import com.example.task.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
//@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestParam("name") String name, @RequestParam("email") String email,
			@RequestParam("dob") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dob,
			@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("gender") Gender gender, @RequestParam("address") String address,
			@RequestParam("mobileNo") String mobileNo, @RequestParam("pinCode") String pinCode,
			@RequestParam("accessRole") String accessRole,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

		try {
			String message = adminService.registerUser(name, email, dob, username, password, gender, address, mobileNo,
					pinCode, accessRole, profileImage);

			return new ResponseEntity<>(new Response(message, HttpStatus.CREATED.toString()), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(
					new Response("Registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST.toString()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
		String expectedCaptcha = (String) session.getAttribute("captcha");
//		System.err.println("Expected captcha----> " + expectedCaptcha);
//		System.err.println("From Frontend----> " + req.getCaptchaResponse());

		if (expectedCaptcha == null || !expectedCaptcha.equalsIgnoreCase(req.getCaptchaResponse())) {
			return new ResponseEntity<>(
					new Response("Invalid CAPTCHA. Please try again.", HttpStatus.UNAUTHORIZED.toString()),
					HttpStatus.UNAUTHORIZED);
		}

		// Proceed with normal login
		try {
			LoginResponse res = adminService.login(req);
			return new ResponseEntity<>(res, HttpStatus.ACCEPTED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(new Response(e.getMessage(), HttpStatus.UNAUTHORIZED.toString()),
					HttpStatus.UNAUTHORIZED);
		}
	}

	// 1. Get all users
	@GetMapping("/get-all-user-details")
	public ResponseEntity<?> getAllUsers() {
		List<UserProxy> users = adminService.getAllusers();

		if (users != null && !users.isEmpty())
			return new ResponseEntity<>(users, HttpStatus.OK);
		else
			return new ResponseEntity<>(new Response("No user records found.", HttpStatus.NOT_FOUND.toString()),
					HttpStatus.NOT_FOUND);
	}

	// 2. Get user by username
	@GetMapping("/get-user-by-username/{username}")
	public ResponseEntity<?> getUserByUsername(@Valid @PathVariable("username") String username) {
		try {
			User user = adminService.getUserByUsername(username);
			return new ResponseEntity<>(user, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(
					new Response("User not found with given username.", HttpStatus.NOT_FOUND.toString()),
					HttpStatus.NOT_FOUND);
		}
	}

	// 3. Update user by username
	@PostMapping("/update-user-by-username")
	public ResponseEntity<?> updateUserByUsername(@Valid @RequestBody UserProxy userProxy,
			BindingResult bindingResult) {
		System.err.println(userProxy);
		if (bindingResult.hasErrors()) {
			List<String> errors = bindingResult.getFieldErrors().stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			return new ResponseEntity<>(new Response(errors.toString(), HttpStatus.BAD_REQUEST.toString()),
					HttpStatus.BAD_REQUEST);
		}

		try {
			String result = adminService.updateUser(userProxy, userProxy.getUsername());
			return new ResponseEntity<>(new Response(result, HttpStatus.OK.toString()), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(
					new Response("User not found with given username.", HttpStatus.NOT_FOUND.toString()),
					HttpStatus.NOT_FOUND);
		}
	}

	// 4. Delete user by username
	@GetMapping("/delete-user-by-username/{username}")
	public ResponseEntity<?> deleteUserByUsername(@Valid @PathVariable("username") String username) {
		try {
			String result = adminService.deleteUser(username);
			return new ResponseEntity<>(new Response(result, HttpStatus.OK.toString()), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(
					new Response("User not found with given username.", HttpStatus.NOT_FOUND.toString()),
					HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/get-all-users-pagewise")
	public ResponseEntity<?> getAllUsersPageWise(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Page<UserProxy> usersPage = adminService.getAllUsersPageWise(PageRequest.of(page, size));

		if (usersPage.hasContent()) {
			return new ResponseEntity<>(usersPage, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					new Response("No users found for the given page.", HttpStatus.NOT_FOUND.toString()),
					HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest,
			HttpSession session) {
		String expectedCaptcha = (String) session.getAttribute("captcha");
		System.err.println("Expected captcha----> " + expectedCaptcha);
		System.err.println("From Frontend----> " + forgotPasswordRequest.getCaptchaResponse());

		if (expectedCaptcha == null || !expectedCaptcha.equalsIgnoreCase(forgotPasswordRequest.getCaptchaResponse())) {
			return new ResponseEntity<>(
					new Response("Invalid CAPTCHA. Please try again.", HttpStatus.UNAUTHORIZED.toString()),
					HttpStatus.UNAUTHORIZED);
		}

		String s = adminService.forgotPassword(forgotPasswordRequest.getEmail());

		if (s.equals("A password reset link has been sent to your registered email address. "
				+ "Please check your inbox and follow the instructions to reset your password."))
			return new ResponseEntity<>(new Response(s, HttpStatus.OK.toString()), HttpStatus.OK);

		return new ResponseEntity<>(new Response(s, HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);
	}

	@PostMapping("/reset-password/{username}/{timestamp}/{token}")
	public ResponseEntity<?> resetPassword(@Valid @PathVariable("username") String username,
			@PathVariable("timestamp") String timestamp, @PathVariable("token") String token,
			@RequestBody ResetPassword proxy) {
		try {
			// Pass username and token to service for processing
			String s = adminService.resetPassword(username, timestamp, token, proxy);

			if (s.equals("Password not matching.") || s.equals("Username in token does not match provided username!")
					|| s.equals("Token is expired, please request again to reset your password!")
					|| s.equals("User was not found to perform this action!")) {
				return new ResponseEntity<>(new Response(s, HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);
			}

			return new ResponseEntity<>(new Response(s, HttpStatus.OK.toString()), HttpStatus.OK);

		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(new Response("Invalid token format.", HttpStatus.BAD_REQUEST.toString()),
					HttpStatus.BAD_REQUEST);
		}
	}

//	@GetMapping("/generate-fake-users")
//	public ResponseEntity<?> generateFakeUsers() {
//		String message = adminService.generateFakeUsers();
//		return new ResponseEntity<>(new Response(message, HttpStatus.CREATED.toString()), HttpStatus.CREATED);
//	}

	@GetMapping("/search-users")
	public ResponseEntity<?> searchUsers(@RequestParam String searchTerm, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		try {
			Page<UserProxy> usersPage = adminService.searchUsers(searchTerm, PageRequest.of(page, size));

			if (usersPage.hasContent()) {
				return new ResponseEntity<>(usersPage, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						new Response("No users found matching your search criteria.", HttpStatus.NOT_FOUND.toString()),
						HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new Response("Error searching users: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/export-users-excel")
	public ResponseEntity<?> exportUsersToExcel(@RequestParam(required = false) String searchTerm) {
		try {
			ByteArrayResource excelReport = adminService.generateExcelReport(searchTerm);

			// Get current date for filename
			LocalDate now = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String filename = "users_export_" + now.format(formatter) + ".xlsx";

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType
							.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(excelReport);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}
}
