package com.example.broke_no_more.ui.subscription

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.FragmentSubscriptionBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionFragment: Fragment(), DatePickerDialog.OnDateSetListener {
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var addPaymentBtn: FloatingActionButton
    private val paymentDue = mutableListOf<PaymentDue>()
    private lateinit var paidSection: LinearLayout
    private lateinit var totalAmountSubscription: TextView
    private var num = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        paidSection = binding.subscriptionContainer
        totalAmountSubscription = binding.totalAmountSubsciption

        addPaymentBtn = binding.addSubsciptionButton
        addPaymentBtn.setOnClickListener(){
            addPaymentDue()
        }

        return root
    }

    private fun addPaymentDue(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment_due, null)
        val paymentName = dialogView.findViewById<EditText>(R.id.edit_payment_name)
        val paymentDate = dialogView.findViewById<Button>(R.id.button_chooseDueDate)
        val choosenDate = dialogView.findViewById<TextView>(R.id.text_choosen_date)
        val paymentAmount = dialogView.findViewById<EditText>(R.id.edit_payment_amount)

        var date = Calendar.getInstance().time//Choose current time as default

        paymentDate.setOnClickListener(){
            //Show DatePickerDialog to choose Date
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(),{ _, selectedYear, selectedMonth, selectedDay ->
                //Set date to chosen date
                calendar.set(selectedYear, selectedMonth, selectedDay)
                date = calendar.time

                //Set format to day month (in text), year
                choosenDate.setText(SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(date))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Subscription")//Set title
            .setView(dialogView)
            .setPositiveButton("Save"){_, _ ->
                //Define name, amount and amount from user
                var name = paymentName.text.toString()
                val amount = paymentAmount.text.toString().toDoubleOrNull()?: 0.0

                //Create new object paymentDue with info from input
                val newPaymentDue = PaymentDue(name, date, amount)
                paymentDue.add(newPaymentDue)

                //Add default name if user does not enter anything
                if(name.isEmpty()){
                    num++
                    name = "Subscription $num"
                }

                displayPaymentDue(name, date, amount)//Display new added info to UI
                updateTotal()//Update total after added new subscription
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //Display new added subscription to UI
    private fun displayPaymentDue(name: String, date: Date, amount: Double){
        val paymentView = layoutInflater.inflate(R.layout.subscription_list, null)
        val paymentName = paymentView.findViewById<TextView>(R.id.payment_name)
        val paymentDue = paymentView.findViewById<TextView>(R.id.payment_due)
        val paymentAmount = paymentView.findViewById<TextView>(R.id.payment_amount)

        paymentName.text = name
        paymentDue.setText(SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(date))
        paymentAmount.text = "$$amount"

        paidSection.addView(paymentView)//Add new payment dynamically
    }

    private fun updateTotal(){
        //Update total amount spent for subscription this month
        val total = paymentDue.sumOf { it.amount ?:0.0 }
        totalAmountSubscription.text = "$$total"
    }

    data class PaymentDue(val name: String, val date: Date, val amount: Double?)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        println("Selected date is $dayOfMonth/ $month/ $year")
    }
}