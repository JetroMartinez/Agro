package com.fcc.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SalesAdapter(
    var productList: List<Product>,
    var cart: Map<Int, Int>,
    private val onQuantityChange: (Product, Int) -> Unit
) : RecyclerView.Adapter<SalesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtName)
        val info: TextView = view.findViewById(R.id.txtInfo)
        val qty: TextView = view.findViewById(R.id.txtQty)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val imgProduct: ImageView = view.findViewById(R.id.imgProductItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        val currentQty = cart[product.id] ?: 0

        holder.name.text = product.name
        // Mostramos Precio y Stock disponible
        holder.info.text = "$${product.price} | Disp: ${product.stock} ${product.unit}"
        holder.qty.text = currentQty.toString()

        // Cargar Imagen
        if (product.imageUri != null) {
            try {
                holder.imgProduct.setImageURI(android.net.Uri.parse(product.imageUri))
            } catch (e: Exception) {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Lógica de botones
        holder.btnMinus.setOnClickListener {
            if (currentQty > 0) {
                onQuantityChange(product, currentQty - 1)
            }
        }

        holder.btnPlus.setOnClickListener {
            if (currentQty < product.stock) {
                onQuantityChange(product, currentQty + 1)
            }
        }

        // Deshabilitar botón + si ya alcanzamos el máximo stock
        holder.btnPlus.isEnabled = product.stock > 0 && currentQty < product.stock
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    fun updateCartData(newCart: Map<Int, Int>) {
        this.cart = newCart
        notifyDataSetChanged()
    }
}