package com.example.if3210_2024_android_ppl.ui.setting

import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.if3210_2024_android_ppl.LoginActivity
import com.example.if3210_2024_android_ppl.R
import com.example.if3210_2024_android_ppl.TokenCheckService
import com.example.if3210_2024_android_ppl.database.transaction.Transaction
import com.example.if3210_2024_android_ppl.database.transaction.TransactionDatabase
import com.example.if3210_2024_android_ppl.database.user.UserViewModel
import com.example.if3210_2024_android_ppl.databinding.FragmentSettingBinding
import com.example.if3210_2024_android_ppl.util.DialogUtils
import com.example.if3210_2024_android_ppl.util.EmailSender
import com.example.if3210_2024_android_ppl.util.ExcelFileCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.random.Random

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private lateinit var userViewModel: UserViewModel
    private val db by lazy { TransactionDatabase(requireContext()) }
    private var isOperationInProgress = false
    private var isSaveOperationInProgress = false
    private var isLogoutOperationInProgress = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val randomTransactionReceiver = RandomTransactionReceiver()

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

    private fun sendReport(transactions: List<Transaction>, useXlsxFormat: Boolean) {
        context?.let { ctx ->
            val excelFileCreator = ExcelFileCreator(ctx)
            val emailSender = EmailSender(ctx)

            val fileUri = excelFileCreator.createExcelFile(transactions, useXlsxFormat)
            val mimeType = if (useXlsxFormat) {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            } else {
                "application/vnd.ms-excel"
            }

            userViewModel.getActiveUserEmail { email ->
                emailSender.sendEmailWithAttachment(
                    email?: "13521015@std.stei.itb.ac.id",
                    "Transaction Report",
                    "Here is your transaction report.",
                    fileUri,
                    mimeType
                )
            }
        }
    }

    private fun setupButtonListeners() {
        binding.buttonSendTransaction.setOnClickListener {
            if (!isOperationInProgress) {
                isOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                userViewModel.getActiveUserEmail { email ->
                    if (email != null) {
                        lifecycleScope.launch {
                            try {
                                val job = withTimeoutOrNull(10000) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val transactions = db.transactionDao().getTransactions()
                                        val isXlsxFormat = binding.switchXlsXlsx.isChecked
                                        sendReport(transactions, isXlsxFormat)
                                    }
                                }
                                if (job == null) {
                                    DialogUtils.showTimeoutDialog(requireContext())
                                }
                            } catch (e: TimeoutCancellationException) {
                                DialogUtils.showTimeoutDialog(requireContext())
                            } finally {
                                dialog.dismiss()
                                isOperationInProgress = false
                            }
                        }
                    } else {
                        Log.e("SettingFragment", "Failed to retrieve active user's email")
                        dialog.dismiss()
                        isOperationInProgress = false
                    }
                }
            }
        }

        binding.buttonSaveTransaction.setOnClickListener {
            if (!isSaveOperationInProgress) {
                isSaveOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        val transactions = db.transactionDao().getTransactions()
                        val isXlsxFormat = binding.switchXlsXlsx.isChecked
                        val fileName = "Transactions_${System.currentTimeMillis()}" + if (isXlsxFormat) ".xlsx" else ".xls"
                        val mimeType = if (isXlsxFormat) {
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        } else {
                            "application/vnd.ms-excel"
                        }
                        val excelFileCreator = ExcelFileCreator(requireContext())
                        val fileUri = excelFileCreator.createExcelFile(transactions, isXlsxFormat)

                        val savedFilePath = saveFileToExternalStorage(fileName, mimeType, fileUri)
                        Log.d("SettingFragment", savedFilePath.toString())
                        withContext(Dispatchers.Main) {
                            DialogUtils.showFileSavedDialog(requireContext())
                        }
                    } finally {
                        dialog.dismiss()
                        isSaveOperationInProgress = false
                    }
                }
            }
        }

        binding.buttonBelow.setOnClickListener {
            if (!isLogoutOperationInProgress) {
                isLogoutOperationInProgress = true
                val dialog = DialogUtils.showLoadingDialog(requireContext())
                lifecycleScope.launch {
                    try {
                        activity?.stopService(Intent(context, TokenCheckService::class.java))
                        withContext(Dispatchers.IO) {
                            userViewModel.logout()
                        }
                        withContext(Dispatchers.Main) {
                            val intent = Intent(activity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }
                    } finally {
                        dialog.dismiss()
                        isLogoutOperationInProgress = false
                    }
                }
            }
        }

        binding.buttonRandomizeTransaction.setOnClickListener {
            val randomTitle = generateRandomTitle()
            val randomQuantity = generateRandomQuantity()
            val randomPrice = generateRandomPrice()
            val randomCategory = generateRandomCategory()
            Log.d("Main Activity", "dbResponse: $randomTitle")

            // Broadcast intent with random transaction details
            val intent = Intent("com.example.if3210_2024_android_ppl.RANDOM_TRANSACTION")
            intent.putExtra("title", randomTitle)
            intent.putExtra("quantity", randomQuantity)
            intent.putExtra("price", randomPrice)
            intent.putExtra("category", randomCategory)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

            findNavController().navigate(R.id.navigation_addTransaction)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity","dbResponse: masuk")
        val filter = IntentFilter("com.example.if3210_2024_android_ppl.RANDOM_TRANSACTION")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(randomTransactionReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity","dbResponse: keluar")
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(randomTransactionReceiver)
    }

    private fun generateRandomTitle(): String {
        val titles = listOf("Groceries", "Electronics", "Clothing", "Books", "Restaurant", "Travel")
        return titles.random()
    }

    private fun generateRandomQuantity(): Int {
        return Random.nextInt(1, 101)
    }

    private fun generateRandomPrice(): Double {
        return String.format("%.2f", Random.nextDouble(1.0, 1000.0)).toDouble()
    }

    private fun generateRandomCategory(): String {
        val category = listOf("Pembelian", "Pemasukan")
        return category.random()
    }

    private fun saveFileToExternalStorage(fileName: String, mimeType: String, fileUri: Uri) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                val inputStream = resolver.openInputStream(fileUri)
                inputStream?.copyTo(outputStream!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}