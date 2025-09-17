package com.example.terminal

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.terminal.ui.theme.TerminalTheme

@Composable
fun TerminalApp() {
    val navController = rememberNavController()
    val loginStates = rememberSaveable(saver = LoginStateSaver) { mutableStateMapOf<Int, Boolean>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "menu",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("menu") {
                LeftPanel(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("clock") {
                ClockScreen(
                    loginStates = loginStates,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("workorders") {
                WorkOrdersScreen(modifier = Modifier.fillMaxSize())
            }
            composable("materials") {
                IssueMaterialsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun LeftPanel(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Panel de Información",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(32.dp))
        MenuButton(text = "Clock In/Out", onClick = { navController.navigate("clock") })
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton(text = "Work Orders", onClick = { navController.navigate("workorders") })
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton(text = "Issue Materials", onClick = { navController.navigate("materials") })
    }
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun KeypadPanel(
    modifier: Modifier = Modifier,
    inputValue: String,
    onNumberClick: (String) -> Unit,
    onClear: () -> Unit,
    onEnter: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = inputValue.ifEmpty { " " },
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.End
        )

        val numberRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        numberRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { number ->
                    KeypadButton(
                        label = number,
                        onClick = { onNumberClick(number) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KeypadButton(label = "Clear", onClick = onClear)
            KeypadButton(label = "0", onClick = { onNumberClick("0") })
            KeypadButton(label = "Enter", onClick = onEnter)
        }
    }
}

@Composable
private fun RowScope.KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .height(72.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun ClockScreen(
    loginStates: SnapshotStateMap<Int, Boolean>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clock In/Out",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(24.dp))
        KeypadPanel(
            modifier = Modifier.fillMaxWidth(),
            inputValue = inputValue,
            onNumberClick = { digit -> inputValue += digit },
            onClear = { inputValue = "" },
            onEnter = {
                val employeeNumber = inputValue.toIntOrNull()
                if (employeeNumber == null) {
                    Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT).show()
                } else {
                    val isLoggedIn = loginStates[employeeNumber] == true
                    if (isLoggedIn) {
                        Toast.makeText(context, "Clock Out Successful", Toast.LENGTH_SHORT).show()
                        loginStates.remove(employeeNumber)
                    } else {
                        Toast.makeText(context, "Clock In Successful", Toast.LENGTH_SHORT).show()
                        loginStates[employeeNumber] = true
                    }
                }
                inputValue = ""
            }
        )
    }
}

@Composable
private fun WorkOrdersScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla Work Orders",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun IssueMaterialsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla Issue Materials",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

private val LoginStateSaver: Saver<SnapshotStateMap<Int, Boolean>, Map<Int, Boolean>> = Saver(
    save = { stateMap -> stateMap.toMap() },
    restore = { restoredMap ->
        mutableStateMapOf<Int, Boolean>().apply { putAll(restoredMap) }
    }
)

@Preview(showBackground = true, widthDp = 900, heightDp = 450)
@Composable
private fun TerminalAppPreview() {
    TerminalTheme {
        TerminalApp()
    }
}
