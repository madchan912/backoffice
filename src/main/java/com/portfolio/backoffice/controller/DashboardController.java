package com.portfolio.backoffice.controller;

import com.portfolio.backoffice.domain.InterfaceHistory;
import com.portfolio.backoffice.domain.ProjectCost;
import com.portfolio.backoffice.dto.InterfaceDetailResponse;
import com.portfolio.backoffice.dto.NewInterfaceRequest;
import com.portfolio.backoffice.repository.InterfaceHistoryRepository;
import com.portfolio.backoffice.repository.ProjectCostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * 대시보드(화면)와 간단한 REST API를 담당하는 컨트롤러입니다.
 *
 * <p>[엔터프라이즈 SM 관점]<br>
 * “조회(GET)”만 있으면 운영자는 <b>통제(Control)</b>를 할 수 없습니다. 재처리(재시도)·신규 인터페이스 메타데이터 등록·
 * 상세 전문 확인 같은 쓰기/명령 API가 함께 있어야 장애 대응 시간(MTTR)을 줄일 수 있습니다. 컨트롤러는 HTTP 계약(경로·메서드·상태코드)을
 * 지키는 얇은 경계층으로 두고, 트랜잭션 경계는 변경이 있는 유스케이스에 {@code @Transactional}로 표시하는 것이 일반적입니다.
 *
 * <p>{@code @Controller}는 “응답으로 HTML 뷰(Thymeleaf)를 반환할 수 있다”는 뜻입니다.
 * (반대로 {@code @RestController}는 기본적으로 JSON 등으로 바디만 내릴 때 자주 씁니다.)
 *
 * <p>{@code @RequiredArgsConstructor}(Lombok)는 <b>final 필드</b>를 모아 생성자를 만들어 줍니다.
 * 스프링은 그 생성자로 빈(Bean)을 주입하므로, {@code private final Repository} 형태가
 * “생성자 주입” 패턴이 됩니다.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    /** [SM 실무] 운영 콘솔에서 허용하는 프로토콜 값을 화이트리스트로 고정하면 오타·보안 사고(잘못된 스킴 입력)를 예방합니다. */
    private static final Set<String> ALLOWED_PROTOCOLS =
            Set.of("REST", "SOAP", "MQ", "Batch", "SFTP");

    /**
     * [관리회계] 차트 X축(본부) 순서를 조직도·경영 보고서와 동일하게 고정합니다.
     * 임의 정렬이면 임원진이 슬라이드 간 비교 시 혼선을 겪습니다.
     */
    private static final List<String> COST_CHART_DEPARTMENT_ORDER =
            List.of("A본부", "B본부", "C본부", "D본부", "E본부");

    private final InterfaceHistoryRepository interfaceHistoryRepository;
    private final ProjectCostRepository projectCostRepository;

    /**
     * 루트 URL({@code /}) 접속 시 인터페이스 모니터링 화면으로 보냅니다.
     *
     * <p>[SM 실무] 사용자가 북마크한 진입점이 달라도 동일한 관제 화면으로 수렴시키면 교육·온콜 인수인계 비용이 줄어듭니다.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/interface";
    }

    /**
     * 인터페이스 연동 이력 화면({@code interface.html})을 보여 줍니다.
     *
     * <p><b>Model에 데이터를 담는 이유</b><br>
     * 컨트롤러가 DB에서 꺼낸 자바 객체(List 등)를 화면(Thymeleaf)이 읽으려면,
     * 스프링 MVC가 제공하는 {@link org.springframework.ui.Model}에 “이름표(키) + 값” 형태로 넣어 줘야 합니다.
     *
     * <p><b>정렬(Sort)</b><br>
     * {@code findAll(Sort.by(...))}는 DB에서 가져올 때부터 최근 실행일 순으로 정렬합니다.
     * 초기 로딩 UX를 맞추기 위한 서버 기본값이며, 화면에서는 추가로 클라이언트 정렬을 제공합니다.
     *
     * <p><b>평균 응답 시간</b><br>
     * [SM 실무] 관제 상단 KPI는 “건수”뿐 아니라 <b>지연</b>이 함께 있어야 성능 이슈를 조기에 감지할 수 있습니다.
     * (실서비스에서는 시계열 DB·메트릭 서버에서 p95/p99를 가져오는 것이 표준입니다.)
     */
    @GetMapping("/interface")
    public String interfacePage(Model model) {
        List<InterfaceHistory> histories =
                interfaceHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "executeDate"));

        long totalCount = histories.size();
        long successCount = histories.stream().filter(h -> "Success".equals(h.getStatus())).count();
        long failCount = histories.stream().filter(h -> "Fail".equals(h.getStatus())).count();

        double avg =
                histories.stream().mapToInt(InterfaceHistory::getResponseTime).average().orElse(0.0);
        long avgResponseMs = Math.round(avg);

        model.addAttribute("histories", histories);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("avgResponseMs", avgResponseMs);
        model.addAttribute("activeMenu", "interface");

        return "interface";
    }

    /**
     * 원가(프로젝트 비용) 모니터링 화면({@code cost.html})을 보여 줍니다.
     *
     * <p><b>집계 로직 (KPI)</b><br>
     * 표준 원가(예산) 합, 집행 원가 합, 잔여 예산(예산−집행)은 전사 관점의 요약 지표입니다.
     * 경영진이 “한 장의 숫자”로 재무 건전성을 판단할 수 있게 서버에서 미리 합산합니다.
     *
     * <p><b>본부별 예산 소진율(차트용)</b><br>
     * [관리회계] 부서별 {@code Σ집행 / Σ표준 × 100}은 “예산 대비 소진 속도”를 나타내는 대표 지표입니다.
     * 100% 초과 시 초과 집행(또는 표준 산정과의 시차)을 의미하며, Chart.js에는 소수 첫째 자리까지 반올림해 전달합니다.
     * (실무에서는 회계 마감·인식 기준에 따라 분모를 ‘연간 예산’으로 바꾸기도 합니다.)
     */
    @GetMapping("/cost")
    public String costPage(Model model) {
        List<ProjectCost> costs = projectCostRepository.findAll(Sort.by("departmentName", "id"));

        long totalBudget = costs.stream().mapToLong(ProjectCost::getStandardCost).sum();
        long totalSpent = costs.stream().mapToLong(ProjectCost::getCurrentCost).sum();
        long remainingBudget = totalBudget - totalSpent;

        List<Double> deptBurnRates = new ArrayList<>(COST_CHART_DEPARTMENT_ORDER.size());
        for (String dept : COST_CHART_DEPARTMENT_ORDER) {
            long sumStandard =
                    costs.stream()
                            .filter(c -> dept.equals(c.getDepartmentName()))
                            .mapToLong(ProjectCost::getStandardCost)
                            .sum();
            long sumCurrent =
                    costs.stream()
                            .filter(c -> dept.equals(c.getDepartmentName()))
                            .mapToLong(ProjectCost::getCurrentCost)
                            .sum();
            double pct = sumStandard > 0 ? (100.0 * sumCurrent / sumStandard) : 0.0;
            deptBurnRates.add(Math.round(pct * 10.0) / 10.0);
        }

        model.addAttribute("costs", costs);
        model.addAttribute("totalBudget", totalBudget);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("chartDeptLabels", COST_CHART_DEPARTMENT_ORDER);
        model.addAttribute("chartDeptBurnRates", deptBurnRates);
        model.addAttribute("activeMenu", "cost");
        return "cost";
    }

    /**
     * [엔터프라이즈 SM 관점] 신규 연동 건의 “카탈로그 등록”은 변경관리(ITSM)와 연결됩니다.
     * 운영 콘솔에서 즉시 등록하면 장애 시 온콜이 엔드포인트·담당 조직을 바로 확인할 수 있어 MTTR이 단축됩니다.
     *
     * <p>POST {@code /api/interface/new}는 JSON 바디를 받아 DB에 저장합니다. 생성 직후의 식별자를 내려주면
     * 프론트가 토스트/리다이렉트 처리하기 쉽습니다.
     */
    @PostMapping(value = "/api/interface/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> registerInterface(@RequestBody NewInterfaceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body required");
        }
        String name = blankToNull(request.interfaceName());
        String protocol = canonicalProtocol(request.protocol());
        String endpoint = blankToNull(request.endpoint());
        String department = blankToNull(request.department());

        if (name == null || protocol == null || endpoint == null || department == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "required fields missing");
        }

        String interfaceId = nextInterfaceId();
        InterfaceHistory row =
                InterfaceHistory.builder()
                        .interfaceId(interfaceId)
                        .targetSystem("수동등록")
                        .interfaceName(name)
                        .endpoint(endpoint)
                        .department(department)
                        .responseTime(ThreadLocalRandom.current().nextInt(10, 2001))
                        .communicationType(protocol)
                        .executeDate(LocalDateTime.now())
                        .status("Success")
                        .build();
        interfaceHistoryRepository.save(row);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "id", row.getId(),
                                "interfaceId", row.getInterfaceId()));
    }

    /**
     * [엔터프라이즈 SM 관점] “요청/응답 전문 + 에러 상세”는 감사·분쟁·보안 조사에서 요구되는 정보입니다.
     * 실제로는 마스킹(주민번호·계좌)·접근권한(RBAC)이 필수이며, 여기서는 교육용으로 Mock JSON을 생성합니다.
     */
    @GetMapping(value = "/api/interface/{id}/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public InterfaceDetailResponse interfaceDetail(@PathVariable Long id) {
        InterfaceHistory history =
                interfaceHistoryRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return buildInterfaceDetailMock(history);
    }

    /**
     * 장애(Fail) 건을 “재처리했다”고 가정하고 상태를 Success로 바꾸는 <b>간이 REST API</b>입니다.
     *
     * <p>[SM 실무] 운영자의 “재시도”는 표준 운영 절차(SOP)에 따라 이벤트 로그·승인자·사유 코드를 남겨야 합니다.
     * 이 포트폴리오에서는 UI에서 지연 시뮬레이션을 두고, 서버는 상태 전이만 수행합니다.
     *
     * <p><b>왜 {@code @ResponseBody}인가?</b><br>
     * 이 메서드는 HTML 페이지가 아니라 문자열 {@code "ok"}를 HTTP 응답 <b>바디</b>에 그대로 실어 보냅니다.
     *
     * <p><b>왜 {@code @Transactional}인가?</b><br>
     * JPA에서 {@code findById}로 읽은 엔티티를 수정한 뒤 {@code save}하는 흐름은 DB 트랜잭션 안에서
     * 일관되게 처리하는 것이 안전합니다.
     */
    @PostMapping("/api/interface/{id}/retry")
    @ResponseBody
    @Transactional
    public String retryInterface(@PathVariable Long id) {
        InterfaceHistory history =
                interfaceHistoryRepository
                        .findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if ("Fail".equals(history.getStatus())) {
            history.setStatus("Success");
            history.setResponseTime(ThreadLocalRandom.current().nextInt(10, 500));
            interfaceHistoryRepository.save(history);
        }
        return "ok";
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** 화이트리스트에 있는 표기(대소문자 무시)만 허용합니다. */
    private String canonicalProtocol(String input) {
        if (input == null) {
            return null;
        }
        String t = input.trim();
        return ALLOWED_PROTOCOLS.stream().filter(a -> a.equalsIgnoreCase(t)).findFirst().orElse(null);
    }

    private String nextInterfaceId() {
        long seq = interfaceHistoryRepository.count() + 1L;
        return "IF-" + String.format("%05d", seq);
    }

    private static InterfaceDetailResponse buildInterfaceDetailMock(InterfaceHistory h) {
        String ep = jsonEscape(h.getEndpoint());
        String ifName = jsonEscape(h.getInterfaceName());
        String ifId = jsonEscape(h.getInterfaceId());

        String requestPayloadJson =
                """
                {
                  "traceId": "T-%d",
                  "interfaceId": "%s",
                  "interfaceName": "%s",
                  "endpoint": "%s",
                  "requestedAt": "%s",
                  "operatorHint": "SM-console-mock"
                }"""
                        .formatted(h.getId(), ifId, ifName, ep, h.getExecuteDate());

        boolean fail = "Fail".equals(h.getStatus());
        String responsePayloadJson =
                fail
                        ? """
                {
                  "httpStatus": 504,
                  "code": "GATEWAY_TIMEOUT",
                  "message": "Upstream did not respond in time"
                }"""
                        : """
                {
                  "httpStatus": 200,
                  "code": "OK",
                  "message": "Processed successfully"
                }""";

        String errorLogDetail =
                fail
                        ? ("[ERROR] peer=" + ep + "\n"
                                + "java.net.SocketTimeoutException: Read timed out (3000ms)\n"
                                + "    at com.insurance.gateway.OutboundClient.execute(OutboundClient.java:112)\n"
                                + "    ... suppressed for portfolio demo\n"
                                + "Caused by: target SLA breach — consider circuit breaker / retry backoff")
                        : "에러 없음 (정상 종료)";

        return new InterfaceDetailResponse(
                h.getId(),
                h.getInterfaceId(),
                h.getInterfaceName(),
                h.getEndpoint(),
                h.getStatus(),
                requestPayloadJson,
                responsePayloadJson,
                errorLogDetail);
    }

    private static String jsonEscape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
