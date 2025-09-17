package com.example.terminal

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.terminal.ui.theme.TerminalTheme

@Composable
fun TerminalApp() {
    val context = LocalContext.current
    val inputState = remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LeftPanel(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            )
            KeypadPanel(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                inputValue = inputState.value,
                onNumberClick = { digit -> inputState.value += digit },
                onClear = { inputState.value = "" },
                onEnter = {
                    Toast.makeText(context, inputState.value, Toast.LENGTH_SHORT).show()
                    inputState.value = ""
                }
            )
        }
    }
}

@Composable
private fun LeftPanel(modifier: Modifier = Modifier) {
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
        MenuButton(text = "Clock In/Out")
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton(text = "Work Orders")
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton(text = "Issue Materials")
    }
}

@Composable
private fun MenuButton(text: String) {
    Button(
        onClick = { /* TODO */ },
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

@Preview(showBackground = true, widthDp = 900, heightDp = 450)
@Composable
private fun TerminalAppPreview() {
    TerminalTheme {
        TerminalApp()
    }
}
