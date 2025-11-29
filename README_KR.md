# VelocityDiscordLogger (한국어)

Minecraft Velocity 프록시 서버를 위한 강력한 디스코드 로깅 플러그인입니다. 네트워크 전체의 접속/퇴장 로그, 서버 상태 알림, 콘솔 로그 전송을 지원하며, 백엔드 플러그인과 연동하여 도전과제 및 사망 메시지도 로깅할 수 있습니다.

## 주요 기능

- **네트워크 전체 로깅**:
  - **접속/퇴장**: 플레이어가 네트워크에 접속하거나 나갈 때 로그를 남깁니다.
- **서버 상태 알림**:
  - 프록시 서버가 시작될 때(`:white_check_mark:`)와 종료될 때(`:octagonal_sign:`) 알림을 보냅니다.
  - **강력한 종료 감지**: `docker restart`와 같은 강제 종료 상황에서도 알림을 보낼 수 있도록 JVM Shutdown Hook과 REST API 폴백 기능을 탑재했습니다.
- **콘솔 로그 전송**:
  - Velocity 콘솔에 찍히는 로그를 실시간으로 디스코드 채널로 전송합니다.
- **백엔드 연동**:
  - `VelocityDiscordLogger-Backend` 플러그인과 연동하여 백엔드 서버(Paper/Purpur)에서 발생하는 **도전과제**와 **사망 메시지**를 로깅합니다.
- **완벽한 커스터마이징**:
  - `config.toml`을 통해 메시지 포맷, 색상, 활성화 여부를 자유롭게 설정할 수 있습니다.
  - 플레이어 아바타(Head) 표시를 지원합니다.

## 설치 방법

1. 최신 릴리즈를 다운로드합니다.
2. `VelocityDiscordLogger-1.0.0.jar` 파일을 Velocity 서버의 `plugins` 폴더에 넣습니다.
3. 서버를 재시작합니다.
4. `plugins/velocitydiscordlogger/config.toml` 파일에 디스코드 봇 토큰과 채널 ID를 입력합니다.

## 설정 예시

```toml
# 디스코드 봇 설정
bot_token = "여기에_봇_토큰_입력"

[channels]
log = "채널_ID"           # 접속/퇴장 로그
status = "채널_ID"        # 서버 시작/종료 알림
console = "채널_ID"       # 콘솔 로그
achievements = "채널_ID"  # 도전과제 로그 (백엔드 플러그인 필요)
deaths = "채널_ID"        # 사망 로그 (백엔드 플러그인 필요)

[messages.join]
enabled = true
format = "%username% 님이 서버에 접속하셨습니다."

[messages.stop]
enabled = true
format = ":octagonal_sign: 서버가 종료되었습니다."
```

## 백엔드 플러그인

**도전과제**와 **사망 메시지** 로깅을 활성화하려면, 백엔드 서버(Lobby, Survival 등)에 [VelocityDiscordLogger-Backend](https://github.com/minseok7891/VelocityDiscordLogger-Backend) 플러그인을 설치해야 합니다.
