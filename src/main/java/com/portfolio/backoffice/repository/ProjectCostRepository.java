package com.portfolio.backoffice.repository;

import com.portfolio.backoffice.domain.ProjectCost;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link ProjectCost} 엔티티에 대한 DB 접근을 담당하는 리포지토리입니다.
 *
 * <p>[엔터프라이즈 SM 운영 관점]<br>
 * 원가/예산 데이터는 IT 거버넌스(과제 승인, 과금, 클라우드 비용 최적화)의 입력입니다. 리포지토리를 얇게 유지하면
 * 나중에 “월별 집계 쿼리”, “부서별 상한 알람” 같은 읽기 모델을 {@code @Query}로 확장하기 쉽습니다.
 *
 * <p>제네릭의 두 번째 타입 파라미터 {@code Long}은 엔티티 {@link ProjectCost#getId()}의 타입(기본 키 타입)입니다.
 */
public interface ProjectCostRepository extends JpaRepository<ProjectCost, Long> {
}
