package com.example.terminal

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.terminal.ui.theme.TerminalTheme
import java.util.ArrayList

@Composable
fun TerminalApp() {
    val tabs = TerminalTab.values()
    var selectedTab by rememberSaveable { mutableStateOf(TerminalTab.CLOCK) }
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
        Column(modifier = Modifier.fillMaxSize()) {
            val selectedIndex = tabs.indexOf(selectedTab)
            TabRow(selectedTabIndex = selectedIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    TerminalTab.CLOCK -> ClockTabContent(
                        loginStates = loginStates,
                        modifier = Modifier.fillMaxSize()
                    )

                    TerminalTab.WORK_ORDERS -> WorkOrdersTabContent(
                        workOrdersStates = workOrdersStates,
                        modifier = Modifier.fillMaxSize()
                    )

                    TerminalTab.ISSUE_MATERIALS -> IssueMaterialsTabContent(
                        materialsStates = materialsStates,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

private enum class TerminalTab(val title: String) {
    CLOCK("Clock In/Out"),
    WORK_ORDERS("Work Orders"),
    ISSUE_MATERIALS("Issue Materials")
}

@Composable
private fun ClockTabContent(
    loginStates: SnapshotStateMap<Int, Boolean>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var lastEmployee by rememberSaveable { mutableStateOf<Int?>(null) }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Clock In/Out",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            DisplayValue(
                label = "Empleado",
                value = inputValue.ifEmpty { lastEmployee?.toString().orEmpty() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val statusText = lastEmployee?.let { employee ->
                if (loginStates[employee] == true) "Clocked In" else "Clocked Out"
            } ?: "--"
            DisplayValue(label = "Estado", value = statusText)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ingrese el número de empleado y presione Enter para clock in/out.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        NumericKeypad(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(24.dp),
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
                    lastEmployee = employeeNumber
                }
                inputValue = ""
            }
        )
    }
}

private enum class WorkOrderInputField { EMPLOYEE, WORK_ORDER }

private enum class MaterialInputField { EMPLOYEE, MATERIAL }

@Composable
private fun WorkOrdersTabContent(
    workOrdersStates: SnapshotStateMap<Int, MutableSet<Int>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var employeeNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var workOrderNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentField by rememberSaveable { mutableStateOf(WorkOrderInputField.EMPLOYEE) }

    val instructionText = when (currentField) {
        WorkOrderInputField.EMPLOYEE -> "Ingrese o escanee el número de empleado y presione Enter."
        WorkOrderInputField.WORK_ORDER -> "Ingrese o escanee el número de Work Order y presione Enter."
    }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Work Orders",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            DisplayValue(label = "Employee", value = employeeNumber?.toString().orEmpty())
            Spacer(modifier = Modifier.height(8.dp))
            DisplayValue(label = "Work Order", value = workOrderNumber?.toString().orEmpty())
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = instructionText,
                style = MaterialTheme.typography.bodyLarge
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

        NumericKeypad(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(24.dp),
            onNumberClick = { digit -> inputValue += digit },
            onClear = { inputValue = "" },
            onEnter = {
                when (currentField) {
                    WorkOrderInputField.EMPLOYEE -> {
                        val employee = inputValue.toIntOrNull()
                        if (employee == null) {
                            Toast.makeText(
                                context,
                                "Ingrese un número de empleado",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            employeeNumber = employee
                            currentField = WorkOrderInputField.WORK_ORDER
                            inputValue = ""
                        }
                    }

                    WorkOrderInputField.WORK_ORDER -> {
                        val workOrder = inputValue.toIntOrNull()
                        if (workOrder == null) {
                            Toast.makeText(
                                context,
                                "Ingrese un número de Work Order",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            workOrderNumber = workOrder
                            currentField = WorkOrderInputField.EMPLOYEE
                            inputValue = ""
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun IssueMaterialsTabContent(
    materialsStates: SnapshotStateMap<Int, MutableList<String>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputValue by rememberSaveable { mutableStateOf("") }
    var employeeNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var materialCode by rememberSaveable { mutableStateOf<String?>(null) }
    var currentField by rememberSaveable { mutableStateOf(MaterialInputField.EMPLOYEE) }

    val instructionText = when (currentField) {
        MaterialInputField.EMPLOYEE -> "Ingrese o escanee el número de empleado y presione Enter."
        MaterialInputField.MATERIAL -> "Ingrese o escanee el código de material y presione Enter."
    }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Issue Materials",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            DisplayValue(label = "Employee", value = employeeNumber?.toString().orEmpty())
            Spacer(modifier = Modifier.height(8.dp))
            DisplayValue(label = "Material", value = materialCode.orEmpty())
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = instructionText,
                style = MaterialTheme.typography.bodyLarge
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

        NumericKeypad(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(24.dp),
            onNumberClick = { digit -> inputValue += digit },
            onClear = { inputValue = "" },
            onEnter = {
                when (currentField) {
                    MaterialInputField.EMPLOYEE -> {
                        val employee = inputValue.toIntOrNull()
                        if (employee == null) {
                            Toast.makeText(
                                context,
                                "Ingrese un número de empleado",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            employeeNumber = employee
                            currentField = MaterialInputField.MATERIAL
                            inputValue = ""
                        }
                    }

                    MaterialInputField.MATERIAL -> {
                        if (inputValue.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Ingrese un código de material",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            materialCode = inputValue
                            currentField = MaterialInputField.EMPLOYEE
                            inputValue = ""
                        }
                    }
                }
            }
        )
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

@Composable
private fun NumericKeypad(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onClear: () -> Unit,
    onEnter: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val keypadItems = listOf(
            "1" to { onNumberClick("1") },
            "2" to { onNumberClick("2") },
            "3" to { onNumberClick("3") },
            "4" to { onNumberClick("4") },
            "5" to { onNumberClick("5") },
            "6" to { onNumberClick("6") },
            "7" to { onNumberClick("7") },
            "8" to { onNumberClick("8") },
            "9" to { onNumberClick("9") },
            "Clear" to onClear,
            "0" to { onNumberClick("0") },
            "Enter" to onEnter
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.SpaceEvenly,
            userScrollEnabled = false
        ) {
            items(keypadItems, key = { it.first }) { (label, action) ->
                KeypadButton(
                    label = label,
                    onClick = action
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.button),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        Text(
            text = label,
            color = Color.Black,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
    }
}

private val LoginStateSaver:
    Saver<SnapshotStateMap<Int, Boolean>, ArrayList<Pair<Int, Boolean>>> = Saver(
        save = { stateMap ->
            ArrayList(stateMap.map { it.key to it.value })
        },
        restore = { restoredList ->
            mutableStateMapOf<Int, Boolean>().apply {
                restoredList.forEach { (employee, isLoggedIn) ->
                    this[employee] = isLoggedIn
                }
            }
        }
    )

private val WorkOrdersStateSaver:
    Saver<SnapshotStateMap<Int, MutableSet<Int>>, ArrayList<Pair<Int, ArrayList<Int>>>> = Saver(
        save = { stateMap ->
            ArrayList(stateMap.map { (employee, workOrders) ->
                employee to ArrayList(workOrders)
            })
        },
        restore = { restoredList ->
            mutableStateMapOf<Int, MutableSet<Int>>().apply {
                restoredList.forEach { (employee, workOrders) ->
                    this[employee] = workOrders.toMutableSet()
                }
            }
        }
    )

private val MaterialsStateSaver:
    Saver<SnapshotStateMap<Int, MutableList<String>>, ArrayList<Pair<Int, ArrayList<String>>>> = Saver(
        save = { stateMap ->
            ArrayList(stateMap.map { (employee, materials) ->
                employee to ArrayList(materials)
            })
        },
        restore = { restoredList ->
            mutableStateMapOf<Int, MutableList<String>>().apply {
                restoredList.forEach { (employee, materials) ->
                    this[employee] = materials.toMutableList()
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
