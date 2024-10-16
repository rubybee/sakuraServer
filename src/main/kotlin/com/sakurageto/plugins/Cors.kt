package com.sakurageto.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configurationCORS() {
    // CORS 플러그인 설치 및 설정
    install(CORS) {
        // 허용할 특정 호스트를 지정 (예: http://localhost:3000)
        allowHost("localhost:3000")

        // 모든 도메인 허용하려면 아래 설정 사용 (개발 환경에서만 사용하는 것이 좋습니다)
        // anyHost()

        // 허용할 메서드 설정
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        // 허용할 헤더 설정
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        // 인증 정보(쿠키, 인증 헤더 등)를 포함한 요청 허용
        allowCredentials = true

        // CORS 설정을 사용하는 HTTP 요청의 최대 지속 시간 설정 (브라우저가 이 설정을 캐시)
        maxAgeInSeconds = 3600
    }
}
