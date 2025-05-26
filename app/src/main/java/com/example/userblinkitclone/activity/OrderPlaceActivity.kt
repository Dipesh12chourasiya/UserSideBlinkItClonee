package com.example.userblinkitclone.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.userblinkitclone.CartListner
import com.example.userblinkitclone.Constants
import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.adapters.AdapterCartProduct
import com.example.userblinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.userblinkitclone.databinding.AddressLayoutBinding
import com.example.userblinkitclone.models.Order
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.roomDB.CartProducts
import com.example.userblinkitclone.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import okio.HashingSink.sha256
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class OrderPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderPlaceBinding

    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProduct: AdapterCartProduct

    private lateinit var b2BPGRequest : B2BPGRequest
    private var totalPrice = 0

    private var cartListner : CartListner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)

        this.window.statusBarColor = Color.TRANSPARENT

        setContentView(binding.root)

        getAllCartProducts()
//        initializePhonePay()
        onPlaceOrderClicked()
    }

    private fun initializePhonePay() {
        val data = JSONObject()
        PhonePe.init(this, PhonePeEnvironment.UAT, Constants.MERCHANTID, "")

        data.put("merchantId", Constants.MERCHANTID)
        data.put("merchantTransactionId",Constants.merchantTransactionId)
        data.put("amount",200)
        data.put("mobileNumber","8839990051")
        data.put("callbackUrl","https://webhook.site/callback-ur")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type","UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")

        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOs","ANDROID")

        data.put("deviceContext",deviceContext)

        val payloadBase64 = android.util.Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1";
//        val rawString = payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY
//        val checksum = "sha256_" + generateHmacSHA256(rawString, Constants.SALT_KEY) + "###1"


        Log.d("some", "onCreate: $payloadBase64")
        Log.d("some", "onCreate: $checksum")

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

//    fun generateHmacSHA256(data: String, key: String): String {
//        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
//        val mac = Mac.getInstance("HmacSHA256")
//        mac.init(secretKey)
//        val hmacBytes = mac.doFinal(data.toByteArray())
//        return hmacBytes.joinToString("") { "%02x".format(it) }
//    }

    private val paymentSimulationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
//            Utils.showToast(this@OrderPlaceActivity, "Result is Ok")
            simulatePaymentStatus()
        }
    }

    private fun simulatePaymentStatus() {
        lifecycleScope.launch {
            viewModel.setPaymentStatus(true)

            viewModel.paymentStatus.collect { status ->
                if (status) {

                    // order save delete product
                    saveOrder()

                    // delete the cart productss
                    lifecycleScope.launch {
                        viewModel.deleteAllCartProducts()
                        viewModel.savingCartItemCount(0)
                    }

                    cartListner?.hideCartLayout()

                    Utils.showToast(this@OrderPlaceActivity, "Payment Successfull")
                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                    finish()
                } else{
                    Utils.showToast(this@OrderPlaceActivity, "Payment Failed")
                }
            }
        }
    }

    private fun saveOrder() {
        lifecycleScope.launch {
            viewModel.getAllCartProducts().observe(this@OrderPlaceActivity){ cartProductList ->

                if(!cartProductList.isEmpty()){

                    viewModel.getUserAddress { address ->
                        val order = Order(
                            orderId = Utils.getRandId(),
                            userAddress = address,
                            orderList = cartProductList,
                            orderStatus = 0,
                            orderDate = Utils.getCurrentDate(),
                            orderingUserId = Utils.getCurrentUserId()
                        )

                        viewModel.saveOrderedProduct(order)
                    }

                    for(product in cartProductList){
                        val count = product.productCount
                        val stock = product.productStock!! - count!!

                        viewModel.saveProductsAfterOrder(stock, product)
                    }
                }
            }
        }
    }


    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {

            viewModel.getAddressStatus().observe(this){ status ->
                if(status){ // agr status == true hai means address exist in the db so start payment work
//  https://developer.phonepe.com/v1/docs/android-pg-sdk-integration/  doc for implimenting
                    // payment work

//                    getPaymentView() // instead of this i am replacing it with

                    val intent = Intent(this, SimulatedPaymentActivity::class.java)
                    intent.putExtra("totalAmount", totalPrice)
                    paymentSimulationLauncher.launch(intent)

                } else {
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog = AlertDialog.Builder(this).setView(addressLayoutBinding.root).create()

                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener{
                        saveAddress(alertDialog, addressLayoutBinding)
                    }
                }
            }
        }
    }


//    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//        if(it.resultCode == RESULT_OK){
//            checkStatus()
//        }
//    }


//    private fun checkStatus() {
//        // SIMULATED PAYMENT SUCCESS
//        lifecycleScope.launch {
//            viewModel.setPaymentStatus(true) // simulate success
//
//            viewModel.paymentStatus.collect { status ->
//                if (status) {
//                    showPaymentSuccessDialog()
////                    Utils.showToast(this@OrderPlaceActivity, "Payment Simulated Successfully")
////                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
////                    finish()
//                } else {
//                    Utils.showToast(this@OrderPlaceActivity, "Payment Failed")
//                }
//            }
//        }
//    }

    // for showing the payment status
    private fun showPaymentSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Status")
        builder.setMessage("Payment Simulated Successfully")
        builder.setCancelable(false) // prevent dismiss by tapping outside
        builder.setPositiveButton("Continue") { dialog, _ ->
            dialog.dismiss()
            startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
            finish()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }



//    private fun checkStatus() {
//        val xVerify = sha256("/pg/v1/status/${Constants.MERCHANTID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
//        val headers = mapOf(
//            "Content-Type" to "application/json",
//            "X-VERIFY" to xVerify,
//            "X-MERCHANT-ID" to Constants.MERCHANTID,
//        )
//        lifecycleScope.launch {
//            viewModel.checkPaymentStatus(headers)
//
//            viewModel.paymentStatus.collect{ status ->
//                if (status){
//                    Utils.showToast(this@OrderPlaceActivity, "Payment Successfull")
//                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
//                    finish()
//                } else{
//                    Utils.showToast(this@OrderPlaceActivity, "Payment Unuccessfull")
//
//                }
//            }
//        }
//    }

//    private fun getPaymentView() {
//        Utils.showToast(this, "Simulating payment flow...")
//        checkStatus() // directly call it to simulate payment
////        try {
////            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")?.let {
////                phonePayView.launch(it)
////                Utils.showToast(this, "PhonePe Intent Created")
////            } ?: Utils.showToast(this, "Failed to create PhonePe intent")
////
////        } catch (e: PhonePeInitException){
////            Utils.showToast(this,e.message.toString())
////        }
//    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing...")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"


        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }

        alertDialog.dismiss()
        Utils.hideDialog()
        Utils.showToast(this,"Address Saved")

        val intent = Intent(this, SimulatedPaymentActivity::class.java)
        intent.putExtra("totalAmount", totalPrice)
        paymentSimulationLauncher.launch(intent)
    }

    private fun getAllCartProducts() {
        lifecycleScope.launch {
            viewModel.getAllCartProducts().observe(this@OrderPlaceActivity){ cartProducts -> // list of cart products
                adapterCartProduct = AdapterCartProduct()
                binding.rvProductsItems.adapter = adapterCartProduct
                adapterCartProduct.differ.submitList(cartProducts)

                totalPrice = 0

                for (product in cartProducts) {
                    val priceString = product.productPrice!!.filter { it.isDigit() }  // Extracts only digits
                    val price = priceString.toIntOrNull() ?: 0

                    val itemCount = product.productCount!!.toInt()

                    totalPrice += price * itemCount
                }

                binding.tvSubTotal.text = "₹${totalPrice}"

                if(totalPrice < 200){
                    binding.tvDeliveryCharge.text = "₹20"
                    totalPrice += 20
                }

                binding.tvGrandTotal.text = "₹$totalPrice"

            }
        }
    }
}