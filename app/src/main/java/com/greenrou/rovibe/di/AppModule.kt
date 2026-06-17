package com.greenrou.rovibe.di

import com.greenrou.rovibe.data.SoundItemStore
import com.greenrou.rovibe.data.SoundRepository
import com.greenrou.rovibe.data.composition.CompositionRepository
import com.greenrou.rovibe.data.composition.CompositionStore
import com.greenrou.rovibe.data.sound.AudioTrackSoundEngine
import com.greenrou.rovibe.data.sound.SoundCommandRepository
import com.greenrou.rovibe.data.sound.SoundEngine
import com.greenrou.rovibe.ui.screen.composition_editor.CompositionEditorViewModel
import com.greenrou.rovibe.ui.screen.compositions.CompositionsViewModel
import com.greenrou.rovibe.ui.screen.create.CreateViewModel
import com.greenrou.rovibe.ui.screen.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SoundItemStore(androidContext()) }
    single { SoundRepository(get()) }
    single<SoundEngine> { AudioTrackSoundEngine() }
    single { SoundCommandRepository(get()) }
    single { CompositionStore(androidContext()) }
    single { CompositionRepository(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { (itemId: String?) -> CreateViewModel(get(), get(), itemId) }
    viewModel { CompositionsViewModel(get()) }
    viewModel { (compositionId: String?) -> CompositionEditorViewModel(get(), get(), compositionId) }
}
