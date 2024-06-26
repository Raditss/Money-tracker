package com.example.if3210_2024_android_ppl.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.if3210_2024_android_ppl.R

object DialogUtils {
    fun showLoadingDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.loading_background)
        dialog.show()
        return dialog
    }

    fun showLoginFailedDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_login_fail, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.show()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showNoInternetDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_no_internet, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.show()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showTimeoutDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_timeout, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.show()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
    }

    fun showFileSavedDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_file_saved, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showTransactionSavedDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaction_saved, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showSomethingWentWrongDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_something_went_wrong, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        dialog.show()
        dialogView.findViewById<Button>(R.id.buttonTryAgain).setOnClickListener {
            dialog.dismiss()
        }
    }

}
