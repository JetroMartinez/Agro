package com.fcc.agro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AgroPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3 // Tenemos 3 pestañas

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SalesFragment()      // Pestaña 1: Ventas
            1 -> InventoryFragment()  // Pestaña 2: Inventario
            2 -> PriceListFragment()  // Pestaña 3: Precios
            else -> SalesFragment()
        }
    }
}