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
        // 1. Obtener datos del carrito para construir el recibo
        val currentCart = viewModel.cartFlow.value
        val allProducts = adapter.productList // Obtenemos la lista completa del adapter actual
        val checkoutItems = ArrayList<CheckoutItem>()
        var calculatedTotal = 0.0

        currentCart.forEach { (id, qty) ->
            val product = allProducts.find { it.id == id }
            if (product != null) {
                val subtotal = product.price * qty
                calculatedTotal += subtotal
                checkoutItems.add(CheckoutItem(product.name ?: "Sin nombre", qty, product.price, subtotal))
            }
        }

        // Si el carrito está vacío (por seguridad), no abrir
        if (checkoutItems.isEmpty()) return

        // 2. Inflar el diseño del Ticket
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_checkout, null)

        // 3. Configurar el RecyclerView del Ticket
        val recyclerCheckout = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerCheckout)
        recyclerCheckout.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerCheckout.adapter = CheckoutAdapter(checkoutItems)

        // 4. Poner el Total
        val txtFinalTotal = dialogView.findViewById<android.widget.TextView>(R.id.txtFinalTotal)
        txtFinalTotal.text = "$${String.format("%.2f", calculatedTotal)}"

        // 5. Crear el Dialog (sin botones default)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Fondo transparente para que se vean las esquinas redondeadas del CardView
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 6. Lógica de Botones del Ticket
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btnConfirmSale)
        val radioGroup = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            // Obtener método de pago seleccionado
            val selectedId = radioGroup.checkedRadioButtonId
            val paymentMethod = if (selectedId == R.id.rbCard) "Tarjeta" else "Efectivo"

            // Llamar al ViewModel para guardar en BD y restar stock
            viewModel.checkout(paymentMethod)

            Toast.makeText(context, "¡Venta Exitosa!", Toast.LENGTH_LONG).show()

            // Limpiezas visuales
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()

            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}