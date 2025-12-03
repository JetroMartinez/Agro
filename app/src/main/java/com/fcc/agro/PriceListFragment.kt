package com.fcc.agro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView // Importante importar esta versión
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.agro.databinding.FragmentPriceListBinding
import kotlinx.coroutines.launch

class PriceListFragment : Fragment() {

    private var _binding: FragmentPriceListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AgroViewModel
    private lateinit var adapter: PriceListAdapter

    // Variable para guardar la copia completa de los productos
    private var fullProductList: List<Product> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPriceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]

        adapter = PriceListAdapter(emptyList())
        binding.recyclerPriceList.layoutManager = LinearLayoutManager(context)
        binding.recyclerPriceList.adapter = adapter

        // 1. Configurar el Buscador
        setupSearchView()

        // 2. Observar datos y guardar la lista completa
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsFlow.collect { products ->
                    // Guardamos la lista original completa
                    fullProductList = products
                    // Inicialmente mostramos todo (o aplicamos el filtro si ya hay texto escrito)
                    filterList(binding.searchView.query.toString())
                }
            }
        }
        viewModel.loadProducts()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Se ejecuta al dar Enter en el teclado
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            // Se ejecuta cada vez que escribes o borras una letra
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    // Función para filtrar
    private fun filterList(query: String?) {
        if (query.isNullOrEmpty()) {
            // Si no hay texto, mostramos la lista completa original
            adapter.updateList(fullProductList)
        } else {
            // Si hay texto, filtramos
            val filteredList = fullProductList.filter { product ->
                // Buscamos coincidencias en el nombre (ignorando mayúsculas/minúsculas)
                product.name?.contains(query, ignoreCase = true) == true
            }

            // Actualizamos el adaptador solo con los resultados
            if (filteredList.isEmpty()) {
                // Opcional: Podrías mostrar un Toast o un TextView de "No hay resultados"
                adapter.updateList(emptyList())
            } else {
                adapter.updateList(filteredList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}