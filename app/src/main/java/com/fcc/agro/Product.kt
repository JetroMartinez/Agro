package com.fcc.agro

data class Product(
    var id: Int = 0,
    var name: String? = null,
    var price: Double = 0.0,
    var purchasePrice: Double = 0.0,
    var stock: Int = 0,
    var unit: String? = null,
    var expirationDate: String? = null,
    var imageUri: String? = null // <--- ESTE ES EL CAMPO QUE FALTABA
) {
    // Actualizamos los constructores para incluir la imagen (opcionalmente nula)
    constructor(name: String, price: Double, pPrice: Double, stock: Int, unit: String, exp: String, img: String?)
            : this(0, name, price, pPrice, stock, unit, exp, img)
}