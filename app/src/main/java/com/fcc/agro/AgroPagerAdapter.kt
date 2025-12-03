package com.fcc.agro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AgroPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // CAMBIO AQUÍ: Posición 0 ahora es Precios
            0 -> PriceListFragment()

            // Posición 1 se queda igual (Inventario en medio)
            1 -> InventoryFragment()

            // CAMBIO AQUÍ: Posición 2 ahora es Ventas
            2 -> SalesFragment()

            else -> PriceListFragment() // Por defecto regresamos el primero
        }
    }
}