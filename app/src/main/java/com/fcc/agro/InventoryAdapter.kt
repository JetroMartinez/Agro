package com.fcc.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(android.R.id.text1)
        val details: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Usamos un layout de sistema simple para no complicarnos,
        // pero puedes crear uno personalizado si prefieres.
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.name.text = "${product.name} (Stock: ${product.stock} ${product.unit})"

        // AQUÍ ES DONDE MOSTRAMOS LA FECHA Y EL COSTO
        // Si la fecha es nula o vacía, mostramos "Sin fecha", si no, la fecha real.
        val fechaTexto = if (product.expirationDate.isNullOrEmpty()) "Sin fecha" else product.expirationDate

        holder.details.text = "Compra: $${product.purchasePrice} | Venta: $${product.price} | Caduca: $fechaTexto"
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}