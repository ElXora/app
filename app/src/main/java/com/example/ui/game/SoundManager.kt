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
    private var vibrator: android.os.Vibrator? = null

    fun initialize(context: Context) {
        val appCtx = context.applicationContext
        appContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appCtx.createAttributionContext("audio")
        } else {
            appCtx
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator ?: (appContext?.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator)
        } else {
            @Suppress("DEPRECATION")
            appContext?.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
    }

    fun vibrate(durationMs: Long) {
        val vib = vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // fail-safe
        }
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
                    // Add warm wooden/hollow harmonics (Fundamental + 2nd harmonic + 3rd harmonic)
                    // This imitates a pleasant physical mallet (marimba) pop in Candy Crush & Royal Kingdom
                    val sampleValue = sin(angle) + 0.35 * sin(2.0 * angle) + 0.15 * sin(3.0 * angle)
                    val finalSample = (sampleValue / 1.5).toFloat()
                    samples[i] = finalSample * volume * envelope
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && appContext != null) {
                    builder.setContext(appContext!!)
                }

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && appContext != null) {
                    builder.setContext(appContext!!)
                }

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
        vibrate(15)
        // A very quick bubbly upward sweep or popping chord
        GlobalScope.launch {
            playTone(880.0, 45, 0.45f)
            kotlinx.coroutines.delay(20)
            playTone(1174.66, 55, 0.45f)
        }
    }

    fun playWoodHit() {
        vibrate(10)
        playTone(320.0, 90, 0.45f)
    }

    fun playWoodBreak() {
        vibrate(25)
        GlobalScope.launch {
            playTone(280.0, 100, 0.5f)
            kotlinx.coroutines.delay(40)
            playSweepTone(200.0, 80.0, 150, 0.55f)
        }
    }

    fun playStoneHit() {
        vibrate(15)
        playTone(180.0, 110, 0.52f)
    }

    fun playStoneBreak() {
        vibrate(50)
        GlobalScope.launch {
            playTone(150.0, 140, 0.6f)
            kotlinx.coroutines.delay(50)
            playSweepTone(140.0, 40.0, 280, 0.65f)
        }
    }

    fun playIceHit() {
        vibrate(8)
        playTone(1100.0, 60, 0.42f)
    }

    fun playIceBreak() {
        vibrate(20)
        GlobalScope.launch {
            playTone(1250.0, 80, 0.45f)
            kotlinx.coroutines.delay(30)
            playSweepTone(1400.0, 800.0, 200, 0.5f)
        }
    }

    fun playChainHit() {
        vibrate(12)
        playTone(850.0, 70, 0.38f)
    }

    fun playChainBreak() {
        vibrate(30)
        GlobalScope.launch {
            playTone(920.0, 60, 0.42f)
            kotlinx.coroutines.delay(40)
            playSweepTone(880.0, 1600.0, 150, 0.45f)
        }
    }

    fun playMagicHit() {
        vibrate(15)
        playSweepTone(450.0, 650.0, 110, 0.4f)
    }

    fun playMagicBreak() {
        vibrate(40)
        GlobalScope.launch {
            playSweepTone(700.0, 300.0, 160, 0.48f)
            kotlinx.coroutines.delay(50)
            playSweepTone(280.0, 950.0, 250, 0.5f)
        }
    }

    fun playGoalChime() {
        vibrate(25)
        GlobalScope.launch {
            playTone(880.00, 60, 0.38f)
            kotlinx.coroutines.delay(50)
            playTone(1109.73, 60, 0.42f)
            kotlinx.coroutines.delay(50)
            playTone(1318.51, 120, 0.45f)
        }
    }

    fun playMatch4Burst() {
        vibrate(40)
        GlobalScope.launch {
            playSweepTone(880.0, 440.0, 160, 0.45f)
            playSweepTone(523.25, 261.63, 200, 0.4f)
        }
    }

    fun playMatch5Sparkle() {
        vibrate(60)
        GlobalScope.launch {
            val notes = listOf(587.33, 698.46, 880.00, 1174.66)
            for (note in notes) {
                playTone(note, 90, 0.35f)
                kotlinx.coroutines.delay(50)
            }
        }
    }

    fun playCombosPitchIncrease(comboIndex: Int) {
        vibrate(20)
        val baseMultiplier = 1.0 + (comboIndex * 0.12)
        GlobalScope.launch {
            playTone(523.25 * baseMultiplier, 90, 0.4f)
            kotlinx.coroutines.delay(40)
            playTone(659.25 * baseMultiplier, 110, 0.42f)
        }
    }

    fun playStripedLaser() {
        vibrate(50)
        playSweepTone(300.0, 1500.0, 220, 0.45f)
    }

    fun playWrappedExplosion() {
        vibrate(100)
        playSweepTone(140.0, 45.0, 320, 0.65f)
    }

    fun playColorBombRainbow() {
        vibrate(80)
        GlobalScope.launch {
            val freqs = listOf(523.25, 587.33, 659.25, 698.46, 783.99, 880.00, 987.77, 1046.50)
            for (f in freqs) {
                playTone(f, 50, 0.32f)
                kotlinx.coroutines.delay(45)
            }
        }
    }

    fun playTntFuse() {
        vibrate(20)
        playSweepTone(1300.0, 950.0, 250, 0.18f)
    }

    fun playTntExplosion() {
        vibrate(150)
        GlobalScope.launch {
            playSweepTone(140.0, 30.0, 450, 0.85f)
            kotlinx.coroutines.delay(50)
            playSweepTone(100.0, 20.0, 400, 0.8f)
            kotlinx.coroutines.delay(40)
            playTone(45.0, 500, 0.9f)
        }
    }

    fun playDupeBomb() {
        vibrate(35)
        playSweepTone(400.0, 800.0, 300, 0.4f)
    }

    var isBossFightActive = false

    fun playSoftClick() {
        vibrate(5)
        playTone(710.0, 40, 0.45f) // high crisp bubble-glass pop
    }

    fun playButtonPress() {
        vibrate(12)
        // beautiful woody positive double-sound chord
        GlobalScope.launch {
            playTone(523.25, 55, 0.42f)
            kotlinx.coroutines.delay(15)
            playTone(659.25, 65, 0.45f)
        }
    }

    fun playMenuWhoosh() {
        playSweepTone(200.0, 600.0, 150, 0.33f)
    }

    fun playRewardCollection() {
        vibrate(20)
        GlobalScope.launch {
            for (i in 1..6) {
                playTone(1046.50 + i * 150, 80, 0.35f)
                kotlinx.coroutines.delay(65)
            }
        }
    }

    fun playStarEarned() {
        vibrate(40)
        playSweepTone(800.0, 1800.0, 280, 0.4f)
    }

    fun playObjectiveComplete() {
        vibrate(75)
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
        vibrate(120)
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
            while (true) {
                if (musicEnabled) {
                    if (isBossFightActive) {
                        // Procedural intense boss fight synth music: tense chromatic and minor progression in lower-middle keys
                        val bossMelody = listOf(
                            Pair(146.83, 160), // D3
                            Pair(174.61, 160), // F3
                            Pair(220.00, 180), // A3
                            Pair(207.65, 180), // G#3 (tense chromatic pitch!)
                            Pair(146.83, 160), // D3
                            Pair(196.00, 160), // G3
                            Pair(185.00, 200), // F#3
                            Pair(130.81, 240)  // low C3 tension root
                        )
                        for (note in bossMelody) {
                            if (!musicEnabled || !isBossFightActive) break
                            playTone(note.first, note.second, volume = 0.045f)
                            kotlinx.coroutines.delay(260)
                        }
                        kotlinx.coroutines.delay(350)
                    } else {
                        // Relaxing, joyful adventure background exploration music
                        val normalMelody = listOf(523.25, 659.25, 587.33, 698.46, 659.25, 783.99, 880.00)
                        for (note in normalMelody) {
                            if (!musicEnabled || isBossFightActive) break
                            playTone(note, 210, volume = 0.02f)
                            kotlinx.coroutines.delay(480)
                        }
                        kotlinx.coroutines.delay(2800)
                    }
                } else {
                    kotlinx.coroutines.delay(1000)
                }
            }
        }
    }
}
