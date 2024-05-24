package com.example.energyhub

import BottomNavigationBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.energyhub.model.Config
import com.example.energyhub.model.SystemModel
import com.example.energyhub.model.loadConfig
import com.example.energyhub.ui.nav.Navigation
import com.example.energyhub.ui.theme.EnergyHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        loadConfig( this
            .resources
            .openRawResource(R.raw.config)
        )
        SystemModel.build(Config)

        super.onCreate(savedInstanceState)
        setContent {
            EnergyHubTheme {
                MainView()
//                testRefresh()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    EnergyHubTheme {
        MainView()
    }
}

@Composable
fun MainView(){
    val navController = rememberNavController()
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController
) {

    Scaffold(
        bottomBar = {
            BottomAppBar(modifier = Modifier.height(60.dp)) {
                BottomNavigationBar(navController = navController)
            }
        },

        ) { innerPadding ->
        Box(
            modifier = Modifier.padding(
                PaddingValues(
                    0.dp,
                    0.dp,
                    0.dp,
                    innerPadding.calculateBottomPadding()
                )
            )
        ) {
            Navigation(navController = navController)
        }
    }

}