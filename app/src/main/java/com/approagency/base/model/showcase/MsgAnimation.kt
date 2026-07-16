package com.approagency.base.model.showcase

sealed class MsgAnimation(open val duration: Int = DEFAULT_DURATION) {
    companion object {
        const val DEFAULT_DURATION = 250
        const val RESET_DELAY = 250
    }


    data class FadeInOut(override val duration: Int = DEFAULT_DURATION) : MsgAnimation(duration)


    object None : MsgAnimation(0)
}