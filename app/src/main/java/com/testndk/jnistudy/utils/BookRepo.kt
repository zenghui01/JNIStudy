package com.testndk.jnistudy.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.testndk.jnistudy.bean.BookModel
import com.testndk.jnistudy.bean.Type
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

const val KEY_BOOK_NAME = "key_book_name"
const val KEY_BOOK_PRICE = "key_book_price"
const val KEY_BOOK_TYPE = "key_book_type"

val Context.bookDataStorePf: DataStore<Preferences> by preferencesDataStore(name = "pf_datastore")

//SharedPreference文件名
//const val BOOK_PREFERENCES_NAME = "book_preferences"
//val Context.bookDataStorePf: DataStore<Preferences> by preferencesDataStore(
//    name = "pf_datastore", //DataStore文件名称
////将SP迁移到Preference DataStore中
//    produceMigrations = { context ->
//        listOf(SharedPreferencesMigration(context, BOOK_PREFERENCES_NAME))
//    }
//)

object PreferenceKeys {
    val P_KEY_BOOK_NAME = stringPreferencesKey(KEY_BOOK_NAME)
    val P_KEY_BOOK_PRICE = floatPreferencesKey(KEY_BOOK_PRICE)
    val P_KEY_BOOK_TYPE = stringPreferencesKey(KEY_BOOK_TYPE)
}

suspend fun Context.saveBookPf(book: BookModel) {
    bookDataStorePf.edit { preferences ->
        preferences[PreferenceKeys.P_KEY_BOOK_NAME] = book.name
        preferences[PreferenceKeys.P_KEY_BOOK_PRICE] = book.price
        preferences[PreferenceKeys.P_KEY_BOOK_TYPE] = book.type.name
    }
}

suspend fun Context.getBookBf() = bookDataStorePf.data.catch { exception ->
    emit(emptyPreferences())
}.map { preferences ->
    //对应的Key是 Preferences.Key<T>
    val bookName = preferences[PreferenceKeys.P_KEY_BOOK_NAME] ?: ""
    val bookPrice = preferences[PreferenceKeys.P_KEY_BOOK_PRICE] ?: 0f
    val bookType = Type.valueOf(preferences[PreferenceKeys.P_KEY_BOOK_TYPE] ?: Type.MATH.name)
    return@map BookModel(bookName, bookPrice, bookType)
}