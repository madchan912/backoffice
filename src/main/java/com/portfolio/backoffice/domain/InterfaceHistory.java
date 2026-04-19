package com.portfolio.backoffice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 외부 시스템과의 인터페이스(연동) <b>실행 이력</b>을 표현하는 JPA 엔티티입니다.
 *
 * <p>[엔터프라이즈 SM(서비스 관리) 운영 관점]<br>
 * 단순 조회 화면만으로는 “누가·어디로·어떤 프로토콜로·얼마나 빨리” 연동했는지 감사 추적이 불가능합니다.
 * 금융/보험권에서는 감독 대응·보안 사고 대응 시 <b>엔드포인트·담당 조직·응답 지연</b>이 핵심 메타데이터이므로
 * 운영 DB(또는 운영 색인)에 정규화해 두는 것이 표준입니다. 엔티티 필드가 곧 “운영 통제(Control)의 단위”가 됩니다.
 *
 * <p>엔티티(Entity)는 “DB 테이블 한 줄(row)”과 1:1로 매핑되는 자바 객체로 생각하면 쉽습니다.
 * 필드에 붙은 {@code @Column(name = "...")}는 자바의 카멜케이스와 DB의 스네이크케이스를 연결합니다.
 */
@Entity
@Table(name = "interface_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterfaceHistory {

    /** 기본 키. DB가 자동 증가(Identity)로 채워 줍니다. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업무용 인터페이스 식별자(예: IF-00001).
     *
     * <p>[SM 실무] 운영자/협력사 간 통화·티켓에서 “IF 번호”로 건을 특정합니다. SQL 예약어(interface)와 겹치지 않게
     * 컬럼명을 {@code interface_id}로 분리한 것은 DDL 이식성과 가독성을 위한 선택입니다.
     */
    @Column(name = "interface_id", nullable = false, length = 64)
    private String interfaceId;

    /**
     * 연동 대상 기관/시스템(예: 금감원, 은행).
     *
     * <p>[SM 실무] SLA·장애 등급 산정 시 “대외 기관” 여부가 우선순위를 좌우합니다. 인터페이스명과 별도로 두면
     * 동일 기관에 여러 채널이 있어도 집계·리포팅이 쉬워집니다.
     */
    @Column(name = "target_system", nullable = false, length = 64)
    private String targetSystem;

    /**
     * 업무 관점의 인터페이스 표시명(예: 금감원 대외계 송신).
     *
     * <p>[SM 실무] 기술 ID(IF-00001)만으로는 비개발 운영자가 의미를 파악하기 어렵습니다. 관제 콘솔에는
     * 사람이 읽는 이름을 반드시 병기해 장애 공지·대표이사 보고용 요약을 빠르게 작성할 수 있게 합니다.
     */
    @Column(name = "interface_name", nullable = false, length = 128)
    private String interfaceName;

    /**
     * 실제 접속 주소(URL) 또는 MQ 큐명·배치 경로 등.
     *
     * <p>[SM 실무] 장애 시 “어느 URL/큐가 느린지”가 1차 질문입니다. 엔드포인트 단위로 차단·허용·레이트리밋을
     * 거는 API 게이트웨이 정책과도 1:1로 매핑되므로 운영 데이터로서 필수입니다.
     */
    @Column(name = "endpoint", nullable = false, length = 512)
    private String endpoint;

    /**
     * 내부 담당 부서/파트.
     *
     * <p>[SM 실무] 연동 장애는 거의 항상 “담당 개발/운영 조직” 에스컬레이션으로 이어집니다. 티켓 시스템과 동일한
     * 조직 코드를 쓰면 온콜 로테이션·승인 워크플로와 자동 연계할 수 있습니다.
     */
    @Column(name = "department", nullable = false, length = 64)
    private String department;

    /**
     * 해당 호출의 응답 시간(밀리초).
     *
     * <p>[SM 실무] 평균/백분위 응답 시간은 성능 관제의 기본 KPI입니다. 임계치 초과 시 알람을 울리거나
     * 트래픽 스로틀링을 걸기 위한 입력값으로도 사용됩니다.
     */
    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTime;

    /** 통신 방식(예: REST, SOAP, MQ, Batch, SFTP). */
    @Column(name = "communication_type", nullable = false, length = 32)
    private String communicationType;

    /** 인터페이스가 실행된 시각. */
    @Column(name = "execute_date", nullable = false)
    private LocalDateTime executeDate;

    /** 실행 결과. 이 프로젝트에서는 {@code Success} 또는 {@code Fail} 문자열을 사용합니다. */
    @Column(nullable = false, length = 16)
    private String status;
}
