package com.example.task.service.impl;

import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.task.domain.User;
import com.example.task.enums.Gender;
import com.example.task.globalexception.ListEmptyException;
import com.example.task.proxy.LoginRequest;
import com.example.task.proxy.LoginResponse;
import com.example.task.proxy.ResetPassword;
import com.example.task.proxy.UserProxy;
import com.example.task.repo.UserRepo;
import com.example.task.service.AdminService;
import com.example.task.util.JwtService;
import com.example.task.util.MapperUtil;

import jakarta.mail.internet.MimeMessage;
//import java.time.ZoneId;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UserRepo repo;

	@Autowired
	private CustomPasswordResetTokenGenerator generator;

	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String sender;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private AuthenticationManager manager;

	@Autowired
	private JwtService jwtService;

	@Override
	public LoginResponse login(LoginRequest req) {
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(req.getUsername(),
				req.getPassword());

		try {
			Authentication authenticate = manager.authenticate(auth);
			Optional<User> optionalUser = repo.findByUsername(req.getUsername());

			if (authenticate.isAuthenticated() && optionalUser.isPresent()) {
				User user = optionalUser.get();

				// Get profile image as Base64 encoded string
				String profileImageData = null;
				if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
					try {
						// Remove leading slash if present
						String imagePath = user.getProfileImage();
						if (imagePath.startsWith("/")) {
							imagePath = imagePath.substring(1);
						}

						Path path = Paths.get(imagePath);
						if (Files.exists(path) && Files.isReadable(path)) {
							byte[] imageBytes = Files.readAllBytes(path);
							profileImageData = "data:image/jpeg;base64,"
									+ Base64.getEncoder().encodeToString(imageBytes);

							// Set the profileImage field to the Base64 data directly
							user.setProfileImage(profileImageData);
						} else {
							System.err.println("Cannot access profile image at: " + path);
						}
					} catch (IOException e) {
						System.err.println("Error reading profile image: " + e.getMessage());
					}
				}

				return new LoginResponse(user.getUsername(), jwtService.generateToken(user.getUsername()),
						user.getAccessRole(), user.getName(), user.getEmail(), user.getDob(), user.getAddress(),
						user.getContactNumber(), user.getPinCode(), user.getGender(), user.getPassword(),
						user.getProfileImage() // Now contains the Base64 data
				);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"The username or password you entered is incorrect. Please verify your credentials and try again.");
		}

		return null;
	}

	@Override
	public List<UserProxy> getAllusers() {
		List<User> users = repo.findAll();

		if (users == null || users.isEmpty()) {
			throw new ListEmptyException("No user records found.");
		}

		return MapperUtil.convertListofValue(users, UserProxy.class);
	}

//	@Override
//	public Page<UserProxy> getAllUsersPageWise(Pageable pageable) {
//		Page<User> userPage = repo.findAll(pageable);
//
//		if (!userPage.hasContent()) {
//			throw new ListEmptyException("No users found for the given page.");
//		}
//
//		List<UserProxy> proxyList = MapperUtil.convertListofValue(userPage.getContent(), UserProxy.class);
//		
//		return new PageImpl<>(proxyList, pageable, userPage.getTotalElements());
//	}

//	@Override
//	public Page<UserProxy> getAllUsersPageWise(Pageable pageable) {
//		Page<User> userPage = repo.findAll(pageable);
//
//		if (!userPage.hasContent()) {
//			throw new ListEmptyException("No users found for the given page.");
//		}
//
////		List<User> updatedUsers = userPage.getContent().stream().filter(user -> user.getAccessRole().equals("USER"))
//
//		List<User> updatedUsers = userPage.getContent().stream().map(user -> {
//			String imagePath = user.getProfileImage();
//
//			if (imagePath != null && !imagePath.isEmpty()) {
//				try {
//					// Remove leading slash if present
//					if (imagePath.startsWith("/")) {
//						imagePath = imagePath.substring(1);
//					}
//
//					Path path = Paths.get(imagePath);
//					if (Files.exists(path) && Files.isReadable(path)) {
//						byte[] imageBytes = Files.readAllBytes(path);
//						String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
//						user.setProfileImage(base64Image);
//					} else {
//						System.err.println("Cannot access profile image at: " + path.toAbsolutePath());
//					}
//				} catch (IOException e) {
//					System.err
//							.println("Error reading profile image for user ID " + user.getId() + ": " + e.getMessage());
//				}
//			}
//
//			return user;
//		}).collect(Collectors.toList());
//
//		return new PageImpl<>(MapperUtil.convertListofValue(updatedUsers, UserProxy.class), pageable,
//				userPage.getTotalElements());
//	}

	@Override
	public Page<UserProxy> getAllUsersPageWise(Pageable pageable) {
		// Only fetch users with "USER" role
		Page<User> userPage = repo.findByAccessRoleIgnoreCase("USER", pageable);

		if (!userPage.hasContent()) {
			throw new ListEmptyException("No users found for the given page.");
		}

		List<User> updatedUsers = userPage.getContent().stream().map(user -> {
			String imagePath = user.getProfileImage();

			if (imagePath != null && !imagePath.isEmpty()) {
				try {
					// Remove leading slash if present
					if (imagePath.startsWith("/")) {
						imagePath = imagePath.substring(1);
					}

					Path path = Paths.get(imagePath);
					if (Files.exists(path) && Files.isReadable(path)) {
						byte[] imageBytes = Files.readAllBytes(path);
						String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
						user.setProfileImage(base64Image);
					} else {
						System.err.println("Cannot access profile image at: " + path.toAbsolutePath());
					}
				} catch (IOException e) {
					System.err
							.println("Error reading profile image for user ID " + user.getId() + ": " + e.getMessage());
				}
			}

			return user;
		}).collect(Collectors.toList());

		return new PageImpl<>(MapperUtil.convertListofValue(updatedUsers, UserProxy.class), pageable,
				userPage.getTotalElements());
	}

	@Override
	public User getUserByUsername(String username) {
		// TODO Auto-generated method stub
		Optional<User> user = repo.findByUsername(username);

		if (user.isPresent()) {
			return user.get();
		}

		throw new UsernameNotFoundException("User not found with given username.");
	}

	@Override
	public String updateUser(UserProxy userProxy, String username) {
		Optional<User> optionalUser = repo.findByUsername(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();

			if (userProxy.getName() != null) {
				user.setName(userProxy.getName());
			}

			if (userProxy.getEmail() != null) {
				user.setEmail(userProxy.getEmail());
			}

			if (userProxy.getDob() != null) {
				user.setDob(userProxy.getDob());
			}

//			if (userProxy.getPassword() != null) {
//				user.setPassword(userProxy.getPassword()); // You can encode it here if needed
//			}

			if (userProxy.getGender() != null) {
				user.setGender(userProxy.getGender());
			}

			if (userProxy.getAddress() != null) {
				user.setAddress(userProxy.getAddress());
			}

//			if (userProxy.getProfileImage() != null) {
//				user.setProfileImage(userProxy.getProfileImage());
//			}

			if (userProxy.getContactNumber() != null) {
				user.setContactNumber(userProxy.getContactNumber());
			}

			if (userProxy.getPinCode() != null) {
				user.setPinCode(userProxy.getPinCode());
			}

			if (userProxy.getAccessRole() != null) {
				user.setAccessRole(userProxy.getAccessRole());
			}

			repo.save(user); // Save after applying updates
			return "User updated successfully.";
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}

	@Override
	public String deleteUser(String username) {
		Optional<User> user = repo.findByUsername(username);

		if (user.isPresent()) {
			repo.delete(user.get());
			return "User deleted successfully.";
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}

	@Override
	public String forgotPassword(String email) {
		Optional<User> user = repo.findByEmail(email);

		if (user.isPresent()) {
			String token = generator.generateToken(user.get());

			Long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
			String timestamp = Base64.getEncoder().encodeToString(time.toString().getBytes());

			String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());

			String username = Base64.getEncoder().encodeToString(user.get().getUsername().getBytes());

			String url = "http://localhost:4200/reset-password/" + username + "/" + timestamp + "/" + encodedToken;

			System.err.println(url);

			try {

				// Create the HTML content for the email
				String htmlContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;\">"
						+ "<h2 style=\"color: #333;\">OPL Innovate - Password Reset Request</h2>" + "<p>Dear "
						+ user.get().getName() + ",</p>"
						+ "<p>We received a request to reset your password for JetWayz. "
						+ "You can reset your password by clicking the link below:</p>" + "<p><a href=\"" + url
						+ "\" style=\"background-color: #007bff; color: #fff; padding: 10px 15px; text-decoration: none; border-radius: 5px;\">Reset Password</a></p>"
						+ "<p>If you did not request a password reset, please ignore this email. "
						+ "For security reasons, this link will expire after a certain period.</p>" + "<br>"
						+ "<p>Best regards,<br><strong>The JetWayz Support Team</strong></p>" + "</div>";

				// Create a MimeMessage
				MimeMessage mimeMessage = javaMailSender.createMimeMessage();

				// Create a MimeMessageHelper
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
				helper.setFrom(sender); // Sender's email address
				helper.setTo(email); // Recipient's email address
				helper.setSubject("Password Reset Request");
				helper.setText(htmlContent, true); // Set the HTML content and mark it as HTML

				// Send the email
				javaMailSender.send(mimeMessage);
			}

			catch (Exception e) {
				// TODO: handle exception
				return "Error generated while email sending.";
			}

			return "A password reset link has been sent to your registered email address. "
					+ "Please check your inbox and follow the instructions to reset your password.";
		}
		return "User with the provided email not found.";
	}

	@Override
	public String resetPassword(String username, String timestamp, String token, ResetPassword proxy) {
		if (!proxy.getPassword().equals(proxy.getConfirmPassword())) {
			return "Password not matching.";
		}

		// Decode the token at service layer
		String decodedToken = decodeToken(token);

		// Extract the username from the decoded token
		String tokenUsername = new String(Base64.getDecoder().decode(username), StandardCharsets.UTF_8);

		Long time = Long.parseLong(new String(Base64.getDecoder().decode(timestamp)));

		// Now validate the token
		Optional<User> user = repo.findByUsername(tokenUsername);

		if (user.isPresent()) {
			if (generator.validateToken(time, decodedToken, user.get())) {
				user.get().setPassword(encoder.encode(proxy.getPassword()));
				repo.save(user.get());
				return "Password was updated successfully.";
			} else {
				return "Token is expired, please request again to reset your password!";
			}
		} else {
			return "User was not found to perform this action!";
		}
	}

	// Helper method to decode the token (used inside the service)
	private String decodeToken(String token) {
		try {
			// Decode the Base64-encoded token
			return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid token format.", e);
		}
	}

	@Override
	public String registerUser(String name, String email, Date dob, String username, String password, Gender gender,
			String address, String contactNumber, String pinCode, String accessRole, MultipartFile profileImage) {

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setDob(dob);
		user.setUsername(username);
		user.setPassword(encoder.encode(password));
		user.setGender(gender);
		user.setAddress(address);
		user.setContactNumber(contactNumber);
		user.setPinCode(pinCode);
		user.setAccessRole("USER");

		if (profileImage != null && !profileImage.isEmpty()) {
			try {
				String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
				Path uploadPath = Paths.get("uploads");

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				Path filePath = uploadPath.resolve(fileName);
				Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				user.setProfileImage("/uploads/" + fileName);
			} catch (IOException e) {
				throw new RuntimeException("Error saving profile image", e);
			}
		}

		repo.save(user);
		return "User registered successfully.";
	}

//	@Override
//	public String generateFakeUsers() {
//		Faker faker = new Faker();
//
//		for (int i = 1; i <= 105; i++) {
//			User user = new User();
//			user.setName(faker.name().fullName());
//			user.setDob(new Date( faker.date().birthday().toInstant().atOffset(ZoneOffset.UTC).toLocalDate());
//			user.setEmail(faker.internet().emailAddress());
//			user.setUsername(faker.name().username() + i); // Ensure uniqueness
//			user.setPassword(encoder.encode("Password@123")); // Default encrypted password
//			user.setGender(faker.options().option(Gender.class));
//			user.setAddress(faker.address().fullAddress());
//			user.setProfileImage(null); // Or set dummy image
//			user.setContactNumber(faker.phoneNumber().subscriberNumber(10));
//			user.setPinCode(faker.address().zipCode());
//			user.setAccessRole(i <= 5 ? "ADMIN" : "USER"); // First 5 = ADMIN, rest = USER
//
//			repo.save(user);
//		}
//
//		return "105 fake users added successfully.";
//	}

//	@Override
//	public Page<UserProxy> searchUsers(String searchTerm, Pageable pageable) {
//		// Search across multiple fields
//		Page<User> userPage = repo
//				.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
//						searchTerm, searchTerm, searchTerm, searchTerm, pageable);
//
//		if (!userPage.hasContent()) {
//			throw new ListEmptyException("No users found matching your search criteria.");
//		}
//
//		// Process profile images same as in getAllUsersPageWise
//		List<User> updatedUsers = userPage.getContent().stream().map(user -> {
//			String imagePath = user.getProfileImage();
//
//			if (imagePath != null && !imagePath.isEmpty()) {
//				try {
//					// Remove leading slash if present
//					if (imagePath.startsWith("/")) {
//						imagePath = imagePath.substring(1);
//					}
//
//					Path path = Paths.get(imagePath);
//					if (Files.exists(path) && Files.isReadable(path)) {
//						byte[] imageBytes = Files.readAllBytes(path);
//						String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
//						user.setProfileImage(base64Image);
//					} else {
//						System.err.println("Cannot access profile image at: " + path.toAbsolutePath());
//					}
//				} catch (IOException e) {
//					System.err
//							.println("Error reading profile image for user ID " + user.getId() + ": " + e.getMessage());
//				}
//			}
//
//			return user;
//		}).collect(Collectors.toList());
//
//		return new PageImpl<>(MapperUtil.convertListofValue(updatedUsers, UserProxy.class), pageable,
//				userPage.getTotalElements());
//	}

	@Override
	public Page<UserProxy> searchUsers(String searchTerm, Pageable pageable) {
		// Search across multiple fields AND filter by "USER" role
		Page<User> userPage = repo
				.findByAccessRoleIgnoreCaseAndNameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndUsernameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndEmailContainingIgnoreCaseOrAccessRoleIgnoreCaseAndContactNumberContainingIgnoreCase(
						"USER", searchTerm, "USER", searchTerm, "USER", searchTerm, "USER", searchTerm, pageable);

		if (!userPage.hasContent()) {
			throw new ListEmptyException("No users found matching your search criteria.");
		}

		// Process profile images same as in getAllUsersPageWise
		List<User> updatedUsers = userPage.getContent().stream().map(user -> {
			String imagePath = user.getProfileImage();

			if (imagePath != null && !imagePath.isEmpty()) {
				try {
					// Remove leading slash if present
					if (imagePath.startsWith("/")) {
						imagePath = imagePath.substring(1);
					}

					Path path = Paths.get(imagePath);
					if (Files.exists(path) && Files.isReadable(path)) {
						byte[] imageBytes = Files.readAllBytes(path);
						String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
						user.setProfileImage(base64Image);
					} else {
						System.err.println("Cannot access profile image at: " + path.toAbsolutePath());
					}
				} catch (IOException e) {
					System.err
							.println("Error reading profile image for user ID " + user.getId() + ": " + e.getMessage());
				}
			}

			return user;
		}).collect(Collectors.toList());

		return new PageImpl<>(MapperUtil.convertListofValue(updatedUsers, UserProxy.class), pageable,
				userPage.getTotalElements());
	}

	@Override
	public ByteArrayResource generateExcelReport(String searchTerm) throws IOException {
		List<User> users;

		// If searchTerm is provided, use the search functionality
		if (searchTerm != null && !searchTerm.trim().isEmpty()) {
			// Get all users matching search term
			Page<User> userPage = repo
					.findByAccessRoleIgnoreCaseAndNameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndUsernameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndEmailContainingIgnoreCaseOrAccessRoleIgnoreCaseAndContactNumberContainingIgnoreCase(
							"USER", searchTerm, "USER", searchTerm, "USER", searchTerm, "USER", searchTerm,
							Pageable.unpaged());
			users = userPage.getContent();
		} else {
			// Get all users with role USER
			Page<User> userPage = repo.findByAccessRoleIgnoreCase("USER", Pageable.unpaged());
			users = userPage.getContent();
		}

		if (users.isEmpty()) {
			throw new ListEmptyException("No users found for export.");
		}

		// Create workbook and sheet
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Users");

		// Create header row with styles
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());

		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setBorderBottom(BorderStyle.THIN);

		Row headerRow = sheet.createRow(0);
		String[] columns = { "ID", "Name", "Username", "Email", "Role", "Contact Number", "Address", "PIN Code",
				"Date of Birth", "Gender" };

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerStyle);
			sheet.setColumnWidth(i, 20 * 256); // 20 characters width
		}

		// Create date formatter
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

		// Regular data style
		CellStyle dataStyle = workbook.createCellStyle();
		dataStyle.setBorderBottom(BorderStyle.THIN);
		dataStyle.setBorderTop(BorderStyle.THIN);
		dataStyle.setBorderLeft(BorderStyle.THIN);
		dataStyle.setBorderRight(BorderStyle.THIN);

		// Populate data rows
		int rowNum = 1;
		for (User user : users) {
			Row row = sheet.createRow(rowNum++);

			row.createCell(0).setCellValue(user.getId());
			row.createCell(1).setCellValue(user.getName());
			row.createCell(2).setCellValue(user.getUsername());
			row.createCell(3).setCellValue(user.getEmail());
			row.createCell(4).setCellValue(user.getAccessRole());
			row.createCell(5).setCellValue(user.getContactNumber() != null ? user.getContactNumber() : "N/A");
			row.createCell(6).setCellValue(user.getAddress() != null ? user.getAddress() : "N/A");
			row.createCell(7).setCellValue(user.getPinCode() != null ? user.getPinCode() : "N/A");

			Cell dobCell = row.createCell(8);
			if (user.getDob() != null) {
				dobCell.setCellValue(dateFormatter.format(user.getDob()));
			} else {
				dobCell.setCellValue("N/A");
			}

			Cell genderCell = row.createCell(9);
			if (user.getGender() != null) {
				genderCell.setCellValue(user.getGender().toString());
			} else {
				genderCell.setCellValue("N/A");
			}

			// Apply style to all cells in the row
			for (int i = 0; i < columns.length; i++) {
				row.getCell(i).setCellStyle(dataStyle);
			}
		}

		// Auto-size columns for better readability
		for (int i = 0; i < columns.length; i++) {
			sheet.autoSizeColumn(i);
		}

		// Write to ByteArrayOutputStream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		// Create and return ByteArrayResource
		byte[] byteArray = outputStream.toByteArray();
		return new ByteArrayResource(byteArray);
	}
}
