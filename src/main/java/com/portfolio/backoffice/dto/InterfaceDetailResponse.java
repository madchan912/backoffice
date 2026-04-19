package com.portfolio.backoffice.dto;

/**
 * [엔터프라이즈 SM 관점]
 * 장애/이상 징후 발생 시 “요청·응답 전문(Message)”과 “스택/에러 상세”를 함께 보여 주는 것은
 * L1/L2 운영자가 개발팀에 넘기기 전 1차 원인 분류(연동사 문제 vs 내부 설정 vs 네트워크)를 가능하게 하는 핵심 기능입니다.
 * 실제로는 APM·로그 수집기(ELK 등)와 연동하지만, 포트폴리오/교육 목적에서는 Mock JSON으로도
 * “왜 이 화면이 필요한지”를 설명할 수 있습니다.
 */
public record InterfaceDetailResponse(
        Long id,
        String interfaceId,
        String interfaceName,
        String endpoint,
        String status,
        String requestPayloadJson,
        String responsePayloadJson,
        String errorLogDetail) {}
