
## 🔗 Related Issue
- #123 

## 📝 Description
> 무엇을(What), 왜(Why) 변경했는지 설명합니다.

### 기획 배경 및 비즈니스 문제
- 예: 기존 결제 시스템에서 특정 카드사의 할부 승인이 거절되는 문제를 해결하기 위해 로직을 수정했습니다.

## 🛠 Changes
> 핵심 변경 사항을 요약합니다.

- **API 변경**: `POST /api/v1/payments` 엔드포인트에 `installment_month` 파라미터 추가
- **DB 스키마**: `orders` 테이블에 `promotion_code` 컬럼 추가

## ✅ Test Checklist
> 변경 사항이 정상적으로 동작하는지 확인하기 위해 수행한 테스트 목록입니다.

- [ ] 단위 테스트(Unit Test)를 작성하고 통과했나요?
- [ ] 관련 API 응답 결과가 기획서와 일치하는지 확인했나요?