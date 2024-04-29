package com.example.energyhub.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.graphics.vector.ImageVector


sealed class NavigationItem(var route: String, val icon: ImageVector?){
    object CurrentStatus : NavigationItem("CurrentStatus", Icons.Rounded.Home, )
    object History : NavigationItem("History", Icons.Rounded.Info)
    object Planning : NavigationItem("Planning", Icons.Rounded.Home)
}
