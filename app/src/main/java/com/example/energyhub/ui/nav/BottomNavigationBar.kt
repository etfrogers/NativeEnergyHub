import androidx.navigation.NavController
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.text.style.TextOverflow
import com.example.energyhub.ui.nav.NavigationItem



@Composable
fun BottomNavigationBar( navController: NavController){

    val items = listOf(
        NavigationItem.CurrentStatus,
        NavigationItem.History,
        NavigationItem.Planning,
    )
    var selectedItem by remember { mutableIntStateOf(0) }
    var currentRoute by remember { mutableStateOf(NavigationItem.CurrentStatus.route) }

    items.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(

                alwaysShowLabel = true,
                icon = { Icon(item.icon!!, contentDescription = item.route) },
//                label = { Text(item.title, overflow = TextOverflow.Ellipsis, maxLines = 1) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    currentRoute = item.route
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults
                    .colors(
//                        selectedIconColor = NavigationBarTokens.ActiveIndicatorColor.toColor(),
//                        unselectedIconColor = AppDefaults.navigationContentColor(),
//                        unselectedTextColor = AppDefaults.navigationContentColor(),
//                        indicatorColor = AppDefaults.navigationIndicatorColor()
                        selectedTextColor = Blue,
                        selectedIconColor = Blue,
                        unselectedTextColor = Gray,
                        )



            )

        }
    }

}