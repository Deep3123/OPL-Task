package com.example.task.util;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
		return objectMapper.convertValue(fromValue, toValueType);
	}

	public static <T> List<T> convertListofValue(List<?> fromValue, Class<T> toValueType)
			throws IllegalArgumentException {
		return objectMapper.convertValue(fromValue,
				objectMapper.getTypeFactory().constructCollectionType(List.class, toValueType));
	}
}
