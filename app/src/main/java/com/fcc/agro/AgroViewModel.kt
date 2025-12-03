package com.fcc.agro

import android.app.Application
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fcc.agro.provider.AgroProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow // CAMBIO IMPORTANTE
import kotlinx.coroutines.flow.asStateFlow    // CAMBIO IMPORTANTE
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AgroViewModel(application: Application) : AndroidViewModel(application) {

    // CAMBIO: Usamos StateFlow para que siempre recuerde la última lista cargada
    // Iniciamos con una lista vacía
    private val _productsFlow = MutableStateFlow<List<Product>>(emptyList())
    val productsFlow = _productsFlow.asStateFlow()

    // CARRITO
    private val _cartFlow = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cartFlow = _cartFlow.asStateFlow()

    // TOTAL
    private val _totalFlow = MutableStateFlow(0.0)
    val totalFlow = _totalFlow.asStateFlow()

    private var allProductsCache = listOf<Product>()

    fun loadProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = getApplication<Application>().contentResolver.query(
                AgroProvider.CONTENT_URI, null, null, null, null
            )
            val list = ArrayList<Product>()

            // Verificamos que el cursor no sea nulo y tenga datos
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Asegúrate que estos índices coincidan con tu AgroDBHandler v5
                    // 0:_id, 1:name, 2:price, 3:p_price, 4:stock, 5:unit, 6:exp, 7:image
                    try {
                        val id = cursor.getInt(0)
                        val name = cursor.getString(1)
                        val price = cursor.getDouble(2)
                        val pPrice = cursor.getDouble(3)
                        val stock = cursor.getInt(4)
                        val unit = cursor.getString(5)
                        val exp = cursor.getString(6)
                        val img = cursor.getString(7) // La foto

                        list.add(Product(id, name, price, pPrice, stock, unit, exp, img))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }

            allProductsCache = list
            // CAMBIO: Con StateFlow usamos .value en lugar de .emit() (aunque ambos funcionan en corrutinas)
            _productsFlow.value = list
        }
    }

    // ... El resto de tus funciones (addProduct, searchProducts, etc) siguen igual ...

    fun addProduct(name: String, price: Double, pPrice: Double, stock: Int, unit: String, exp: String, imgUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val values = ContentValues()
            values.put(AgroDBHandler.COLUMN_NAME, name)
            values.put(AgroDBHandler.COLUMN_PRICE, price)
            values.put(AgroDBHandler.COLUMN_PURCHASE_PRICE, pPrice)
            values.put(AgroDBHandler.COLUMN_STOCK, stock)
            values.put(AgroDBHandler.COLUMN_UNIT, unit)
            values.put(AgroDBHandler.COLUMN_EXPIRATION, exp)
            values.put(AgroDBHandler.COLUMN_IMAGE, imgUri)

            getApplication<Application>().contentResolver.insert(AgroProvider.CONTENT_URI, values)

            // IMPORTANTE: Recargar inmediatamente después de insertar
            loadProducts()
        }
    }

    // ... (Mantén aquí searchProducts, updateCart, calculateTotal y checkout igual que antes) ...
    // Asegúrate de incluir las funciones que te di en los pasos anteriores.
    fun searchProducts(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                _productsFlow.value = allProductsCache
            } else {
                val filtered = allProductsCache.filter {
                    it.name?.contains(query, ignoreCase = true) == true
                }
                _productsFlow.value = filtered
            }
        }
    }

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
        allProductsCache.forEach { product ->
            val qty = cart[product.id] ?: 0
            if (qty > 0) {
                total += product.price * qty
            }
        }
        _totalFlow.value = total
    }

    fun checkout(paymentMethod: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val dbHandler = AgroDBHandler(context, null, null, 1)
            val cart = _cartFlow.value
            val detailsBuilder = StringBuilder()
            var finalTotal = 0.0

            cart.forEach { (id, qty) ->
                val product = allProductsCache.find { it.id == id }
                if (product != null) {
                    val newStock = product.stock - qty
                    val values = ContentValues()
                    values.put(AgroDBHandler.COLUMN_STOCK, newStock)
                    context.contentResolver.update(
                        AgroProvider.CONTENT_URI, values,
                        "${AgroDBHandler.COLUMN_ID}=?", arrayOf(id.toString())
                    )
                    detailsBuilder.append("${qty}x ${product.name}; ")
                    finalTotal += (product.price * qty)
                }
            }

            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            dbHandler.addSale(currentDate, currentTime, paymentMethod, finalTotal, detailsBuilder.toString())

            _cartFlow.value = emptyMap()
            _totalFlow.value = 0.0
            loadProducts()
        }
    }

    fun sellProduct(product: Product, quantityToSell: Int) {
        if (product.stock >= quantityToSell) {
            viewModelScope.launch(Dispatchers.IO) {
                val newStock = product.stock - quantityToSell
                val values = ContentValues()
                values.put(AgroDBHandler.COLUMN_STOCK, newStock)
                val selection = "${AgroDBHandler.COLUMN_ID} = ?"
                val selectionArgs = arrayOf(product.id.toString())
                getApplication<Application>().contentResolver.update(
                    AgroProvider.CONTENT_URI, values, selection, selectionArgs
                )
                loadProducts()
            }
        }
    }
}