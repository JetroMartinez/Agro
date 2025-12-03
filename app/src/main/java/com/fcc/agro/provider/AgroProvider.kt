package com.fcc.agro.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.fcc.agro.AgroDBHandler

class AgroProvider : ContentProvider() {

    private var myDB: AgroDBHandler? = null

    // Inicialización de UriMatcher
    // IMPORTANTE: Asegúrate de que sURIMatcher se inicializa aquí o en un bloque init {}
    private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PRODUCTS_TABLE, PRODUCTS)
        addURI(AUTHORITY, "$PRODUCTS_TABLE/#", PRODUCT_ID)
    }

    override fun onCreate(): Boolean {
        myDB = context?.let { AgroDBHandler(it, null, null, 1) }
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val queryBuilder = android.database.sqlite.SQLiteQueryBuilder()
        queryBuilder.tables = AgroDBHandler.TABLE_PRODUCTS

        when (sURIMatcher.match(uri)) {
            // Si es un ID específico
            PRODUCT_ID -> queryBuilder.appendWhere(AgroDBHandler.COLUMN_ID + "=" + uri.lastPathSegment)
            // Si es la tabla completa
            PRODUCTS -> { }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val cursor = queryBuilder.query(myDB?.readableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = myDB!!.writableDatabase
        val id: Long

        when (uriType) {
            PRODUCTS -> {
                id = sqlDB.insert(AgroDBHandler.TABLE_PRODUCTS, null, values)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri") // Aquí es donde estaba fallando
        }

        context?.contentResolver?.notifyChange(uri, null)
        return Uri.parse("$PRODUCTS_TABLE/$id")
    }

    // ... (delete y update siguen la misma lógica con el 'when') ...
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted: Int

        when (uriType) {
            PRODUCTS -> rowsDeleted = sqlDB.delete(AgroDBHandler.TABLE_PRODUCTS, selection, selectionArgs)
            PRODUCT_ID -> {
                val id = uri.lastPathSegment
                rowsDeleted = if (selection.isNullOrEmpty()) {
                    sqlDB.delete(AgroDBHandler.TABLE_PRODUCTS, "${AgroDBHandler.COLUMN_ID}=$id", null)
                } else {
                    sqlDB.delete(AgroDBHandler.TABLE_PRODUCTS, "${AgroDBHandler.COLUMN_ID}=$id and $selection", selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsUpdated: Int

        when (uriType) {
            PRODUCTS -> rowsUpdated = sqlDB.update(AgroDBHandler.TABLE_PRODUCTS, values, selection, selectionArgs)
            PRODUCT_ID -> {
                val id = uri.lastPathSegment
                rowsUpdated = if (selection.isNullOrEmpty()) {
                    sqlDB.update(AgroDBHandler.TABLE_PRODUCTS, values, "${AgroDBHandler.COLUMN_ID}=$id", null)
                } else {
                    sqlDB.update(AgroDBHandler.TABLE_PRODUCTS, values, "${AgroDBHandler.COLUMN_ID}=$id and $selection", selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun getType(uri: Uri): String? = null

    companion object {
        const val AUTHORITY = "com.fcc.agro.provider" // Debe coincidir con el Manifest
        const val PRODUCTS_TABLE = "products" // Debe coincidir con AgroDBHandler
        const val PRODUCTS = 1
        const val PRODUCT_ID = 2

        // ¡OJO! Esto es importante para el ContentResolver
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PRODUCTS_TABLE")
    }
}