package com.fcc.agro

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PriceListAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<PriceListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
        holder.name.text = product.name
        holder.price.text = "$${product.price}"

        // CORRECCIÓN: Solo mostramos la Unidad. Quitamos la fecha.
        holder.details.text = "Venta por: ${product.unit}"

        if (product.stock > 0) {
            holder.status.text = "DISPONIBLE"
            holder.status.setTextColor(Color.BLUE)
        } else {
            holder.status.text = "AGOTADO"
            holder.status.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}