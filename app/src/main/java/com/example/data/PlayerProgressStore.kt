package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.PlayerProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wordlyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wordly_puzzle_prefs"
)

class PlayerProgressStore(private val context: Context) {

    companion object {
        const val STARTING_COINS = 300
        private val KEY_COINS = intPreferencesKey("coins_balance")
        private val KEY_COMPLETED_CATEGORIES = stringSetPreferencesKey("completed_category_ids")
        private val KEY_IS_MUTED = booleanPreferencesKey("sound_is_muted")
    }

    val progressFlow: Flow<PlayerProgress> = context.wordlyDataStore.data.map { prefs ->
        val coins = prefs[KEY_COINS] ?: STARTING_COINS
        val completedIds = prefs[KEY_COMPLETED_CATEGORIES]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
        val isMuted = prefs[KEY_IS_MUTED] ?: false
        PlayerProgress(
            coins = coins,
            completedCategoryIds = completedIds,
            isMuted = isMuted,
            isLoaded = true,
        )
    }

    suspend fun recordLevelCompleted(categoryId: Int, coinsReward: Int) {
        context.wordlyDataStore.edit { prefs ->
            val currentCoins = prefs[KEY_COINS] ?: STARTING_COINS
            prefs[KEY_COINS] = currentCoins + coinsReward

            val currentCompleted = prefs[KEY_COMPLETED_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            currentCompleted.add(categoryId.toString())
            prefs[KEY_COMPLETED_CATEGORIES] = currentCompleted
        }
    }

    suspend fun setMuted(muted: Boolean) {
        context.wordlyDataStore.edit { prefs ->
            prefs[KEY_IS_MUTED] = muted
        }
    }
}
