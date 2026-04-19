package com.portfolio.backoffice.config;

import com.portfolio.backoffice.domain.InterfaceHistory;
import com.portfolio.backoffice.domain.ProjectCost;
import com.portfolio.backoffice.repository.InterfaceHistoryRepository;
import com.portfolio.backoffice.repository.ProjectCostRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션이 처음 뜰 때 H2 같은 인메모리 DB가 비어 있으면 화면이 텅 비어 보입니다.
 * 그래서 <b>개발/포트폴리오용 목(Mock) 데이터</b>를 자동으로 넣는 설정 클래스입니다.
 *
 * <p>[엔터프라이즈 SM 관점]<br>
 * 운영 관제 콘솔은 “실제와 유사한 메타데이터(엔드포인트·부서·응답시간 분포)”가 있어야 UX 검증·데모·교육이 가능합니다.
 * 단순 랜덤 문자열은 이해관계자에게 신뢰를 주지 못하므로, 금융/보험 IT에서 자주 보는 유형(금감원, 은행 API, MQ 사고 접수 등)을
 * 시드로 고정해 두는 방식이 일반적입니다. {@code @PostConstruct}로 1회 적재하는 것은 CI 파이프라인·로컬 개발의 반복 실행을
 * 고려한 “멱등성에 가까운” 초기화 패턴입니다.
 *
 * <p>{@code @Component}로 등록되어 스프링이 한 개의 빈으로 관리하고,
 * {@code @PostConstruct}가 붙은 {@code init()}은 “의존성 주입이 끝난 직후 딱 한 번” 실행됩니다.
 * (DB 스키마 생성 이후, 리포지토리를 사용할 수 있는 시점에 맞추기 좋습니다.)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

    /**
     * [SM 실무] 시드 데이터는 “현업 용어”로 써야 운영자·감사 대응 시나리오 리허설에 바로 쓸 수 있습니다.
     * 아래 각 행은 (인터페이스명, 대상 시스템, 프로토콜, 엔드포인트/큐, 담당 부서)를 나타냅니다.
     */
    private static final InterfaceSeed[] INTERFACE_SEEDS = {
        new InterfaceSeed(
                "금감원 대외계 송신",
                "금감원",
                "REST",
                "https://disclosure.fss.or.kr/api/v2/external/submit",
                "대외감독운영팀"),
        new InterfaceSeed(
                "은행 잔액 조회 API",
                "은행",
                "REST",
                "https://openapi.kbbank.co.kr/v1/accounts/balance",
                "디지털채널개발팀"),
        new InterfaceSeed(
                "제휴처 사고 접수 MQ",
                "제휴사",
                "MQ",
                "QUEUE://INSURANCE/CLAIM/PARTNER.INBOUND.V1",
                "손해사정연동파트"),
        new InterfaceSeed(
                "배치 약관 갱신 파일 수신",
                "카드사",
                "SFTP",
                "sftp://batch.partner.co.kr/incoming/policy/daily",
                "정보보호실"),
        new InterfaceSeed(
                "보험료 결제 대행 SOAP",
                "결제대행사",
                "SOAP",
                "https://pg.settlement.co.kr/soap/PaymentGateway",
                "수금운영팀"),
        new InterfaceSeed(
                "증권사 펀드 시세 Batch",
                "증권사",
                "Batch",
                "file:///batch/incoming/fund/nav/daily.csv",
                "자산운용지원팀"),
        new InterfaceSeed(
                "핀테크 건강검진 결과 Webhook",
                "핀테크",
                "REST",
                "https://api.healthlink.io/v3/checkups/callback",
                "헬스케어사업팀"),
        new InterfaceSeed(
                "내부 코어 보험계약 조회",
                "보험사",
                "MQ",
                "QUEUE://CORE/POLICY.LOOKUP.REQUEST",
                "코어시스템운영팀"),
        new InterfaceSeed(
                "카드사 무이자 할부 제휴 검증 API",
                "카드사",
                "REST",
                "https://partner.card.co.kr/api/v1/promotions/validate",
                "마케팅기획팀"),
        new InterfaceSeed(
                "금융결제원 이체 결과 통지",
                "결제대행사",
                "MQ",
                "QUEUE://KFTC/TRANSFER.NOTIFY.OUT",
                "자금이체운영팀"),
    };

    /** 인터페이스 실행 결과 후보입니다. */
    private static final String[] INTERFACE_STATUSES = {"Success", "Fail"};

    /** 원가 데이터의 본부 이름 후보(5개)입니다. */
    private static final String[] DEPARTMENTS = {"A본부", "B본부", "C본부", "D본부", "E본부"};

    /**
     * [관리회계] 집행이 표준(예산)을 초과한 경우 흔히 보고되는 사유 후보입니다.
     * 실무에서는 ERP·그룹웨어 전표의 비고·WBS 비용요소와 연동됩니다.
     */
    private static final String[] COST_OVER_REASONS = {
        "인건비 단가 상승",
        "공통비 배분 증가",
        "외주 인력 추가 투입",
        "클라우드 이용료 인상",
        "전산 개발 공수 증가",
        "긴급 장애 대응 인력 투입",
        "제휴사 인증 범위 확대로 외부 검증 비용 증가",
        "IT 인프라 내부대체비용 정산액 증가"
    };

    /** [관리회계] 예산 범위 내·절감형 집행에 대한 설명 후보입니다. */
    private static final String[] COST_STABLE_REASONS = {
        "예산 내 정상 집행",
        "절감 활동으로 집행 페이스 조절",
        "초기 단계로 집행이 분산되어 표준 대비 여유",
        "내부 인력 전환으로 외주비 절감"
    };

    /** 랜덤 금액의 최솟값(원 단위, 예: 100만). */
    private static final long COST_MIN = 1_000_000L;

    /**
     * 랜덤 금액의 최댓값(원 단위, 예: 1000만). {@code nextLong}의 상한은 제외이므로 +1 처리는 아래 메서드에서 합니다.
     */
    private static final long COST_MAX = 10_000_000L;

    private final InterfaceHistoryRepository interfaceHistoryRepository;
    private final ProjectCostRepository projectCostRepository;

    /**
     * 이미 데이터가 있으면 다시 넣지 않습니다.
     *
     * <p>[SM 실무] 운영 DB에서는 이런 자동 시드를 쓰지 않지만, 개발/스테이징에서는 “빈 환경”이 더 위험합니다.
     * 반복 insert로 데이터가 폭증하면 성능 테스트·알람 임계치 검증이 왜곡되므로 count 기반 가드가 필요합니다.
     */
    @PostConstruct
    public void init() {
        if (interfaceHistoryRepository.count() == 0) {
            interfaceHistoryRepository.saveAll(buildInterfaceHistories());
        }
        if (projectCostRepository.count() == 0) {
            projectCostRepository.saveAll(buildProjectCosts());
        }
    }

    /**
     * 인터페이스 이력 50건을 <b>실무형 시드 + 랜덤 변주</b>로 만듭니다.
     *
     * <p>{@code responseTime}은 10~2000ms 범위의 난수로 부여하여 평균 응답·지연 알람 시뮬레이션에 쓰입니다.
     * {@link ThreadLocalRandom#current()}는 멀티스레드 환경에서 권장되는 난수 생성기입니다.
     */
    private List<InterfaceHistory> buildInterfaceHistories() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        List<InterfaceHistory> list = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            InterfaceSeed seed = INTERFACE_SEEDS[r.nextInt(INTERFACE_SEEDS.length)];
            list.add(
                    InterfaceHistory.builder()
                            .interfaceId("IF-" + String.format("%05d", i + 1))
                            .targetSystem(seed.targetSystem())
                            .interfaceName(seed.interfaceName())
                            .endpoint(seed.endpoint())
                            .department(seed.department())
                            .responseTime(r.nextInt(10, 2001))
                            .communicationType(seed.communicationType())
                            .executeDate(
                                    LocalDateTime.now()
                                            .minusDays(r.nextLong(0, 90))
                                            .minusHours(r.nextInt(24))
                                            .minusMinutes(r.nextInt(60)))
                            .status(INTERFACE_STATUSES[r.nextInt(INTERFACE_STATUSES.length)])
                            .build());
        }
        return list;
    }

    /**
     * 프로젝트 원가 200건을 랜덤으로 만듭니다.
     *
     * <p><b>본부 분배</b><br>
     * {@code i % 5}는 0,1,2,3,4가 반복되므로 A~E본부에 <b>골고루(각 40건)</b> 배치됩니다.
     *
     * <p><b>금액과 상태</b><br>
     * 표준 원가와 집행 원가를 각각 랜덤으로 만든 뒤,
     * 집행이 표준을 넘으면 {@code "초과"}, 아니면 {@code "안정"}으로 비즈니스 규칙을 단순 구현했습니다.
     *
     * <p><b>사유({@code costReason})</b><br>
     * [관리회계] 상태와 사유를 일치시켜 두면 대시보드·드릴다운 리포트에서 “숫자 뒤의 스토리”를 재현할 수 있습니다.
     * 초과 건에는 비용 발생 원인을, 안정 건에는 통제 정상·절감 narrative를 부여합니다.
     */
    private List<ProjectCost> buildProjectCosts() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        List<ProjectCost> list = new ArrayList<>(200);
        for (int i = 0; i < 200; i++) {
            long standard = randomCost(r);
            long current = randomCost(r);
            String status = current > standard ? "초과" : "안정";
            String reason =
                    "초과".equals(status)
                            ? COST_OVER_REASONS[r.nextInt(COST_OVER_REASONS.length)]
                            : COST_STABLE_REASONS[r.nextInt(COST_STABLE_REASONS.length)];
            list.add(
                    ProjectCost.builder()
                            .departmentName(DEPARTMENTS[i % DEPARTMENTS.length])
                            .projectName("프로젝트-" + String.format("%03d", i + 1))
                            .standardCost(standard)
                            .currentCost(current)
                            .status(status)
                            .costReason(reason)
                            .build());
        }
        return list;
    }

    /** {@code COST_MIN} 이상 {@code COST_MAX} 이하(포함)의 난수 금액을 만듭니다. */
    private static long randomCost(ThreadLocalRandom r) {
        return r.nextLong(COST_MIN, COST_MAX + 1);
    }

    private record InterfaceSeed(
            String interfaceName,
            String targetSystem,
            String communicationType,
            String endpoint,
            String department) {}
}
