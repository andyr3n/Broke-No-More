package com.example.broke_no_more.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.R

class MyDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var goalAmount: EditText
    private lateinit var spentAmount: EditText

    companion object{
        const val DIALOG_KEY = "dialog"
        const val SPENDING_GOAL = 1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        if(dialogId == SPENDING_GOAL){
            val builder = AlertDialog.Builder(requireActivity())
            val view: View = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_spendgoal, null)

            //Initialize editText which user type in for goal and spent amount
            goalAmount = view.findViewById(R.id.goal_editText)
            spentAmount = view.findViewById(R.id.spent_editText)

            builder.setView(view)
            builder.setPositiveButton("Save", this)
            builder.setNegativeButton("Cancel", this)
            ret = builder.create()
        }
        return ret
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            //Get input from EditText
            val goal = goalAmount.text.toString()
            val spent = spentAmount.text.toString()

            //Saved new goal and spending amount
            val sharedPref = requireContext().getSharedPreferences("SPENDING GOAL", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("spendgoal", goal)
            editor.putString("spent amount", spent)
            editor.apply()

            //Update to new goal to view model
            val homeViewModel =
                ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
            homeViewModel.updateSpendingGoal(goal.toDouble())
            activity?.finish()

            Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }
}