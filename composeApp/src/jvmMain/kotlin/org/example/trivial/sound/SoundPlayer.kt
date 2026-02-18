package org.example.trivial.sound

import javazoom.jl.player.Player
import java.io.BufferedInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

object SoundPlayer {
    
    /**
     * Reproduce un archivo MP3 como efecto de sonido
     */
    private fun playSound(resourcePath: String) {
        Thread {
            try {
                val inputStream = this::class.java.getResourceAsStream(resourcePath)
                if (inputStream != null) {
                    val buffered = BufferedInputStream(inputStream)
                    val player = Player(buffered)
                    player.play()
                    player.close()
                } else {
                    // Si no existe el archivo MP3, usar tono de respaldo
                    when {
                        resourcePath.contains("correct") -> playToneFallback(800.0, 100)
                        resourcePath.contains("incorrect") -> playToneFallback(200.0, 300)
                    }
                }
            } catch (e: Exception) {
                // Ignorar errores de audio
            }
        }.start()
    }
    
    /**
     * Tono de respaldo si no hay archivos MP3
     */
    private fun playToneFallback(frequency: Double, duration: Int) {
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
                val sample = (Math.sin(angle) * 0.3 * Short.MAX_VALUE).toInt().toShort()
                buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
            }

            line.write(buffer, 0, buffer.size)
            line.drain()
            line.close()
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
    
    // Sonidos del juego
    fun playCorrect() {
        playSound("/sounds/correct.mp3")
    }
    
    fun playIncorrect() {
        playSound("/sounds/incorrect.mp3")
    }
}
