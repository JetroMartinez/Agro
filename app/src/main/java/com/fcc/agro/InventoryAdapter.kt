package com.fcc.agro

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Agregamos el callback 'onEditClick' al constructor
class InventoryAdapter(
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgProductThumb)
        val name: TextView = view.findViewById(R.id.txtProductName)
        val details: TextView = view.findViewById(R.id.txtDetails)
        val prices: TextView = view.findViewById(R.id.txtAvailability)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit) // Referencia al lápiz
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_price_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // ... (Carga de imagen igual que antes) ...
        if (product.imageUri != null) {
            try { holder.image.setImageURI(Uri.parse(product.imageUri)) }
            catch (e: Exception) { holder.image.setImageResource(android.R.drawable.ic_menu_gallery) }
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.name.text = product.name
        holder.details.text = "Stock: ${product.stock} ${product.unit} | Cad: ${product.expirationDate}"
        holder.prices.text = "Costo: $${product.purchasePrice}\nVenta: $${product.price}"

        // --- LÓGICA DEL LÁPIZ ---
        holder.btnEdit.visibility = View.VISIBLE // Hacemos visible el lápiz SOLO en este adaptador
        holder.btnEdit.setOnClickListener {
            onEditClick(product) // Enviamos el producto al Fragment
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}