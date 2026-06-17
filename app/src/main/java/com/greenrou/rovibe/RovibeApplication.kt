package com.greenrou.rovibe

import android.app.Application
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.sound.VoiceRecorder
import com.greenrou.rovibe.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

class RovibeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RovibeApplication)
            modules(appModule)
        }
        cleanupOrphanedVoiceRecordings()
    }

    private fun cleanupOrphanedVoiceRecordings() {
        val voiceIdRegex = Regex("""\bvoice\((\w+)\)""")
        val repository = get<SoundRepository>(SoundRepository::class.java)
        val voiceRecorder = get<VoiceRecorder>(VoiceRecorder::class.java)
        val referenced = repository.items.value
            .flatMap { voiceIdRegex.findAll(it.content).map { m -> m.groupValues[1] } }
            .toSet()
        voiceRecorder.cleanupOrphaned(referenced)
    }
}
