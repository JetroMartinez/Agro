package com.fcc.agro

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager // Importante
import com.fcc.agro.databinding.FragmentSalesBinding
import kotlinx.coroutines.launch

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AgroViewModel
    private lateinit var adapter: SalesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]

        // Inicializar Adapter
        adapter = SalesAdapter(emptyList(), emptyMap()) { product, newQty ->
            viewModel.updateCart(product, newQty)
        }

        // --- CORRECCIÓN AQUÍ ---
        // Usamos GridLayoutManager con 2 columnas para que se vea cuadrado
        binding.recyclerSales.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerSales.adapter = adapter

        // Configurar Búsqueda
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchProducts(newText ?: "")
                return true
            }
        })

        // Botón Cobrar
        binding.btnPay.setOnClickListener {
            showPaymentDialog()
        }

        // Observar DATOS (Productos y Carrito)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Observar cambios en la lista de productos (para llenar el grid)
                launch {
                    viewModel.productsFlow.collect { products ->
                        adapter.updateList(products)
                    }
                }
                // 2. Observar cambios en el carrito (para los números 0, 1, 2...)
                launch {
                    viewModel.cartFlow.collect { cart ->
                        adapter.updateCartData(cart)
                    }
                }
                // 3. Observar Total
                launch {
                    viewModel.totalFlow.collect { total ->
                        binding.txtTotal.text = "Total: $${String.format("%.2f", total)}"
                        binding.btnPay.isEnabled = total > 0
                    }
                }
            }
        }

        viewModel.loadProducts()
    }

    private fun showPaymentDialog() {
        val methods = arrayOf("Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito", "Transferencia")
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Venta")
            .setMessage("Total a pagar: ${binding.txtTotal.text}")
            .setItems(methods) { _, which ->
                val selectedMethod = methods[which]
                viewModel.checkout(selectedMethod)
                Toast.makeText(context, "Venta registrada con éxito", Toast.LENGTH_LONG).show()
                binding.searchView.setQuery("", false)
                binding.searchView.clearFocus()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}