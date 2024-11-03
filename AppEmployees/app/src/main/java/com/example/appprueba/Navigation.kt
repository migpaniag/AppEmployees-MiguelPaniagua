
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.appprueba.Screen

@Composable
fun MyNavigationBar( navController : NavHostController, )  {
        var selectedTab = 0
        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = {
                    selectedTab = 3
                    navController.navigate(Screen.SearchEmployee.route) // Navegar a Settings
                },
                icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                label = { Text("Search") })
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = {
                    selectedTab = 3
                    navController.navigate(Screen.Settings.route) // Navegar a Settings
                },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text("Settings") })


        }
}
