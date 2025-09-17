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
    val workOrdersStates = rememberSaveable(saver = WorkOrdersStateSaver) {
        mutableStateMapOf<Int, MutableSet<Int>>()
    }
    val materialsStates = rememberSaveable(saver = MaterialsStateSaver) {
        mutableStateMapOf<Int, MutableList<String>>()
    }

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
                WorkOrdersScreen(
                    workOrdersStates = workOrdersStates,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("materials") {
                IssueMaterialsScreen(
                    materialsStates = materialsStates,
                    modifier = Modifier.fillMaxSize()
                )
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

private enum class WorkOrderInputField { EMPLOYEE, WORK_ORDER }

private enum class MaterialInputField { EMPLOYEE, MATERIAL }

@Composable
private fun WorkOrdersScreen(
    workOrdersStates: SnapshotStateMap<Int, MutableSet<Int>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var employeeNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var workOrderNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentField by rememberSaveable { mutableStateOf(WorkOrderInputField.EMPLOYEE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Work Orders",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(24.dp))
        DisplayValue(label = "Employee", value = employeeNumber?.toString().orEmpty())
        Spacer(modifier = Modifier.height(8.dp))
        DisplayValue(label = "Work Order", value = workOrderNumber?.toString().orEmpty())
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (currentField) {
                WorkOrderInputField.EMPLOYEE -> "Ingresando Employee Number"
                WorkOrderInputField.WORK_ORDER -> "Ingresando Work Order Number"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        KeypadPanel(
            modifier = Modifier.fillMaxWidth(),
            inputValue = inputValue,
            onNumberClick = { digit -> inputValue += digit },
            onClear = { inputValue = "" },
            onEnter = {
                when (currentField) {
                    WorkOrderInputField.EMPLOYEE -> {
                        val employee = inputValue.toIntOrNull()
                        if (employee == null) {
                            Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            employeeNumber = employee
                            currentField = WorkOrderInputField.WORK_ORDER
                            inputValue = ""
                        }
                    }

                    WorkOrderInputField.WORK_ORDER -> {
                        val workOrder = inputValue.toIntOrNull()
                        if (workOrder == null) {
                            Toast.makeText(context, "Ingrese un número de Work Order", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            workOrderNumber = workOrder
                            currentField = WorkOrderInputField.EMPLOYEE
                            inputValue = ""
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val employee = employeeNumber
                val workOrder = workOrderNumber
                if (employee == null) {
                    Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (workOrder == null) {
                    Toast.makeText(context, "Ingrese un número de Work Order", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val updatedSet = workOrdersStates[employee]?.toMutableSet() ?: mutableSetOf()
                updatedSet.add(workOrder)
                workOrdersStates[employee] = updatedSet

                Toast.makeText(
                    context,
                    "Employee $employee Clocked In on WO $workOrder",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text(text = "Clock In WO", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val employee = employeeNumber
                val workOrder = workOrderNumber
                if (employee == null) {
                    Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (workOrder == null) {
                    Toast.makeText(context, "Ingrese un número de Work Order", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val existingSet = workOrdersStates[employee]
                if (existingSet != null) {
                    val updatedSet = existingSet.toMutableSet()
                    updatedSet.remove(workOrder)
                    if (updatedSet.isEmpty()) {
                        workOrdersStates.remove(employee)
                    } else {
                        workOrdersStates[employee] = updatedSet
                    }
                }

                Toast.makeText(
                    context,
                    "Employee $employee Clocked Out from WO $workOrder",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text(text = "Clock Out WO", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun IssueMaterialsScreen(
    materialsStates: SnapshotStateMap<Int, MutableList<String>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var employeeNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var materialCode by rememberSaveable { mutableStateOf<String?>(null) }
    var currentField by rememberSaveable { mutableStateOf(MaterialInputField.EMPLOYEE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Issue Materials",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(24.dp))
        DisplayValue(label = "Employee", value = employeeNumber?.toString().orEmpty())
        Spacer(modifier = Modifier.height(8.dp))
        DisplayValue(label = "Material", value = materialCode.orEmpty())
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (currentField) {
                MaterialInputField.EMPLOYEE -> "Ingresando Employee Number"
                MaterialInputField.MATERIAL -> "Ingresando Material Code"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        KeypadPanel(
            modifier = Modifier.fillMaxWidth(),
            inputValue = inputValue,
            onNumberClick = { digit -> inputValue += digit },
            onClear = { inputValue = "" },
            onEnter = {
                when (currentField) {
                    MaterialInputField.EMPLOYEE -> {
                        val employee = inputValue.toIntOrNull()
                        if (employee == null) {
                            Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            employeeNumber = employee
                            currentField = MaterialInputField.MATERIAL
                            inputValue = ""
                        }
                    }

                    MaterialInputField.MATERIAL -> {
                        if (inputValue.isEmpty()) {
                            Toast.makeText(context, "Ingrese un código de material", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            materialCode = inputValue
                            currentField = MaterialInputField.EMPLOYEE
                            inputValue = ""
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val employee = employeeNumber
                val material = materialCode
                if (employee == null) {
                    Toast.makeText(context, "Ingrese un número de empleado", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (material.isNullOrEmpty()) {
                    Toast.makeText(context, "Ingrese un código de material", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val updatedList = materialsStates[employee]?.toMutableList() ?: mutableListOf()
                updatedList.add(material)
                materialsStates[employee] = updatedList

                Toast.makeText(
                    context,
                    "Employee $employee issued Material $material",
                    Toast.LENGTH_SHORT
                ).show()

                materialCode = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text(text = "Issue Material", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DisplayValue(label: String, value: String) {
    Text(
        text = "$label: ${value.ifEmpty { "--" }}",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

private val LoginStateSaver: Saver<SnapshotStateMap<Int, Boolean>, Map<Int, Boolean>> = Saver(
    save = { stateMap -> stateMap.toMap() },
    restore = { restoredMap ->
        mutableStateMapOf<Int, Boolean>().apply { putAll(restoredMap) }
    }
)

private val WorkOrdersStateSaver:
    Saver<SnapshotStateMap<Int, MutableSet<Int>>, Map<Int, List<Int>>> = Saver(
    save = { stateMap ->
        stateMap.mapValues { it.value.toList() }
    },
    restore = { restoredMap ->
        mutableStateMapOf<Int, MutableSet<Int>>().apply {
            restoredMap.forEach { (employee, workOrders) ->
                put(employee, workOrders.toMutableSet())
            }
        }
    }
)

private val MaterialsStateSaver:
    Saver<SnapshotStateMap<Int, MutableList<String>>, Map<Int, List<String>>> = Saver(
    save = { stateMap ->
        stateMap.mapValues { it.value.toList() }
    },
    restore = { restoredMap ->
        mutableStateMapOf<Int, MutableList<String>>().apply {
            restoredMap.forEach { (employee, materials) ->
                put(employee, materials.toMutableList())
            }
        }
    }
)

@Preview(showBackground = true, widthDp = 900, heightDp = 450)
@Composable
private fun TerminalAppPreview() {
    TerminalTheme {
        TerminalApp()
    }
}
