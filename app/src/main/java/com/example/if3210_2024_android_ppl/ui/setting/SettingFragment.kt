package com.example.if3210_2024_android_ppl.ui.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding
import com.example.if3210_2024_android_ppl.util.EmailSender
import com.example.if3210_2024_android_ppl.util.ExcelFileCreator

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private lateinit var userViewModel: UserViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        setupButtonListeners()

        return root
    }

    private fun sendReport(transactions: List<Transaction>) {
        context?.let { ctx ->
            val excelFileCreator = ExcelFileCreator(ctx)
            val emailSender = EmailSender(ctx)

            val fileUri = excelFileCreator.createExcelFile(transactions)
            emailSender.sendEmailWithAttachment(
                "dummy@example.com",
                "Transaction Report",
                "Here is your transaction report.",
                fileUri
            )
        }
    }

    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            // TODO: Handle send transaction button click
            val transaction = listOf<Transaction>()
            sendReport(transaction)
            Log.d("SettingFragment", "on click")
        }

        binding.buttonSaveTransaction.setOnClickListener {
            // TODO: Handle save transaction button click
        }

        binding.buttonBelow.setOnClickListener {
            // TODO: Handle logout button click
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}