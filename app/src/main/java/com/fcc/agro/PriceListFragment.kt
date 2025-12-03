package com.fcc.agro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPriceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Usar requireActivity() para compartir el mismo ViewModel que Inventario/Ventas
        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]
        adapter = PriceListAdapter(emptyList())
        binding.recyclerPriceList.layoutManager = LinearLayoutManager(context)
        binding.recyclerPriceList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsFlow.collect { products ->
                    adapter.updateList(products) // Aquí se actualiza la lista
                }
            }
        }

        // Opcional: Forzar recarga si la lista está vacía al entrar
        if (viewModel.productsFlow.value.isEmpty()) {
            viewModel.loadProducts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}