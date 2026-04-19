package com.portfolio.backoffice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 본부/프로젝트 단위의 <b>원가(예산 vs 집행)</b> 현황을 표현하는 JPA 엔티티입니다.
 *
 * <p>[엔터프라이즈 SM 운영 관점]<br>
 * 예산 대비 집행은 “통제(Control)”의 재무 축입니다. 집행이 예산을 넘으면 변경 승인·추가 예산이 없는 한
 * 릴리즈·인프라 증설이 막히므로, 운영 조직은 이 지표를 서비스 연속성과 연관 지어 봅니다.
 *
 * <p>금액은 {@link Long}으로 두어 소수 없이 “원” 단위 정수로 다룹니다(간단한 데모/내부용에 적합).
 */
@Entity
@Table(name = "project_cost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 본부 이름(예: A본부). */
    @Column(name = "department_name", nullable = false, length = 32)
    private String departmentName;

    /** 프로젝트 표시 이름. */
    @Column(name = "project_name", nullable = false, length = 128)
    private String projectName;

    /** 표준 원가(예산에 가까운 개념). */
    @Column(name = "standard_cost", nullable = false)
    private Long standardCost;

    /** 현재까지 집행된 원가. */
    @Column(name = "current_cost", nullable = false)
    private Long currentCost;

    /**
     * 화면 표시용 상태.
     * 이 프로젝트에서는 집행이 예산을 넘으면 {@code 초과}, 그렇지 않으면 {@code 안정}으로 세팅합니다.
     */
    @Column(nullable = false, length = 16)
    private String status;
}
