package com.example.di

/**
 * P4-2: Hilt stub — when Hilt added, replace AppContainer with:
 *   @Module @InstallIn(SingletonComponent::class) object AppModule {
 *     @Provides @Singleton fun provideAppDao(db: AppDatabase) = db.appDao()
 *   }
 *   @HiltViewModel class JamaatyViewModel @Inject constructor(...) : ViewModel()
 * Keep manual AppContainer for now to avoid ksp/hilt plugin overhead on CI.
 */
object HiltModuleStub
