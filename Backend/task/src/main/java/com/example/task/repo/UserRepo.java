package com.example.task.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	// Add to UserRepo interface
	Page<User> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
			String name, String username, String email, String contactNumber, Pageable pageable);

	Page<User> findByAccessRoleIgnoreCase(String accessRole, Pageable pageable);

	Page<User> findByAccessRoleIgnoreCaseAndNameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndUsernameContainingIgnoreCaseOrAccessRoleIgnoreCaseAndEmailContainingIgnoreCaseOrAccessRoleIgnoreCaseAndContactNumberContainingIgnoreCase(
			String role1, String name, String role2, String username, String role3, String email, String role4,
			String contactNumber, Pageable pageable);
}
