package com.example.coffelists
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Extension pro snadný přístup k DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "coffee_data")

class CoffeeFileWork(private val context: Context) {
    // Klíč pro uložení seznamu káv
    private val COFFEES_KEY = stringPreferencesKey("coffees_list")

    // JSON serializer
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // Načti všechny kávy
    suspend fun getAllCoffees(): List<Coffee> {
        val preferences = context.dataStore.data.first()
        val coffeesJson = preferences[COFFEES_KEY] ?: return emptyList()

        return try {
            json.decodeFromString<List<Coffee>>(coffeesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    // Aktualizuj kávu
    suspend fun updateCoffee(updatedCoffee: Coffee) {
        val currentCoffees = getAllCoffees().toMutableList()
        val index = currentCoffees.indexOfFirst { it.id == updatedCoffee.id }
        if (index != -1) {
            currentCoffees[index] = updatedCoffee
            saveCoffees(currentCoffees)
        }
    }

    // Ulož kávu (přidá do seznamu)
    suspend fun addCoffee(coffee: Coffee) {
        val currentCoffees = getAllCoffees().toMutableList()
        currentCoffees.add(coffee)
        saveCoffees(currentCoffees)
    }

    // Smaž kávu
    suspend fun deleteCoffee(coffeeId: String) {
        val currentCoffees = getAllCoffees().toMutableList()
        currentCoffees.removeAll { it.id == coffeeId }
        saveCoffees(currentCoffees)
    }

    // Ulož celý seznam
    private suspend fun saveCoffees(coffees: List<Coffee>) {
        val coffeesJson = json.encodeToString(coffees)
        context.dataStore.edit { preferences ->
            preferences[COFFEES_KEY] = coffeesJson
        }
    }
}