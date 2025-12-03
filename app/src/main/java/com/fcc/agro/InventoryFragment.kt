package com.fcc.agro

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcc.agro.databinding.FragmentInventoryBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AgroViewModel
    private lateinit var adapter: InventoryAdapter

    private var currentPhotoUri: Uri? = null
    private var currentPhotoPathString: String? = null
    private var activeDialogView: View? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoUri != null && activeDialogView != null) {
            val imgPreview = activeDialogView!!.findViewById<ImageView>(R.id.imgProductInput)
            imgPreview.setImageURI(currentPhotoUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[AgroViewModel::class.java]

        // Configurar Adapter pasando la función para EDITAR
        adapter = InventoryAdapter(emptyList()) { productToEdit ->
            showProductDialog(productToEdit) // Reutilizamos el dialog para editar
        }

        binding.recyclerInventory.layoutManager = LinearLayoutManager(context)
        binding.recyclerInventory.adapter = adapter

        // Botón "Añadir" (Modo Crear, pasamos null)
        binding.btnShowAddForm.setOnClickListener {
            showProductDialog(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsFlow.collect { products -> adapter.updateList(products) }
            }
        }
        viewModel.loadProducts()
    }

    // Función unificada para Crear o Editar
    private fun showProductDialog(productToEdit: Product?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null)
        activeDialogView = dialogView

        // Referencias a vistas
        val txtTitle = dialogView.findViewById<TextView>(R.id.inputName) // Asegúrate que el ID del título en el XML sea este o ponle uno
        val imgInput = dialogView.findViewById<ImageView>(R.id.imgProductInput)
        val editName = dialogView.findViewById<EditText>(R.id.inputName)
        val editCost = dialogView.findViewById<EditText>(R.id.inputPurchasePrice)
        val editPrice = dialogView.findViewById<EditText>(R.id.inputSalePrice)
        val editStock = dialogView.findViewById<EditText>(R.id.inputStock)
        val editDate = dialogView.findViewById<EditText>(R.id.inputDate)
        val spinner = dialogView.findViewById<Spinner>(R.id.inputUnitSpinner)

        // Configurar Spinner
        val units = arrayOf("Kilogramo", "Litro", "Pieza")
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, units)
        spinner.adapter = adapterSpinner

        // --- MODO EDICIÓN: Rellenar datos si productToEdit no es null ---
        if (productToEdit != null) {
            // Buscamos el TextView título si existe, o cambiamos el texto del botón después
            // Rellenar campos
            editName.setText(productToEdit.name)
            editCost.setText(productToEdit.purchasePrice.toString())
            editPrice.setText(productToEdit.price.toString())
            editStock.setText(productToEdit.stock.toString())
            editDate.setText(productToEdit.expirationDate)

            // Seleccionar unidad en spinner
            val unitIndex = units.indexOf(productToEdit.unit)
            if (unitIndex >= 0) spinner.setSelection(unitIndex)

            // Cargar imagen existente
            if (productToEdit.imageUri != null) {
                try { imgInput.setImageURI(Uri.parse(productToEdit.imageUri)) }
                catch (e: Exception) { /* Ignorar error */ }
            }
        }

        imgInput.setOnClickListener { takePhoto() }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton(if (productToEdit == null) "Guardar" else "Actualizar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = editName.text.toString()
            val cost = editCost.text.toString().toDoubleOrNull()
            val price = editPrice.text.toString().toDoubleOrNull()
            val stock = editStock.text.toString().toIntOrNull()
            val date = editDate.text.toString()
            val unit = spinner.selectedItem.toString()

            if (name.isNotEmpty() && cost != null && price != null && stock != null) {
                if (productToEdit == null) {
                    // CREAR NUEVO
                    viewModel.addProduct(name, price, cost, stock, unit, date, currentPhotoPathString)
                    Toast.makeText(context, "Producto Creado", Toast.LENGTH_SHORT).show()
                } else {
                    // ACTUALIZAR EXISTENTE
                    // Si no se tomó foto nueva (currentPhotoPathString es null), enviamos null para conservar la vieja en el ViewModel
                    viewModel.updateProduct(productToEdit.id, name, price, cost, stock, unit, date, currentPhotoPathString)
                    Toast.makeText(context, "Producto Actualizado", Toast.LENGTH_SHORT).show()
                }

                currentPhotoPathString = null
                activeDialogView = null
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Faltan datos obligatorios", Toast.LENGTH_LONG).show()
            }
        }

        dialog.setOnDismissListener {
            activeDialogView = null
            currentPhotoPathString = null // Limpiar foto temporal al cerrar
        }
    }

    private fun takePhoto() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), "com.fcc.agro.fileprovider", photoFile)
            currentPhotoPathString = currentPhotoUri.toString()
            takePictureLauncher.launch(currentPhotoUri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}