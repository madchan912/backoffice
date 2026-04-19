package com.portfolio.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 스프링 부트 애플리케이션의 <b>진입점(Entry Point)</b> 클래스입니다.
 *
 * <p>[엔터프라이즈 SM 운영 관점]<br>
 * 백오피스·관제 콘솔은 보통 사내 전용망에서 실행되며, 가용성(무중단 배포·헬스체크)과 구성 분리(프로파일별 설정)가
 * 운영 품질을 좌우합니다. 부트스트랩 클래스는 “런타임 조립의 시작점”이므로, 여기서 과도한 비즈니스 로직을 넣지 않고
 * 컴포넌트 스캔 범위만 명확히 유지하는 것이 장기 유지보수에 유리합니다.
 *
 * <p>{@code @SpringBootApplication} 한 줄에 다음이 포함됩니다(개념적으로는 이렇게 이해하면 면접에 좋습니다).
 * <ul>
 *   <li>{@code @Configuration} — 빈 설정 클래스로 인식</li>
 *   <li>{@code @EnableAutoConfiguration} — 클래스패스에 맞는 기본 설정 자동 등록</li>
 *   <li>{@code @ComponentScan} — 같은 패키지 이하 컴포넌트(@Controller 등) 스캔</li>
 * </ul>
 *
 * <p>{@link SpringApplication#run}이 내장 톰캣을 띄우고, 웹 요청을 받을 준비를 마칩니다.
 */
@SpringBootApplication
public class BackofficeApplication {

    /** JVM이 실행하는 최초 메서드입니다. 여기서 스프링 컨텍스트를 구동합니다. */
    public static void main(String[] args) {
        SpringApplication.run(BackofficeApplication.class, args);
    }
}
