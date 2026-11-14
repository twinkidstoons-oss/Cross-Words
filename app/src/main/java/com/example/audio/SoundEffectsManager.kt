package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R

class SoundEffectsManager(context: Context) {

    private var soundPool: SoundPool? = null
    private var tapId = 0
    private var correctId = 0
    private var wrongId = 0
    private var winId = 0
    private var tickId = 0
    private var swipeId = 0
    private var bonusId = 0
    private var timeOverId = 0

    @Volatile
    var isMuted: Boolean = false

    private var lastSwipeTimeMs: Long = 0L

    init {
        runCatching {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build()
            tapId = pool.load(context, R.raw.sfx_tap, 1)
            correctId = pool.load(context, R.raw.sfx_correct, 1)
            wrongId = pool.load(context, R.raw.sfx_wrong, 1)
            winId = pool.load(context, R.raw.sfx_win, 1)
            tickId = pool.load(context, R.raw.sfx_tick, 1)
            swipeId = pool.load(context, R.raw.sfx_swipe, 1)
            bonusId = pool.load(context, R.raw.sfx_bonus, 1)
            timeOverId = pool.load(context, R.raw.sfx_time_over, 1)
            soundPool = pool
        }
    }

    fun playTap() = play(tapId, 0.7f)

    fun playCorrect() = play(correctId, 0.9f)

    fun playWrong() = play(wrongId, 0.75f)

    fun playWin() = play(winId, 0.95f)

    fun playTick() = play(tickId, 0.55f)

    fun playBonus() = play(bonusId, 0.9f)

    fun playTimeOver() = play(timeOverId, 0.85f)

    fun playSwipe() {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTimeMs < 75L) return
        lastSwipeTimeMs = now
        play(swipeId, 0.45f)
    }

    private fun play(soundId: Int, volume: Float) {
        if (isMuted || soundId == 0) return
        runCatching {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun release() {
        runCatching {
            soundPool?.release()
            soundPool = null
        }
    }
}
