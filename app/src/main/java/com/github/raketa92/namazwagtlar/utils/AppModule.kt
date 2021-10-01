package com.github.raketa92.namazwagtlar.utils

import android.content.Context
import androidx.room.Room
import com.github.raketa92.namazwagtlar.dao.NamazDao
import com.github.raketa92.namazwagtlar.db.NamazDB
import com.github.raketa92.namazwagtlar.repository.NamazRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideNamazDao(appDatabase: NamazDB): NamazDao {
        return appDatabase.namazDao()
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): NamazDB {
        return Room.databaseBuilder(appContext, NamazDB::class.java, "namaz_database").build()
    }

    @Singleton
    @Provides
    fun provideNamazRepo(namazDao: NamazDao): NamazRepo {
        return NamazRepo(namazDao)
    }
}