package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** One row per (user, data type). The row holds the user's list as JSON. */
@Entity(tableName = "saved_lists")
data class SavedList(
    @PrimaryKey val key: String,
    val json: String
)

@Dao
interface SavedListDao {
    @Query("SELECT json FROM saved_lists WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Upsert
    suspend fun put(item: SavedList)

    @Query("DELETE FROM saved_lists WHERE `key` = :key")
    suspend fun delete(key: String)
}

@Database(entities = [SavedList::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedListDao(): SavedListDao
}

/** Local (on-device) storage for what the user enters. Data is kept per signed-in user. */
object AppStore {
    private var db: AppDatabase? = null
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; coerceInputValues = true }

    fun init(context: Context) {
        if (db == null) {
            db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "lungcare_data.db").build()
        }
    }

    private fun key(name: String): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return "$uid:$name"
    }

    suspend fun <T> load(name: String, serializer: KSerializer<T>): List<T> {
        val raw = db?.savedListDao()?.get(key(name)) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(serializer), raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun <T> save(name: String, serializer: KSerializer<T>, list: List<T>) {
        db?.savedListDao()?.put(SavedList(key(name), json.encodeToString(ListSerializer(serializer), list)))
    }
}
