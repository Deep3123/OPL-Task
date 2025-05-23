package com.example.task.globalexception;

import com.example.task.proxy.Response;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// Handle validation errors from @Valid in DTOs
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		return new ResponseEntity<>(new Response(errors.toString(), HttpStatus.BAD_REQUEST.toString()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ListEmptyException.class)
	public ResponseEntity<?> handleListEmptyException(ListEmptyException ex) {
		return new ResponseEntity<>(new Response(ex.getMessage(), HttpStatus.NOT_FOUND.toString()),
				HttpStatus.NOT_FOUND);
	}

	// Handle invalid path variables or request params (e.g., String to int
	// conversion issues)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String message = String.format("Invalid value for parameter '%s'. Expected type: %s", ex.getName(),
				ex.getRequiredType().getSimpleName());
		return new ResponseEntity<>(new Response(message, HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);
	}

	// Handle missing required parameters
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
		String message = "Missing required parameter: " + ex.getParameterName();
		return new ResponseEntity<>(new Response(message, HttpStatus.BAD_REQUEST.toString()), HttpStatus.BAD_REQUEST);
	}

	// Handle bad JSON inputs
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		return new ResponseEntity<>(new Response("Malformed JSON request.", HttpStatus.BAD_REQUEST.toString()),
				HttpStatus.BAD_REQUEST);
	}

	// Handle unsupported HTTP methods (e.g., POST instead of GET)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		String message = "This method is not supported for the requested endpoint.";
		return new ResponseEntity<>(new Response(message, HttpStatus.METHOD_NOT_ALLOWED.toString()),
				HttpStatus.METHOD_NOT_ALLOWED);
	}

	// Handle constraint violations from validation annotations on method parameters
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
		List<String> messages = ex.getConstraintViolations().stream().map(v -> v.getMessage())
				.collect(Collectors.toList());

		return new ResponseEntity<>(new Response(messages.toString(), HttpStatus.BAD_REQUEST.toString()),
				HttpStatus.BAD_REQUEST);
	}

	// Handle Spring Security-related username errors
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException ex) {
		return new ResponseEntity<>(new Response(ex.getMessage(), HttpStatus.NOT_FOUND.toString()),
				HttpStatus.NOT_FOUND);
	}

	// Generic fallback exception handler
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGeneralException(Exception ex) {
		ex.printStackTrace(); // Optional: log to file
		return new ResponseEntity<>(new Response("Internal server error occurred: " + ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
