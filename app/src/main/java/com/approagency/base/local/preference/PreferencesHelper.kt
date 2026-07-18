package com.approagency.base.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

object PreferencesHelper {


    private lateinit var dataStore: DataStore<Preferences>

    val isInitialized: Boolean
        get() = ::dataStore.isInitialized

    fun init(
        context: Context,
        applicationPackageName: String
    ) {
        if (::dataStore.isInitialized) return

        dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(
                "$applicationPackageName.ds"
            )
        }
    }

    object Keys {
        val THEME_MODE = stringPreferencesKey("THEME_MODE")
        val LANGUAGE = stringPreferencesKey("LANGUAGE")
    }

    fun <T> write(key: Preferences.Key<T>, value: T) {
        check(::dataStore.isInitialized) {
            "PreferencesHelper.init() must be called before using PreferencesHelper"
        }

        runBlocking {
            dataStore.edit {
                it[key] = value
            }
        }
    }

    fun <T> read(key: Preferences.Key<T>): T? {
        check(::dataStore.isInitialized) {
            "PreferencesHelper.init() must be called before using PreferencesHelper"
        }

        return runBlocking {
            dataStore.data
                .map { it[key] }
                .first()
        }
    }

    fun <T> read(key: Preferences.Key<T>, defaultValue: T): T {
        check(::dataStore.isInitialized) {
            "PreferencesHelper.init() must be called before using PreferencesHelper"
        }

        return runBlocking {
            dataStore.data
                .map { it[key] ?: defaultValue }
                .first()
        }
    }

    fun <T> readFlow(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        check(::dataStore.isInitialized) {
            "PreferencesHelper.init() must be called before using PreferencesHelper"
        }

        return dataStore.data.map {
            it[key] ?: defaultValue
        }
    }

    suspend fun remove(
        key: Preferences.Key<*>
    ) {
        check(::dataStore.isInitialized) {
            "PreferencesHelper.init() must be called before using PreferencesHelper"
        }

        dataStore.edit {
            it.remove(key)
        }
    }
}