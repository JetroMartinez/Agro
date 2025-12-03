package com.fcc.agro.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.fcc.agro.AgroDBHandler

class AgroProvider : ContentProvider() {

    private var myDB: AgroDBHandler? = null

    // Configuración del UriMatcher
    private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        sURIMatcher.addURI(AUTHORITY, PRODUCTS_TABLE, PRODUCTS)
        sURIMatcher.addURI(AUTHORITY, "$PRODUCTS_TABLE/#", PRODUCT_ID)
    }

    override fun onCreate(): Boolean {
        myDB = context?.let { AgroDBHandler(it, null, null, 1) }
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val queryBuilder = android.database.sqlite.SQLiteQueryBuilder()
        queryBuilder.tables = AgroDBHandler.TABLE_PRODUCTS

        when (sURIMatcher.match(uri)) {
            PRODUCT_ID -> queryBuilder.appendWhere(AgroDBHandler.COLUMN_ID + "=" + uri.lastPathSegment)
            PRODUCTS -> { }
            else -> throw IllegalArgumentException("Unknown URI")
        }

        val cursor = queryBuilder.query(myDB?.readableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val sqlDB = myDB!!.writableDatabase
        val id = sqlDB.insert(AgroDBHandler.TABLE_PRODUCTS, null, values)
        context?.contentResolver?.notifyChange(uri, null)
        return Uri.parse("$PRODUCTS_TABLE/$id")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted = sqlDB.delete(AgroDBHandler.TABLE_PRODUCTS, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val sqlDB = myDB!!.writableDatabase
        val rowsUpdated = sqlDB.update(AgroDBHandler.TABLE_PRODUCTS, values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun getType(uri: Uri): String? = null

    companion object {
        // IMPORTANTE: Esta autoridad debe coincidir con el AndroidManifest
        const val AUTHORITY = "com.fcc.agro.provider"
        private const val PRODUCTS_TABLE = "products"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PRODUCTS_TABLE")
        private const val PRODUCTS = 1
        private const val PRODUCT_ID = 2
    }
}