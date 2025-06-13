package com.diary.api.domain.diary.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter
public class TagsConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("태그를 JSON으로 변환하는 중 오류 발생", e);
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return new ArrayList<>();
            }

            JsonNode jsonNode = objectMapper.readTree(dbData);
            List<String> result = new ArrayList<>();

            if (jsonNode.isArray()) {
                // 배열인 경우
                for (JsonNode node : jsonNode) {
                    if (node.isTextual()) {
                        result.add(node.asText());
                    }
                }
            } else if (jsonNode.isObject()) {
                // 객체인 경우 (예: {"0": "태그1", "1": "태그2", ...})
                // 키를 숫자로 정렬하여 순서대로 처리
                for (int i = 0;; i++) {
                    JsonNode value = jsonNode.get(String.valueOf(i));
                    if (value == null) {
                        break;
                    }
                    if (value.isTextual()) {
                        result.add(value.asText());
                    }
                }
            }

            return result;
        } catch (JsonProcessingException e) {
            log.error("JSON을 태그로 변환하는 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}