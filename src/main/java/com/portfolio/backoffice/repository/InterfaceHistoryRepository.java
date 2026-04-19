package com.portfolio.backoffice.repository;

import com.portfolio.backoffice.domain.InterfaceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link InterfaceHistory} 엔티티에 대한 DB 접근을 담당하는 리포지토리입니다.
 *
 * <p>[엔터프라이즈 SM 운영 관점]<br>
 * 인터페이스 이력은 “사후 감사·장애 리포트·용량 산정”의 원천 데이터입니다. 리포지토리 계층을 분리해 두면
 * (1) 동일 쿼리를 여러 화면/API에서 재사용하고, (2) 읽기 전용 복제본(reader)으로 라우팅하는 등
 * 운영 성능 튜닝을 나중에 넣기 쉽습니다. {@link JpaRepository} 상속만으로 기본 CRUD가 생성되는 것은
 * 반복 코드를 줄여 <b>관제 기능 개발 속도</b>를 높이기 위한 스프링 데이터 JPA의 설계 철학입니다.
 */
public interface InterfaceHistoryRepository extends JpaRepository<InterfaceHistory, Long> {
}
