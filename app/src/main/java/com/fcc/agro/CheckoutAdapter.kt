package com.fcc.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CheckoutAdapter(private val items: List<CheckoutItem>) :
    RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    // CORRECCIÓN: Aquí es donde ocurría el error.
    // Ahora buscamos los IDs que SÍ existen en item_checkout_row.xml
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val qty: TextView = view.findViewById(R.id.rowQty)
        val name: TextView = view.findViewById(R.id.rowName)
        val subtotal: TextView = view.findViewById(R.id.rowSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.qty.text = "${item.quantity}x"
        holder.name.text = item.productName
        holder.subtotal.text = "$${String.format("%.2f", item.subtotal)}"
    }

    override fun getItemCount(): Int = items.size
}