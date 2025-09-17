package com.example.terminal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var displayTextView: TextView
    private val inputBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayTextView = findViewById(R.id.displayTextView)

        val numberButtons = listOf(
            R.id.button0 to "0",
            R.id.button1 to "1",
            R.id.button2 to "2",
            R.id.button3 to "3",
            R.id.button4 to "4",
            R.id.button5 to "5",
            R.id.button6 to "6",
            R.id.button7 to "7",
            R.id.button8 to "8",
            R.id.button9 to "9"
        )

        numberButtons.forEach { (buttonId, value) ->
            findViewById<Button>(buttonId).setOnClickListener {
                appendToInput(value)
            }
        }

        findViewById<Button>(R.id.buttonClear).setOnClickListener {
            clearInput()
        }

        findViewById<Button>(R.id.buttonEnter).setOnClickListener {
            showEnteredValue()
        }
    }

    private fun appendToInput(value: String) {
        inputBuilder.append(value)
        displayTextView.text = inputBuilder.toString()
    }

    private fun clearInput() {
        inputBuilder.clear()
        displayTextView.text = ""
    }

    private fun showEnteredValue() {
        Toast.makeText(this, inputBuilder.toString(), Toast.LENGTH_SHORT).show()
    }
}
