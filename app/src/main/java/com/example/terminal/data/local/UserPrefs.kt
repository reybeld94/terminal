package com.example.terminal.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "user_prefs"
private val LAST_EMPLOYEE_KEY = stringPreferencesKey("last_employee_id")

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class UserPrefs private constructor(private val appContext: Context) {

    val lastEmployeeId: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[LAST_EMPLOYEE_KEY]
        }

    suspend fun saveLastEmployeeId(employeeId: String) {
        appContext.dataStore.edit { preferences ->
            preferences[LAST_EMPLOYEE_KEY] = employeeId
        }
    }

    companion object {
        fun create(context: Context): UserPrefs {
            return UserPrefs(context.applicationContext)
        }
    }
}
