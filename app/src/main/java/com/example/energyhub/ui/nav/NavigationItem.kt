package com.example.energyhub.ui.nav

import com.example.energyhub.R


sealed class NavigationItem(var route: String, val icon: Int){
    data object CurrentStatus : NavigationItem("CurrentStatus", R.drawable.three_way_arrow)
    data object History : NavigationItem("History", R.drawable.history)
    data object Planning : NavigationItem("Planning", R.drawable.planning)
}
