package com.fcc.agro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.agro.databinding.FragmentInventoryBinding
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AgroViewModel
    private lateinit var adapter: InventoryAdapter // Usamos el nuevo adaptador

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]

        // Configurar Spinner
        val units = arrayOf("Kilogramo", "Litro", "Pieza")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, units)
        binding.spinnerUnit.adapter = spinnerAdapter

        // Configurar RecyclerView de Inventario (Detallado)
        adapter = InventoryAdapter(emptyList())
        binding.recyclerInventory.layoutManager = LinearLayoutManager(context)
        binding.recyclerInventory.adapter = adapter

        // Botón Guardar
        binding.btnAdd.setOnClickListener {
            val name = binding.editName.text.toString()
            val priceSale = binding.editPrice.text.toString().toDoubleOrNull()
            val pricePurchase = binding.editPurchasePrice.text.toString().toDoubleOrNull()
            val stock = binding.editStock.text.toString().toIntOrNull()
            val expDate = binding.editDate.text.toString()
            val unit = binding.spinnerUnit.selectedItem.toString()

            if (name.isNotEmpty() && priceSale != null && pricePurchase != null && stock != null) {
                // Si el usuario no pone fecha, guardamos "Sin fecha" o un texto vacío
                val finalDate = if (expDate.isBlank()) "Sin fecha" else expDate

                viewModel.addProduct(name, priceSale, pricePurchase, stock, unit, finalDate)

                // Limpiar
                binding.editName.text.clear()
                binding.editPrice.text.clear()
                binding.editPurchasePrice.text.clear()
                binding.editStock.text.clear()
                binding.editDate.text.clear()

                Toast.makeText(context, "Producto registrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar datos para actualizar la lista
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsFlow.collect { products ->
                    adapter.updateList(products)
                }
            }
        }

        // Carga inicial
        viewModel.loadProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}