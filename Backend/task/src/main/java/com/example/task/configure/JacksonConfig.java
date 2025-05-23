//package com.example.task.configure;
//
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class JacksonConfig implements WebMvcConfigurer {
//
//	@Override
//	public void extendMessageConverters(java.util.List<HttpMessageConverter<?>> converters) {
//		// Register JavaTimeModule to handle Java 8 date/time types
//		Jackson2ObjectMapperBuilder.json().modulesToInstall(new JavaTimeModule()) // Install JavaTimeModule to handle
//																					// LocalDate, etc.
//				.build();
//	}
//}
