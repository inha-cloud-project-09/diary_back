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
public class EmotionVectorConverter implements AttributeConverter<List<Double>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("감정 벡터를 JSON으로 변환하는 중 오류 발생", e);
            return "[]";
        }
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return new ArrayList<>();
            }

            JsonNode jsonNode = objectMapper.readTree(dbData);
            List<Double> result = new ArrayList<>();

            if (jsonNode.isArray()) {
                // 배열인 경우
                for (JsonNode node : jsonNode) {
                    if (node.isNumber()) {
                        result.add(node.asDouble());
                    }
                }
            } else if (jsonNode.isObject()) {
                // 객체인 경우 (예: {"0": 0.0, "1": 0.8, ...})
                // 키를 숫자로 정렬하여 순서대로 처리
                for (int i = 0; i < 10; i++) {
                    JsonNode value = jsonNode.get(String.valueOf(i));
                    if (value != null && value.isNumber()) {
                        result.add(value.asDouble());
                    } else {
                        result.add(0.0); // 값이 없는 경우 0으로 채움
                    }
                }
            }

            return result;
        } catch (JsonProcessingException e) {
            log.error("JSON을 감정 벡터로 변환하는 중 오류 발생: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}