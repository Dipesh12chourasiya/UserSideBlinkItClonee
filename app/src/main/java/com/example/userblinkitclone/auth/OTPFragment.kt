package com.example.userblinkitclone.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.example.userblinkitclone.R
import com.example.userblinkitclone.Utils
import com.example.userblinkitclone.activity.UsersMainActivity
import com.example.userblinkitclone.databinding.FragmentOTPBinding
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {

    private lateinit var binding: FragmentOTPBinding
    private var userNumber: String? = null
    private  val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOTPBinding.inflate(layoutInflater)
        requireActivity().window.statusBarColor = Color.TRANSPARENT

        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing you...")
            val editTexts = arrayOf(binding.et0tp1,binding.et0tp2, binding.et0tp3, binding.et0tp4, binding.et0tp5, binding.et0tp6)

            // for joining the digits of all edittexts
            val otp = editTexts.joinToString("") {
                it.text.toString()
            }

            if (otp.length < editTexts.size){
                Utils.showToast(requireContext(),"Please enter valid otp")
            } else{

                editTexts.forEach {
                    it.text?.clear()
                    it.clearFocus()
                }

                varifyOTP(otp) // varify the otp
            }
        }
    }

    private fun varifyOTP(otp: String) {

        val user = Users(uid = null, userPhoneNumber =  userNumber, userAddress = " ")

        viewModel.signInWithPhoneAuthCredential(otp, userNumber!!, user)

        lifecycleScope.launch {
            viewModel.isSignedInSuccfully.collect{
                if(it == true){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Logged In...")

                    startActivity(Intent(requireActivity(),UsersMainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP...")
        viewModel.apply {
            sendOTP(userNumber!!,requireActivity())

            lifecycleScope.launch {
                otpSent.collect{
                    if(it == true){
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "OTP sent...")
                    }
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_signinFragment)
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(binding.et0tp1,binding.et0tp2, binding.et0tp3, binding.et0tp4, binding.et0tp5, binding.et0tp6)

        for(i in editTexts.indices){
            editTexts[i].addTextChangedListener( object: TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if(s?.length == 1){
                        if(i < editTexts.size - 1){
                            editTexts[i+1].requestFocus()
                        }
                    } else if(s?.length == 0){
                        if(i > 0){
                            editTexts[i-1].requestFocus()
                        }
                    }
                }

            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()

        binding.tvUserNumber.text = userNumber
    }

}