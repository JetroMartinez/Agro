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
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.agro.databinding.FragmentSalesBinding
import kotlinx.coroutines.launch

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AgroViewModel
    private lateinit var adapter: SalesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Usar requireActivity() para compartir el estado del carrito entre pestañas
        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]

        // 2. Inicializar el SalesAdapter (¡NO el ProductAdapter!)
        // Le pasamos lista vacía y mapa vacío al inicio.
        // El callback { product, newQty -> ... } le avisa al ViewModel que actualice el carrito.
        adapter = SalesAdapter(emptyList(), emptyMap()) { product, newQty ->
            viewModel.updateCart(product, newQty)
        }

        binding.recyclerSales.layoutManager = LinearLayoutManager(context)
        binding.recyclerSales.adapter = adapter

        // 3. Configurar el Buscador
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchProducts(newText ?: "")
                return true
            }
        })

        // 4. Configurar el Botón de Cobrar
        binding.btnPay.setOnClickListener {
            showPaymentDialog()
        }

        // 5. Observar los datos (Reactividad)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // A) Observar lista de productos (filtrada o completa)
                launch {
                    viewModel.productsFlow.collect { products ->
                        adapter.updateList(products)
                    }
                }

                // B) Observar cambios en el carrito (para actualizar los números en la lista)
                launch {
                    viewModel.cartFlow.collect { cart ->
                        adapter.updateCartData(cart)
                    }
                }

                // C) Observar el Total a Pagar
                launch {
                    viewModel.totalFlow.collect { total ->
                        binding.txtTotal.text = "Total: $${String.format("%.2f", total)}"
                        binding.btnPay.isEnabled = total > 0 // Solo habilitar si hay algo que cobrar
                    }
                }
            }
        }

        // Cargar productos al iniciar
        viewModel.loadProducts()
    }

    private fun showPaymentDialog() {
        val methods = arrayOf("Efectivo", "Tarjeta de Débito", "Tarjeta de Crédito", "Transferencia")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccione Método de Pago")
            .setItems(methods) { _, which ->
                val selectedMethod = methods[which]
                // Llamamos al checkout del ViewModel para guardar venta y descontar stock
                viewModel.checkout(selectedMethod)

                Toast.makeText(context, "Venta registrada con éxito", Toast.LENGTH_LONG).show()

                // Limpiar búsqueda y cerrar teclado si es necesario
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