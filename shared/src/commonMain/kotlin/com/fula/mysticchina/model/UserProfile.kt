// shared/src/commonMain/kotlin/com/mysticchina/model/UserProfile.kt
package com.fula.mysticchina.model

/**
 * 用户资料数据模型。
 * 存储使用 KuiklyUI 内置 SharedPreferencesModule（跨平台，无需 expect/actual）。
 *
 * 存储键：
 *   mysticchina_nickname   → String
 *   mysticchina_bio        → String
 *   mysticchina_avatar     → String
 */
data class UserProfile(
    val nickname: String = "创作者",
    val bio: String = "记录生活的每一刻",
    val avatarEmoji: String = "🎬"
)
