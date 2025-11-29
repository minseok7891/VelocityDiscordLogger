# VelocityDiscordLogger

한국어 | [English](README.md)

Velocity 마인크래프트 프록시용 플러그인으로, 네트워크 레벨의 접속/퇴장 알림을 Discord에 아름다운 Embed와 플레이어 아바타와 함께 전송합니다.

## ✨ 주요 기능

- **네트워크 레벨 감지** - 백엔드 서버 간 이동이 아닌, 실제 네트워크 접속/퇴장만 감지
- **풍부한 Discord Embed** - 왼쪽 색상 바가 있는 아름다운 Embed (접속은 녹색, 퇴장은 빨간색)
- **플레이어 아바타** - Embed의 작성자 필드에 플레이어의 마인크래프트 머리 표시
- **통합 설정** - 모든 Discord 채널 ID를 하나의 설정 파일에서 관리
- **경량화** - 데이터베이스 불필요, 최소한의 성능 영향
- **커스터마이징 가능한 메시지** - 플레이스홀더를 이용한 사용자 정의 메시지 형식 지원

## 📸 미리보기

Discord에 다음과 같이 표시됩니다:
- 🟢 접속 시 녹색 Embed (#00ff00)
- 🔴 퇴장 시 빨간색 Embed (#ff0000)
- 👤 Visage API를 통한 플레이어 아바타
- 📝 한국어/영어 커스터마이징 가능한 메시지

## 📥 설치 방법

1. [Releases](https://github.com/minseok7891/VelocityDiscordLogger/releases)에서 최신 `VelocityDiscordLogger-X.X.X.jar` 다운로드
2. JAR 파일을 `velocity/plugins/` 폴더에 배치
3. Velocity 프록시 서버 재시작
4. `velocity/plugins/velocitydiscordlogger/config.toml`을 봇 토큰과 채널 ID로 수정
5. 설정 적용을 위해 Velocity 다시 재시작

## ⚙️ 설정

최초 실행 후, `velocity/plugins/velocitydiscordlogger/config.toml` 파일을 수정하세요:

```toml
# Discord 봇 토큰 (https://discord.com/developers/applications 에서 발급)
bot_token = "여기에_봇_토큰_입력"

# 채널 ID (한 곳에서 모든 채널 관리)
[channels]
log = "1234567890"           # 서버 로그 채널 ID
chat = "9876543210"          # 채팅 채널 ID (향후 사용)
deaths = "1111111111"        # 사망 로그 채널 ID (향후 사용)
achievements = "2222222222"  # 도전과제 채널 ID (향후 사용)

# 접속 메시지 설정
[messages.join]
enabled = true
color = "#00ff00"
format = "%username% 님이 서버에 접속하셨습니다."

# 퇴장 메시지 설정
[messages.quit]
enabled = true
color = "#ff0000"
format = "%username% 님이 서버에서 나가셨습니다."

# 플레이어 아바타 설정
[avatar]
base_url = "https://visage.surgeplay.com/face/96/{uuid}"
```

### 사용 가능한 플레이스홀더

- `%username%` - 플레이어의 마인크래프트 닉네임
- `%uuid%` - 플레이어의 마인크래프트 UUID

## 🤖 Discord 봇 설정

1. [Discord 개발자 포털](https://discord.com/developers/applications)로 이동
2. "New Application" 클릭 후 이름 지정
3. "Bot" 탭으로 이동하여 "Add Bot" 클릭
4. **중요:** 다음 Gateway Intents를 활성화하세요:
   - ✅ Server Members Intent
   - ✅ Presence Intent
5. 봇 토큰을 복사하여 `config.toml`에 붙여넣기
6. "OAuth2" → "URL Generator"로 이동
   - 스코프 선택: `bot`
   - 권한 선택: `Send Messages`, `Embed Links`
7. 생성된 URL을 복사하여 열고 봇을 Discord 서버에 초대

## 🔧 소스에서 빌드하기

요구사항:
- Java 17+
- Gradle

```bash
git clone https://github.com/minseok7891/VelocityDiscordLogger.git
cd VelocityDiscordLogger
gradle shadowJar
```

빌드된 JAR 파일은 `build/libs/VelocityDiscordLogger-1.0.0.jar`에 생성됩니다.

## 📋 필요 조건

- Velocity 3.3.0 이상
- Java 17 이상
- Gateway Intents가 활성화된 Discord 봇
- 적절한 권한이 있는 Discord 서버

## 🆚 다른 솔루션과 비교

| 기능 | VelocityDiscordLogger | DiscordSRV | HuskChat |
|------|----------------------|------------|----------|
| 네트워크 레벨 감지 | ✅ | ❌ (서버 레벨) | ✅ |
| Embed + 아바타 동시 지원 | ✅ | ✅ | ❌ |
| 서버 이동 시 스팸 없음 | ✅ | ❌ | ✅ |
| 경량화 | ✅ | ⚠️ | ✅ |

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다 - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🙏 크레딧

- [JDA (Java Discord API)](https://github.com/discord-jda/JDA)를 사용하여 제작
- [Visage](https://visage.surgeplay.com/)에서 플레이어 아바타 제공

## 💬 지원

문제, 기능 요청 또는 질문이 있으시면:
- [GitHub Issues](https://github.com/minseok7891/VelocityDiscordLogger/issues)에 이슈 등록
- Discord 서버: [준비 중]

## 🤝 기여

기여는 언제나 환영합니다! Pull Request를 자유롭게 제출해주세요.
