package com.fcc.agro

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.agro.provider.AgroProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgroViewModel(application: Application) : AndroidViewModel(application) {

    // Lista de productos
    private val _productsFlow = MutableSharedFlow<List<Product>>(replay = 1)
    val productsFlow = _productsFlow.asSharedFlow()

    // CARRITO: Mapa mutable (ID Producto -> Cantidad)
    // Usamos StateFlow para que la UI reaccione a cambios en el carrito
    private val _cartFlow = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cartFlow = _cartFlow.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // TOTAL A PAGAR

    private val _totalFlow = MutableStateFlow(0.0)
    val totalFlow = _totalFlow.asStateFlow()

    private var allProductsCache = listOf<Product>() // Cache para búsquedas

    fun loadProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = getApplication<Application>().contentResolver.query(
                AgroProvider.CONTENT_URI, null, null, null, null
            )
            val list = ArrayList<Product>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Leemos los datos de la base de datos
                    val id = cursor.getInt(0)
                    val name = cursor.getString(1)
                    val price = cursor.getDouble(2)
                    val purchasePrice = cursor.getDouble(3)
                    val stock = cursor.getInt(4)
                    val unit = cursor.getString(5)
                    val expDate = cursor.getString(6)

                    // AQUÍ ESTABA EL ERROR: Usamos argumentos nombrados para evitar fallos de orden
                    list.add(
                        Product(
                            id = id,
                            name = name,
                            price = price,
                            purchasePrice = purchasePrice,
                            stock = stock,
                            unit = unit,
                            expirationDate = expDate
                        )
                    )
                } while (cursor.moveToNext())
                cursor.close()
            }
            _productsFlow.emit(list)
        }
    }

    // Filtrar productos por nombre (Búsqueda)
    fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.trim().isEmpty()) {
                // Si no hay texto, la lista se vacía (Requerimiento cumplido)
                _searchResults.value = emptyList()
            } else {
                // Filtramos sobre la memoria caché
                val filtered = allProductsCache.filter {
                    it.name?.contains(query, ignoreCase = true) == true
                }
                _searchResults.value = filtered
            }
        }
    }

    // Actualizar cantidad en el carrito
    fun updateCart(product: Product, quantity: Int) {
        val currentCart = _cartFlow.value.toMutableMap()
        if (quantity > 0) {
            currentCart[product.id] = quantity
        } else {
            currentCart.remove(product.id)
        }
        _cartFlow.value = currentCart
        calculateTotal()
    }

    private fun calculateTotal() {
        var total = 0.0
        val cart = _cartFlow.value
        // Recorremos el cache para obtener precios
        allProductsCache.forEach { product ->
            val qty = cart[product.id] ?: 0
            if (qty > 0) {
                total += product.price * qty
            }
        }
        _totalFlow.value = total
    }

    // FINALIZAR VENTA
    fun checkout(paymentMethod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val dbHandler = AgroDBHandler(context, null, null, 1) // Versión se maneja interna
            val cart = _cartFlow.value
            val detailsBuilder = StringBuilder()
            var finalTotal = 0.0

            // 1. Restar Stock y Construir Detalle
            cart.forEach { (id, qty) ->
                val product = allProductsCache.find { it.id == id }
                if (product != null) {
                    // Restar stock
                    val newStock = product.stock - qty
                    val values = ContentValues()
                    values.put(AgroDBHandler.COLUMN_STOCK, newStock)
                    context.contentResolver.update(
                        AgroProvider.CONTENT_URI, values,
                        "${AgroDBHandler.COLUMN_ID}=?", arrayOf(id.toString())
                    )

                    // Agregar a detalle
                    detailsBuilder.append("${qty}x ${product.name} ($${product.price}); ")
                    finalTotal += (product.price * qty)
                }
            }

            // 2. Obtener Fecha y Hora
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            // 3. Guardar en Tabla Ventas (Usando el DBHandler directamente para métodos custom)
            dbHandler.addSale(currentDate, currentTime, paymentMethod, finalTotal, detailsBuilder.toString())

            // 4. Limpiar carrito y recargar
            _cartFlow.value = emptyMap()
            _totalFlow.value = 0.0
            loadProducts()
        }
    }

    // Mantenemos addProduct igual que antes...
    fun addProduct(name: String, price: Double, pPrice: Double, stock: Int, unit: String, exp: String) {
        // ... (código existente) ...
        viewModelScope.launch(Dispatchers.IO) {
            val values = ContentValues()
            values.put(AgroDBHandler.COLUMN_NAME, name)
            values.put(AgroDBHandler.COLUMN_PRICE, price)
            values.put(AgroDBHandler.COLUMN_PURCHASE_PRICE, pPrice)
            values.put(AgroDBHandler.COLUMN_STOCK, stock)
            values.put(AgroDBHandler.COLUMN_UNIT, unit)
            values.put(AgroDBHandler.COLUMN_EXPIRATION, exp)
            getApplication<Application>().contentResolver.insert(AgroProvider.CONTENT_URI, values)
            loadProducts()
        }
    }

    fun sellProduct(product: Product, quantityToSell: Int) {
        if (product.stock >= quantityToSell) {
            viewModelScope.launch(Dispatchers.IO) {
                val newStock = product.stock - quantityToSell

                // 2. Preparar valores para actualizar
                val values = ContentValues()
                values.put(AgroDBHandler.COLUMN_STOCK, newStock)

                // 3. Ejecutar actualización en la BD
                getApplication<Application>().contentResolver.update(
                    AgroProvider.CONTENT_URI,
                    values,
                    "${AgroDBHandler.COLUMN_ID}=?",
                    arrayOf(product.id.toString())
                )

                // 4. (Opcional) Registrar en historial como "Venta Rápida"
                // Si quieres que esto también se guarde en el historial, descomenta lo siguiente:
                /*
                val dbHandler = AgroDBHandler(getApplication(), null, null, 1)
                val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                dbHandler.addSale(date, time, "Venta Rápida", product.price * quantityToSell, "1x ${product.name}")
                */

                // 5. Recargar la lista para ver el cambio de stock
                loadProducts()
            }
        }
    }
}