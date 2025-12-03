package com.fcc.agro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        // Configuración de Pestañas (TabLayout)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    // CAMBIO AQUÍ: Ahora la primera pestaña es Precios
                    tab.text = "Precios"
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_view)
                }
                1 -> {
                    // Inventario sigue en medio
                    tab.text = "Inventario"
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_add)
                }
                2 -> {
                    // CAMBIO AQUÍ: Ahora la última pestaña es Ventas
                    tab.text = "Ventas"
                    tab.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search)
                }
            }
        }.attach()
    }
}