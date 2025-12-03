package com.fcc.agro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // Importante agregar esta librería
import com.fcc.agro.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val adapter = AgroPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Ventas"
                    // Usamos un ícono de búsqueda (lupa) para simular la búsqueda de productos para venta
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search)
                }
                1 -> {
                    tab.text = "Inventario"
                    // Ícono de "Más" para agregar inventario
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add)
                }
                2 -> {
                    tab.text = "Precios"
                    // Ícono de "Vista" u "Ojo" para ver precios
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
                }
            }
        }.attach()
    }
}