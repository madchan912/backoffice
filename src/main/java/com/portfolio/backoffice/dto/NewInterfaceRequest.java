package com.portfolio.backoffice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * [엔터프라이즈 SM 관점]
 * 신규 인터페이스를 “코드 배포 없이” 등록·승인 흐름에 넣기 위한 최소 계약(Contract)입니다.
 * 보험/금융 IT에서는 대외기관·제휴사 연동 건이 수시로 늘어나므로, 운영 콘솔에서 메타데이터(엔드포인트·담당 조직)를
 * 즉시 반영하지 못하면 장애 시 책임 소재 추적·통제(승인·감사)가 지연됩니다. JSON 바디로 받는 이유는
 * 프론트(fetch)와 백엔드의 필드 매핑을 명확히 하고, 향후 API 게이트웨이·WAF 정책과 연동할 때도 동일 스키마를 재사용하기
 * 좋기 때문입니다.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)}는 클라이언트가 버전 업으로 필드를 추가해도
 * 서버가 당장 모르는 필드 때문에 전체 요청이 깨지지 않게 하는 실무 방어선입니다(하위 호환).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NewInterfaceRequest(
        String interfaceName,
        String protocol,
        String endpoint,
        String department) {}
