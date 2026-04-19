package com.portfolio.backoffice.support;

import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 화면(Thymeleaf)에서 금액을 “1,234,567”처럼 보기 좋게 찍기 위한 작은 도우미(빈)입니다.
 *
 * <p>[엔터프라이즈 SM 운영 관점]<br>
 * 관제·경영 화면에서 금액 오독(자릿수 실수)은 잘못된 의사결정으로 이어집니다. 로케일 기반 포맷을 중앙에 두면
 * 모든 화면이 동일 규칙으로 표시되어 감사·보고서 작성 시 혼선이 줄어듭니다.
 *
 * <p>템플릿에서 {@code ${@moneyFormat.format(totalBudget)}}처럼 호출합니다.
 * {@code @Component("moneyFormat")}의 이름이 곧 템플릿에서의 빈 이름입니다.
 *
 * <p><b>스레드 안전</b><br>
 * {@link NumberFormat} 인스턴스를 필드로 재사용하면 멀티스레드 요청에서 문제가 될 수 있어,
 * 호출할 때마다 새로 만들어 포맷팅합니다(이 규모에서는 충분히 가볍습니다).
 */
@Component("moneyFormat")
public class MoneyFormat {

    /** 원 단위 long 값을 한국 로케일 기준으로 쉼표 구분 문자열로 바꿉니다. */
    public String format(long amount) {
        return NumberFormat.getInstance(Locale.KOREA).format(amount);
    }
}
