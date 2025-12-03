package com.fcc.agro

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AgroDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla Productos
        val CREATE_PRODUCTS = ("CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_PURCHASE_PRICE + " REAL,"
                + COLUMN_STOCK + " INTEGER,"
                + COLUMN_UNIT + " TEXT,"
                + COLUMN_EXPIRATION + " TEXT" + ")")
        db.execSQL(CREATE_PRODUCTS)

        // Tabla Ventas (NUEVA)
        val CREATE_SALES = ("CREATE TABLE " + TABLE_SALES + "("
                + COLUMN_SALE_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SALE_DATE + " TEXT,"
                + COLUMN_SALE_TIME + " TEXT,"
                + COLUMN_SALE_METHOD + " TEXT,"
                + COLUMN_SALE_TOTAL + " REAL,"
                + COLUMN_SALE_DETAILS + " TEXT" + ")")
        db.execSQL(CREATE_SALES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Borramos ambas tablas si cambia la versión
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALES")
        onCreate(db)
    }

    // Método para registrar venta (NUEVO)
    fun addSale(date: String, time: String, method: String, total: Double, details: String) {
        val values = ContentValues()
        values.put(COLUMN_SALE_DATE, date)
        values.put(COLUMN_SALE_TIME, time)
        values.put(COLUMN_SALE_METHOD, method)
        values.put(COLUMN_SALE_TOTAL, total)
        values.put(COLUMN_SALE_DETAILS, details)
        writableDatabase.insert(TABLE_SALES, null, values)
        writableDatabase.close()
    }

    companion object {
        private const val DATABASE_VERSION = 4 // ¡Versión 4!
        private const val DATABASE_NAME = "agroDB.db"

        // Productos
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "productname"
        const val COLUMN_PRICE = "productprice"
        const val COLUMN_PURCHASE_PRICE = "productpurchaseprice"
        const val COLUMN_STOCK = "productstock"
        const val COLUMN_UNIT = "productunit"
        const val COLUMN_EXPIRATION = "productexp"

        // Ventas (Constantes Nuevas)
        const val TABLE_SALES = "sales"
        const val COLUMN_SALE_ID = "_id"
        const val COLUMN_SALE_DATE = "saledate"
        const val COLUMN_SALE_TIME = "saletime"
        const val COLUMN_SALE_METHOD = "salemethod" // Efectivo, Tarjeta, etc.
        const val COLUMN_SALE_TOTAL = "saletotal"
        const val COLUMN_SALE_DETAILS = "saledetails" // Resumen de productos
    }
}