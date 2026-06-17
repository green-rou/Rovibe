package com.greenrou.rovibe.data.sound

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VoiceRecorder(context: Context) {

    private val dir = context.filesDir.resolve("voice").also { it.mkdirs() }
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    @Volatile private var recording = false

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    @SuppressLint("MissingPermission")
    fun start(id: String) {
        stop()
        val bufferSize = AudioRecord.getMinBufferSize(
            WaveformGenerator.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ) * 2

        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            WaveformGenerator.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )
        recorder = record
        recording = true
        _isRecording.value = true
        _elapsedMs.value = 0L

        val file = getFile(id)
        record.startRecording()
        val startTime = System.currentTimeMillis()

        recordingThread = Thread {
            val buffer = ShortArray(bufferSize / 2)
            DataOutputStream(BufferedOutputStream(file.outputStream())).use { out ->
                while (recording) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val bytes = ByteBuffer.allocate(read * 2).order(ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until read) bytes.putShort(buffer[i])
                        out.write(bytes.array())
                        _elapsedMs.value = System.currentTimeMillis() - startTime
                    }
                }
            }
        }.also { it.start() }
    }

    fun stop() {
        recording = false
        recordingThread?.join(1000)
        recordingThread = null
        recorder?.stop()
        recorder?.release()
        recorder = null
        _isRecording.value = false
    }

    fun delete(id: String) {
        getFile(id).delete()
    }

    fun cleanupOrphaned(referencedIds: Set<String>) {
        dir.listFiles()?.forEach { file ->
            if (file.extension == "pcm" && file.nameWithoutExtension !in referencedIds) {
                file.delete()
            }
        }
    }

    fun getFile(id: String): File = dir.resolve("$id.pcm")

    fun loadPcmSamples(id: String): ShortArray {
        val file = getFile(id)
        if (!file.exists() || file.length() == 0L) return ShortArray(0)
        val bytes = file.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val samples = ShortArray(bytes.size / 2)
        buffer.asShortBuffer().get(samples)
        return samples
    }

    fun durationMs(id: String): Long {
        val file = getFile(id)
        if (!file.exists()) return 0L
        return file.length() / 2 * 1000 / WaveformGenerator.SAMPLE_RATE
    }
}
