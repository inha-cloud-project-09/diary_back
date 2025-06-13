package com.diary.api.domain.diary.dto;

import com.diary.api.domain.diary.entity.Diary;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DiaryDto {
    private Long id;
    private String content;
    private Boolean isPublic;
    private String summary;
    private String feedback;
    private List<String> tags;
    private String primaryEmotion;
    private String analysisStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;

    // 사용자 정보는 필요한 필드만 포함
    private Long userId;
    private String userName;

    // Diary 엔티티를 DTO로 변환하는 정적 메서드
    public static DiaryDto from(Diary diary) {
        DiaryDto dto = new DiaryDto();
        BeanUtils.copyProperties(diary, dto);

        // User 정보는 필요한 것만 복사
        if (diary.getUser() != null) {
            dto.setUserId(diary.getUser().getId());
            dto.setUserName(diary.getUser().getEmail());
        }

        return dto;
    }
}
