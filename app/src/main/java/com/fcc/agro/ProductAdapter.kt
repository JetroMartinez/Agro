package com.fcc.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var productList: List<Product>,
    private val onSellClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Usamos text1 y text2 del layout simple de Android
        val textName: TextView = view.findViewById(android.R.id.text1)
        val textDetails: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // 1. Título: Nombre del producto
        holder.textName.text = product.name

        // 2. Subtítulo: Precio, Unidad, Stock y CADUCIDAD. (Sin ID)
        val info = "Precio: $${product.price} | Stock: ${product.stock} ${product.unit}\nCaduca: ${product.expirationDate}"

        holder.textDetails.text = info

        holder.itemView.setOnClickListener {
            onSellClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}