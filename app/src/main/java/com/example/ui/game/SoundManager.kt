package com.example.ui.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    var musicEnabled = true
    var sfxEnabled = true

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.4f) {
        if (!sfxEnabled) return
        
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate / 1000)
                val samples = FloatArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                    val envelope = if (i < numSamples * 0.15f) {
                        i / (numSamples * 0.15f)
                    } else if (i > numSamples * 0.75f) {
                        1.0f - (i - numSamples * 0.75f) / (numSamples * 0.25f)
                    } else {
                        1.0f
                    }
                    samples[i] = (sin(angle) * volume * envelope).toFloat()
                }
                
                val builder = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 4)
                    .setTransferMode(AudioTrack.MODE_STATIC)

                val audioTrack = builder.build()
                
                audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
                audioTrack.play()
                
                kotlinx.coroutines.delay(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun playSweepTone(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float = 0.4f) {
        if (!sfxEnabled) return
        
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val numSamples = (durationMs * sampleRate / 1000)
                val samples = FloatArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * t
                    val angle = 2.0 * Math.PI * i / (sampleRate / currentFreq)
                    val envelope = if (i > numSamples * 0.7f) {
                        1.0f - (i - numSamples * 0.7f) / (numSamples * 0.3f)
                    } else {
                        1.0f
                    }
                    samples[i] = (sin(angle) * volume * envelope).toFloat()
                }
                
                val builder = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 4)
                    .setTransferMode(AudioTrack.MODE_STATIC)

                val audioTrack = builder.build()
                
                audioTrack.write(samples, 0, samples.size, AudioTrack.WRITE_NON_BLOCKING)
                audioTrack.play()
                
                kotlinx.coroutines.delay(durationMs.toLong() + 30)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    fun playMatch3Pop() {
        playTone(950.0, 80, 0.45f)
    }

    fun playWoodHit() {
        playTone(320.0, 90, 0.45f)
    }

    fun playWoodBreak() {
        GlobalScope.launch {
            playTone(280.0, 100, 0.5f)
            kotlinx.coroutines.delay(40)
            playSweepTone(200.0, 80.0, 150, 0.55f)
        }
    }

    fun playStoneHit() {
        playTone(180.0, 110, 0.52f)
    }

    fun playStoneBreak() {
        GlobalScope.launch {
            playTone(150.0, 140, 0.6f)
            kotlinx.coroutines.delay(50)
            playSweepTone(140.0, 40.0, 280, 0.65f)
        }
    }

    fun playIceHit() {
        playTone(1100.0, 60, 0.42f)
    }

    fun playIceBreak() {
        GlobalScope.launch {
            playTone(1250.0, 80, 0.45f)
            kotlinx.coroutines.delay(30)
            playSweepTone(1400.0, 800.0, 200, 0.5f)
        }
    }

    fun playChainHit() {
        playTone(850.0, 70, 0.38f)
    }

    fun playChainBreak() {
        GlobalScope.launch {
            playTone(920.0, 60, 0.42f)
            kotlinx.coroutines.delay(40)
            playSweepTone(880.0, 1600.0, 150, 0.45f)
        }
    }

    fun playMagicHit() {
        playSweepTone(450.0, 650.0, 110, 0.4f)
    }

    fun playMagicBreak() {
        GlobalScope.launch {
            playSweepTone(700.0, 300.0, 160, 0.48f)
            kotlinx.coroutines.delay(50)
            playSweepTone(280.0, 950.0, 250, 0.5f)
        }
    }

    fun playGoalChime() {
        GlobalScope.launch {
            playTone(880.00, 60, 0.38f)
            kotlinx.coroutines.delay(50)
            playTone(1109.73, 60, 0.42f)
            kotlinx.coroutines.delay(50)
            playTone(1318.51, 120, 0.45f)
        }
    }

    fun playMatch4Burst() {
        playSweepTone(800.0, 350.0, 180, 0.5f)
    }

    fun playMatch5Sparkle() {
        GlobalScope.launch {
            val notes = listOf(587.33, 698.46, 880.00, 1174.66)
            for (note in notes) {
                playTone(note, 90, 0.35f)
                kotlinx.coroutines.delay(50)
            }
        }
    }

    fun playCombosPitchIncrease(comboIndex: Int) {
        val baseMultiplier = 1.0 + (comboIndex * 0.15)
        playTone(600.0 * baseMultiplier, 120, 0.4f)
    }

    fun playStripedLaser() {
        playSweepTone(300.0, 1500.0, 220, 0.45f)
    }

    fun playWrappedExplosion() {
        playSweepTone(140.0, 45.0, 320, 0.65f)
    }

    fun playColorBombRainbow() {
        GlobalScope.launch {
            val freqs = listOf(523.25, 587.33, 659.25, 698.46, 783.99, 880.00, 987.77, 1046.50)
            for (f in freqs) {
                playTone(f, 50, 0.32f)
                kotlinx.coroutines.delay(45)
            }
        }
    }

    fun playTntFuse() {
        playSweepTone(1300.0, 950.0, 250, 0.18f)
    }

    fun playTntExplosion() {
        playSweepTone(120.0, 40.0, 450, 0.7f)
    }

    fun playDupeBomb() {
        playSweepTone(400.0, 800.0, 300, 0.4f)
    }

    fun playSoftClick() {
        playTone(650.0, 50, 0.25f)
    }

    fun playButtonPress() {
        playTone(550.0, 70, 0.38f)
    }

    fun playMenuWhoosh() {
        playSweepTone(200.0, 600.0, 150, 0.33f)
    }

    fun playRewardCollection() {
        GlobalScope.launch {
            for (i in 1..6) {
                playTone(1046.50 + i * 150, 80, 0.35f)
                kotlinx.coroutines.delay(65)
            }
        }
    }

    fun playStarEarned() {
        playSweepTone(800.0, 1800.0, 280, 0.4f)
    }

    fun playObjectiveComplete() {
        GlobalScope.launch {
            playTone(523.25, 80, 0.4f)
            kotlinx.coroutines.delay(80)
            playTone(659.25, 80, 0.4f)
            kotlinx.coroutines.delay(80)
            playTone(783.99, 150, 0.45f)
        }
    }

    fun playLevelStart() {
        GlobalScope.launch {
            playTone(523.25, 120, 0.35f)
            kotlinx.coroutines.delay(120)
            playTone(659.25, 120, 0.35f)
            kotlinx.coroutines.delay(120)
            playTone(783.99, 120, 0.35f)
            kotlinx.coroutines.delay(120)
            playTone(1046.50, 250, 0.45f)
        }
    }

    fun playBossRoar() {
        playSweepTone(110.0, 50.0, 600, 0.65f)
    }

    fun playBossDefeated() {
        playVictoryFanfare()
    }

    fun playVictoryFanfare() {
        GlobalScope.launch {
            val fan = listOf(523.25, 523.25, 523.25, 523.25, 659.25, 587.33, 659.25, 783.99, 1046.50)
            val durs = listOf(130, 130, 130, 260, 260, 130, 130, 130, 500)
            for (i in fan.indices) {
                playTone(fan[i], durs[i], 0.45f)
                kotlinx.coroutines.delay(durs[i].toLong() + 15)
            }
        }
    }

    fun playDefeatMelody() {
        GlobalScope.launch {
            val def = listOf(392.00, 349.23, 311.13, 261.63)
            for (f in def) {
                playTone(f, 220, 0.42f)
                kotlinx.coroutines.delay(240)
            }
        }
    }

    private var musicJob: kotlinx.coroutines.Job? = null
    fun startMusicLoop() {
        if (musicJob != null) return
        musicJob = GlobalScope.launch(Dispatchers.Default) {
            val notes = listOf(523.25, 587.33, 659.25, 783.99, 880.00) // C5 major pentatonic scale
            while (true) {
                if (musicEnabled) {
                    for (note in notes) {
                        if (!musicEnabled) break
                        playTone(note, 200, volume = 0.03f)
                        kotlinx.coroutines.delay(450)
                    }
                    kotlinx.coroutines.delay(4000)
                } else {
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }
}
