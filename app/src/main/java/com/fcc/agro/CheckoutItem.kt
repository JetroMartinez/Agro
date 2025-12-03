package com.fcc.agro

data class CheckoutItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)