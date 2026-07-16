package com.approagency.base.theme

enum class ThemeMode(
    val englishLabel: String,
    val persianLabel: String
) {
    SYSTEM(
        englishLabel = "System",
        persianLabel = "سیستم"
    ),
    LIGHT(
        englishLabel = "Light",
        persianLabel = "روشن"
    ),
    DARK(
        englishLabel = "Dark",
        persianLabel = "تیره"
    );

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.firstOrNull {
                it.name.equals(value, true)
            } ?: SYSTEM
        }
    }
}