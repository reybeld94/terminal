package com.example.terminal.ui.workorders

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.terminal.data.network.ClockOutStatus
import com.example.terminal.data.repository.UserStatus
import com.example.terminal.ui.theme.TerminalBackgroundBottom
import com.example.terminal.ui.theme.TerminalBackgroundTop
import com.example.terminal.ui.theme.TerminalHelperText
import com.example.terminal.ui.theme.TerminalKeypadBackground
import com.example.terminal.ui.theme.TerminalKeypadButton
import com.example.terminal.ui.theme.TerminalKeypadClear
import com.example.terminal.ui.theme.TerminalKeypadEnter

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
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(TerminalBackgroundTop, TerminalBackgroundBottom)
                    )
                )
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.Top
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
    val isClockInEnabled = uiState.isEmployeeValidated &&
        uiState.employeeId.isNotBlank() &&
        uiState.workOrderId.isNotBlank() &&
        !uiState.isLoading
    val isClockOutEnabled = isClockInEnabled && !uiState.isLoading
    val employeeInstruction = "Enter your Employee ID and press Enter on the keypad to validate."
    val workOrderInstruction = "Use the keypad to enter or scan the assembly number and press Enter to continue."
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!uiState.isEmployeeValidated) {
            StepHeading(
                title = "Please enter or scan your user ID",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            SelectableField(
                label = "Employee #",
                value = uiState.employeeId,
                isActive = uiState.activeField == WorkOrderInputField.EMPLOYEE,
                onClick = onEmployeeClick,
                enabled = !uiState.isLoading,
                isError = uiState.employeeValidationError != null
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = employeeInstruction,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TerminalHelperText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.employeeValidationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.employeeValidationError,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
            if (uiState.userStatus != null) {
                EmployeeStatusCard(
                    status = uiState.userStatus,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            StepHeading(
                title = "Please enter or scan your assembly number",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            SelectableField(
                label = "Assembly #",
                value = uiState.workOrderId,
                isActive = uiState.activeField == WorkOrderInputField.WORK_ORDER,
                onClick = onWorkOrderClick,
                enabled = uiState.isEmployeeValidated && !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = workOrderInstruction,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TerminalHelperText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                Button(
                    onClick = onClockIn,
                    enabled = isClockInEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "Clock IN WO",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onClockOut,
                    enabled = isClockOutEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isClockOutEnabled) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = "Clock OUT WO",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepHeading(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = TerminalHelperText,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SelectableField(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val displayValue = if (value.isBlank()) "" else value

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (enabled) {
                    it.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    it
                }
            },
        readOnly = true,
        enabled = enabled,
        isError = isError,
        label = { Text(text = label, fontWeight = FontWeight.Medium) },
        placeholder = { Text(text = "--", fontWeight = FontWeight.Medium) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            cursorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}

@Composable
private fun EmployeeStatusCard(
    status: UserStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Employee",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${status.firstName} ${status.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ID: ${status.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            val workOrder = status.activeWorkOrder
            if (workOrder != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Active Work Order",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    WorkOrderDetailRow("Collection ID", workOrder.workOrderCollectionId?.toString())
                    WorkOrderDetailRow("Work Order #", workOrder.workOrderNumber)
                    WorkOrderDetailRow("Assembly #", workOrder.workOrderAssemblyNumber)
                    WorkOrderDetailRow("Clock In", workOrder.clockInTime)
                    WorkOrderDetailRow("Part #", workOrder.partNumber)
                    WorkOrderDetailRow("Operation Code", workOrder.operationCode)
                    WorkOrderDetailRow("Operation Name", workOrder.operationName)
                }
            } else {
                Text(
                    text = "No active work order",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WorkOrderDetailRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "--",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun WorkOrdersKeypad(
    modifier: Modifier = Modifier,
    onNumberClick: (String) -> Unit,
    onClear: () -> Unit,
    onEnter: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(0.95f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = TerminalKeypadBackground,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        val keypadItems = buildList {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9").forEach { digit ->
                add(
                    KeypadItem(
                        label = digit,
                        onClick = { onNumberClick(digit) },
                        backgroundColor = TerminalKeypadButton,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
            }
            add(
                KeypadItem(
                    label = "Clear",
                    onClick = onClear,
                    backgroundColor = TerminalKeypadClear,
                    contentColor = TerminalHelperText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            add(
                KeypadItem(
                    label = "0",
                    onClick = { onNumberClick("0") },
                    backgroundColor = TerminalKeypadButton,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )
            add(
                KeypadItem(
                    label = "Enter",
                    onClick = onEnter,
                    backgroundColor = TerminalKeypadEnter,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    borderColor = Color.Transparent
                )
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(keypadItems, key = { it.label }) { item ->
                KeypadButton(item)
            }
        }
    }
}

@Composable
private fun KeypadButton(
    item: KeypadItem
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = item.backgroundColor,
        contentColor = item.contentColor,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
        border = item.borderColor?.let { BorderStroke(1.dp, it) },
        onClick = item.onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.label,
                fontSize = item.fontSize,
                textAlign = TextAlign.Center,
                color = item.contentColor,
                fontWeight = item.fontWeight
            )
        }
    }
}

private data class KeypadItem(
    val label: String,
    val onClick: () -> Unit,
    val backgroundColor: Color,
    val contentColor: Color,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val borderColor: Color?
)

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
