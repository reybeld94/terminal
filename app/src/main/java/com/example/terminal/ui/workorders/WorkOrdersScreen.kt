package com.example.terminal.ui.workorders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.data.network.ClockOutStatus

@Composable
fun WorkOrdersScreen(
    viewModel: WorkOrdersViewModel = viewModel(
        factory = WorkOrdersViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                WorkOrdersForm(
                    modifier = Modifier.weight(0.6f),
                    uiState = uiState,
                    onEmployeeClick = viewModel::onEmployeeFieldSelected,
                    onWorkOrderClick = viewModel::onWorkOrderFieldSelected,
                    onClockIn = viewModel::onClockIn,
                    onClockOut = viewModel::onClockOutClick
                )

                Spacer(modifier = Modifier.width(24.dp))

                WorkOrdersKeypad(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight(),
                    onNumberClick = viewModel::setDigit,
                    onClear = viewModel::clear,
                    onEnter = viewModel::enter
                )
            }

            if (uiState.showClockOutDialog) {
                ClockOutDialog(
                    onDismiss = viewModel::dismissClockOutDialog,
                    onConfirm = { qty, status -> viewModel.onClockOut(qty, status) }
                )
            }

            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun WorkOrdersForm(
    modifier: Modifier = Modifier,
    uiState: WorkOrdersUiState,
    onEmployeeClick: () -> Unit,
    onWorkOrderClick: () -> Unit,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    val isClockInEnabled = uiState.employeeId.isNotBlank() && uiState.workOrderId.isNotBlank() && !uiState.isLoading
    val isClockOutEnabled = isClockInEnabled && !uiState.isLoading

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Work Orders",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        SelectableField(
            label = "Employee #",
            value = uiState.employeeId,
            isActive = uiState.activeField == WorkOrderInputField.EMPLOYEE,
            onClick = onEmployeeClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        SelectableField(
            label = "Work Order #",
            value = uiState.workOrderId,
            isActive = uiState.activeField == WorkOrderInputField.WORK_ORDER,
            onClick = onWorkOrderClick
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = when (uiState.activeField) {
                WorkOrderInputField.EMPLOYEE -> "Ingrese o escanee el número de empleado y use el keypad."
                WorkOrderInputField.WORK_ORDER -> "Ingrese o escanee el número de Work Order y use el keypad."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onClockIn,
            enabled = isClockInEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Clock IN WO",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClockOut,
            enabled = isClockOutEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = "Clock OUT WO",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SelectableField(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val displayValue = if (value.isBlank()) "" else value

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        readOnly = true,
        label = { Text(text = label) },
        placeholder = { Text(text = "--") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            cursorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
}

@Composable
private fun WorkOrdersKeypad(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onClear: () -> Unit,
    onEnter: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ClockOutDialog(
    onDismiss: () -> Unit,
    onConfirm: (qty: String, status: ClockOutStatus) -> Unit
) {
    var qty by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedStatus by rememberSaveable { mutableStateOf(ClockOutStatus.COMPLETE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(qty, selectedStatus) },
                enabled = qty.isNotBlank()
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        title = { Text(text = "Clock OUT WO") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { input -> qty = input.filter { it.isDigit() } },
                    label = { Text(text = "Cantidad") },
                    placeholder = { Text(text = "0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                StatusDropdown(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    selectedStatus = selectedStatus,
                    onStatusSelected = { status ->
                        selectedStatus = status
                        expanded = false
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedStatus: ClockOutStatus,
    onStatusSelected: (ClockOutStatus) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedStatus.displayName,
            onValueChange = {},
            label = { Text(text = "Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            ClockOutStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = status.displayName) },
                    onClick = { onStatusSelected(status) }
                )
            }
        }
    }
}
