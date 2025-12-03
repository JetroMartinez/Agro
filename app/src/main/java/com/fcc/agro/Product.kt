package com.fcc.agro

data class Product(
    var id: Int = 0,
    var name: String? = null,
    var price: Double = 0.0,          // Precio Venta
    var purchasePrice: Double = 0.0,  // Precio Compra
    var stock: Int = 0,
    var unit: String? = null,
    var expirationDate: String? = null
)
// Eliminamos el constructor secundario para evitar la confusión del compilador.
// Usaremos la clase de datos directamente.