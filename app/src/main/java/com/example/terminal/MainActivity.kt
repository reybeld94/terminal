package com.example.terminal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonClockInOut).setOnClickListener {
            startActivity(Intent(this, ClockInOutActivity::class.java))
        }

        findViewById<Button>(R.id.buttonWorkOrders).setOnClickListener {
            startActivity(Intent(this, WorkOrdersActivity::class.java))
        }

        findViewById<Button>(R.id.buttonIssueMaterials).setOnClickListener {
            startActivity(Intent(this, IssueMaterialsActivity::class.java))
        }
    }
}
