package com.diary.api.domain.user.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private static final String APPLICATION_NAME = "Emotion Diary";

    /**
     * 현재 로그인된 사용자의 지난 한 달간 구글 캘린더 일정을 가져옵니다.
     * 
     * @return CalendarEventDto 리스트
     */
    public List<CalendarEventDto> getEventsForLastMonth() {
        try {
            Calendar calendarService = getCalendarService();

            // 조회할 시간 범위 설정 (오늘부터 지난 한 달까지)
            DateTime timeMin = new DateTime(ZonedDateTime.now().minusMonths(1).toInstant().toEpochMilli());
            DateTime timeMax = new DateTime(System.currentTimeMillis());

            // "primary"는 사용자의 기본 캘린더를 의미합니다.
            // API를 호출하여 이벤트 목록을 가져옵니다.
            Events events = calendarService.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();

            if (items == null || items.isEmpty()) {
                log.info("조회 기간 내에 구글 캘린더 일정이 없습니다.");
                return Collections.emptyList();
            }

            // 구글 Event 모델을 우리 DTO 모델로 변환하여 반환
            return items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (IOException | GeneralSecurityException e) {
            log.error("구글 캘린더 일정을 가져오는 중 오류 발생", e);
            throw new RuntimeException("캘린더 일정 조회에 실패했습니다.", e);
        }
    }

    /**
     * Spring Security에서 Access Token을 가져와 Google Calendar 서비스 객체를 생성합니다.
     */
    private Calendar getCalendarService() throws GeneralSecurityException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                "google", // application.yml에 설정한 registrationId
                authentication.getName());

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        Credential credential = new GoogleCredential().setAccessToken(accessToken.getTokenValue());

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Google의 Event 모델을 우리의 DTO로 변환하는 헬퍼 메소드
    private CalendarEventDto convertToDto(Event event) {
        String id = event.getId();
        String summary = event.getSummary();
        String description = event.getDescription();
        DateTime startDateTime = event.getStart().getDateTime();
        String startTime = (startDateTime != null) ? startDateTime.toString() : "하루 종일"; // 날짜만 있는 경우 처리

        return new CalendarEventDto(id, summary, description, startTime);
    }

    // 클라이언트에게 전달할 데이터 전송 객체(DTO)
    // 이 클래스는 Service 파일 내부에 두거나, 별도의 DTO 패키지 파일로 분리할 수 있습니다.
    public record CalendarEventDto(String id, String summary, String description, String startTime) {
    }
}