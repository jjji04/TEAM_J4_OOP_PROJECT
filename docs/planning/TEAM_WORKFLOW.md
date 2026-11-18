# GitHub 협업 규칙 (초안)

- `main`: 발표·제출용 안정 버전
- `develop`: 팀 코드 통합용
- `feature/movie`: A 영화 및 상영 관리
- `feature/reservation`: B 좌석 및 예약 관리
- `feature/ticket`: C 티켓 및 요금 계산
- `feature/member`: D 회원 및 데이터 관리

## 브랜치 생성 (develop이 GitHub에 올라간 뒤)

각자 자신의 컴퓨터에서:

```bash
git fetch origin
git switch develop
git pull origin develop
git switch -c feature/ticket  # 담당자에 맞게 변경
git push -u origin feature/ticket
```

## 작업 및 통합

1. 담당 브랜치에서 파일 수정
2. `git add .` / `git commit -m "feat: ..."` / `git push`
3. GitHub에서 `feature/*` → `develop` Pull Request 생성
4. 다른 팀원 1명 이상 확인 후 병합
5. 기능 검증 후 `develop` → `main` 병합

### 공동 파일 수정

- 공통 DTO·인터페이스·패키지명은 팀 회의 후 결정
- 다른 담당 패키지를 직접 변경해야 하면 사전 협의
- GitHub에 비밀번호, API 키, 실사용 개인정보 업로드 금지
