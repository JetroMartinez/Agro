package com.fcc.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SalesAdapter(
    private var productList: List<Product>,
    private var cart: Map<Int, Int>, // Mapa: ID -> Cantidad
    private val onQuantityChange: (Product, Int) -> Unit
) : RecyclerView.Adapter<SalesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val qty: TextView = view.findViewById(R.id.txtQty)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // Obtenemos la cantidad del carrito (si es null, es 0)
        val currentQty = cart[product.id] ?: 0

        holder.name.text = product.name
        holder.info.text = "$${product.price}"
        holder.qty.text = currentQty.toString()

        // Lógica Botón Menos (-)
        holder.btnMinus.setOnClickListener {
            if (currentQty > 0) {
                onQuantityChange(product, currentQty - 1)
            }
        }

        // Lógica Botón Más (+)
        holder.btnPlus.setOnClickListener {
            if (currentQty < product.stock) {
                onQuantityChange(product, currentQty + 1)
            }
        }

        // Deshabilitar botón + si alcanzamos el stock máximo
        holder.btnPlus.isEnabled = product.stock > 0 && currentQty < product.stock
        // Deshabilitar botón - si es 0
        holder.btnMinus.isEnabled = currentQty > 0
    }

    override fun getItemCount(): Int = productList.size

    // Actualizar la lista de productos (cuando busques)
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }



    // Actualizar los datos del carrito (cuando sumes o restes)
    fun updateCartData(newCart: Map<Int, Int>) {
        this.cart = newCart
        notifyDataSetChanged()
    }
}