package com.diary.api.controller;

import com.diary.api.domain.user.service.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/events")
    public ResponseEntity<List<GoogleCalendarService.CalendarEventDto>> getMyCalendarEvents() {
        // 서비스 클래스의 메소드를 호출하여 현재 로그인한 사용자의 캘린더 이벤트를 가져옵니다.
        List<GoogleCalendarService.CalendarEventDto> events = googleCalendarService.getEventsForLastMonth();
        return ResponseEntity.ok(events);
    }
}
