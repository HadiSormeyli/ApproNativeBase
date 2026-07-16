package com.approagency.base.config


enum class Flavor(
    val englishLabel: String, val persianLabel: String, val gateway: String
) {
    BAZAAR(
        englishLabel = "Bazaar",
        persianLabel = "بازار",
        gateway = "bazar"
    ),
    MYKET(
        englishLabel = "Myket",
        persianLabel = "مایکت",
        gateway = "myket"
    ),
    GOOGLE_PLAY(
        englishLabel = "Google Play",
        persianLabel = "گوگل پلی",
        gateway = "google_play"
    );

    companion object {
        fun fromString(value: String?): Flavor {
            return entries.firstOrNull {
                it.name.equals(value, true) ||
                        it.englishLabel.equals(value, true) ||
                        it.persianLabel.equals(value, true)
            } ?: BAZAAR
        }
    }
}