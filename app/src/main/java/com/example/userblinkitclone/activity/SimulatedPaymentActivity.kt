package com.example.userblinkitclone.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.userblinkitclone.databinding.ActivitySimulatedPaymentBinding

class SimulatedPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimulatedPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulatedPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalAmount = intent.getIntExtra("totalAmount", 0)
        binding.tvAmount.text = "₹$totalAmount"

        binding.btnPay.setOnClickListener {
            showSuccessDialog()
        }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Status")
        builder.setMessage("Payment Simulated Successfully")
        builder.setCancelable(false)
        builder.setPositiveButton("Continue") { dialog, _ ->
            dialog.dismiss()
            // Navigate back to OrderPlaceActivity with payment success result
            setResult(RESULT_OK)
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
