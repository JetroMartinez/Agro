package com.fcc.agro

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PriceListAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<PriceListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgProductThumb)
        val name: TextView = view.findViewById(R.id.txtProductName)
        val price: TextView = view.findViewById(R.id.txtPrice)
        val details: TextView = view.findViewById(R.id.txtDetails)
        val status: TextView = view.findViewById(R.id.txtAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_price_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // 1. Cargar Imagen
        if (product.imageUri != null) {
            try {
                holder.image.setImageURI(Uri.parse(product.imageUri))
            } catch (e: Exception) {
                // Si la imagen fue borrada del teléfono o hay error, mostrar default
                holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 2. Datos de texto
        holder.name.text = product.name
        holder.price.text = "$${String.format("%.2f", product.price)}"
        holder.details.text = "${product.unit} | Cad: ${product.expirationDate}"

        // 3. Estado de Inventario (Estilo Visual)
        if (product.stock > 0) {
            holder.status.text = "EN STOCK (${product.stock})"
            holder.status.setTextColor(Color.parseColor("#1976D2")) // Azul
            // Si usaste un backgroundTint en el XML, puedes cambiarlo aquí dinámicamente si deseas
        } else {
            holder.status.text = "AGOTADO"
            holder.status.setTextColor(Color.parseColor("#D32F2F")) // Rojo
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}