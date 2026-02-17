package org.example.trivial.sound

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

object SoundPlayer {

    private fun playTone(frequency: Double, duration: Int, volume: Double = 0.5) {
        try {
            val sampleRate = 44100f
            val format = AudioFormat(sampleRate, 16, 1, true, false)
            val line: SourceDataLine = AudioSystem.getSourceDataLine(format)
            line.open(format)
            line.start()

            val samples = (sampleRate * duration / 1000).toInt()
            val buffer = ByteArray(samples * 2)

            for (i in 0 until samples) {
                val angle = 2.0 * Math.PI * i * frequency / sampleRate
                val sample = (Math.sin(angle) * volume * Short.MAX_VALUE).toInt().toShort()
                buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }

            line.write(buffer, 0, buffer.size)
            line.drain()
            line.close()
        } catch (e: Exception) {
            // Silenciosamente ignorar errores de audio
        }
    }

    fun playCorrect() {
        Thread {
            playTone(800.0, 100, 0.3)  // Do alto
            Thread.sleep(50)
            playTone(1000.0, 150, 0.3) // Mi más alto
        }.start()
    }

    fun playIncorrect() {
        Thread {
            playTone(200.0, 300, 0.4)  // Sonido grave y corto
        }.start()
    }

    fun playStreak() {
        Thread {
            playTone(600.0, 80, 0.25)
            Thread.sleep(40)
            playTone(800.0, 80, 0.25)
            Thread.sleep(40)
            playTone(1000.0, 120, 0.25)
        }.start()
    }
}
