package com.example.broke_no_more.ui.SavingsGoal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.R
import com.example.broke_no_more.database.ExpenseDatabase
import com.example.broke_no_more.database.ExpenseDatabaseDao
import com.example.broke_no_more.database.ExpenseRepository
import com.example.broke_no_more.database.ExpenseViewModel
import com.example.broke_no_more.database.ExpenseViewModelFactory
import com.example.broke_no_more.databinding.FragmentSavingsGoalBinding
import com.example.broke_no_more.ui.savings_goal.SavingGoalViewModel
import kotlin.math.absoluteValue

class SavingsGoalFragment : Fragment() {
    private var _binding: FragmentSavingsGoalBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: ExpenseDatabase
    private lateinit var databaseDao: ExpenseDatabaseDao
    private lateinit var repository: ExpenseRepository
    private lateinit var viewModelFactory: ExpenseViewModelFactory
    private lateinit var expenseViewModel: ExpenseViewModel

    private lateinit var clothesAmount: TextView
    private lateinit var entertainmentAmount: TextView
    private lateinit var uncategorizedAmount: TextView

    private lateinit var clothesDetail: TextView
    private lateinit var entertainmentDetail: TextView
    private lateinit var uncategorizedDetail: TextView

    private lateinit var clothesProgress: ProgressBar
    private lateinit var entertainmentProgress: ProgressBar
    private lateinit var uncategorizedProgress: ProgressBar

    private lateinit var clothesGoalLayout: ConstraintLayout
    private lateinit var entertainmentGoalLayout: ConstraintLayout
    private lateinit var uncategorizedGoalLayout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSavingsGoalBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //return inflater.inflate(R.layout.fragment_savings_goal, container, false)

        // Initialize views
        clothesAmount = binding.clothesAmount
        entertainmentAmount = binding.entertainmentAmount
        uncategorizedAmount = binding.uncategorizedAmount

        clothesDetail = binding.clothesDetail
        entertainmentDetail = binding.entertainmentDetail
        uncategorizedDetail = binding.uncategorizedDetail

        clothesProgress = binding.clothesProgress
        entertainmentProgress = binding.entertainmentProgress
        uncategorizedProgress = binding.uncategorizedProgress

        clothesGoalLayout = binding.clothesGoal
        entertainmentGoalLayout = binding.entertainmentGoal
        uncategorizedGoalLayout = binding.uncategorizedGoal

        //Declare variables for database
        database = ExpenseDatabase.getInstance(requireActivity())
        databaseDao = database.expenseDatabaseDao
        repository = ExpenseRepository(databaseDao)
        viewModelFactory = ExpenseViewModelFactory(repository)
        expenseViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ExpenseViewModel::class.java]

        //Get saved goal for each category
        val clothesGoal = getClothesGoal()
        val entertainmentGoal = getEntertainmentGoal()
        val uncategorizedGoal = getUncategorizedGoal()
        println("Uncategorized Goal = $uncategorizedGoal")

        //Set Goal to save goal amount
        clothesAmount.text = "$$clothesGoal"
        entertainmentAmount.text = "$$entertainmentGoal"
        uncategorizedAmount.text = "$$uncategorizedGoal"

        //Tap to Change goal for each category
        clothesGoalLayout.setOnClickListener{ changeGoal("clothes") }
        entertainmentGoalLayout.setOnClickListener{ changeGoal("entertainment") }
        uncategorizedGoalLayout.setOnClickListener{changeGoal("other")}

        //Get saved total amount for each category
        val sharedPref = requireContext().getSharedPreferences("total spent", Context.MODE_PRIVATE)
        val clothesSpent = sharedPref.getFloat("clothes spent", 0.0F).toDouble()
        val entertainmentSpent = sharedPref.getFloat("entertainment spent", 0.0F).toDouble()
        val uncategorizedSpent = sharedPref.getFloat("other spent", 0.0F).toDouble()

        //Change the budget left (As default if have a goal amount)
        if(clothesGoal != 0.0){
            val amountLeft = clothesGoal - clothesSpent
            if(amountLeft <= 0.0)
                clothesDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
            else
                clothesDetail.text = "You have $$amountLeft left in your budget !"

            //Change Progress bar
            clothesProgress.progress = ((clothesSpent / clothesGoal) * 100).toInt()
        }

        if(entertainmentGoal != 0.0){
            val amountLeft = entertainmentGoal - entertainmentSpent
            if(amountLeft <= 0.0)
                entertainmentDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
            else
                entertainmentDetail.text = "You have $$amountLeft left in your budget !"

            //Change Progress bar
            entertainmentProgress.progress = ((entertainmentSpent / entertainmentGoal) * 100).toInt()
        }

        if( uncategorizedGoal != 0.0){
            val amountLeft = uncategorizedGoal - uncategorizedSpent
            if(amountLeft <= 0.0)
                uncategorizedDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
            else
                uncategorizedDetail.text = "You have $$amountLeft left in your budget !"

            //Change Progress bar
            uncategorizedProgress.progress = ((uncategorizedSpent / uncategorizedGoal) * 100).toInt()
        }

        //Observe every time add a new entry or delete an entry to change total amount for each category
        expenseViewModel.allEntriesLiveData.observe(viewLifecycleOwner) { expense ->
            //Group expense by group
            val categoryTotals = expense.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            //Loop through each category and save new total amount
            categoryTotals.forEach{(category, totalCategoryAmount) ->
                when(category){
                    "Clothes" -> {
                        val editor = sharedPref.edit()
                        editor.putFloat("clothes spent", totalCategoryAmount.toFloat())
                        editor.apply()
                    }
                    "Entertainment" -> {
                        val editor = sharedPref.edit()
                        editor.putFloat("entertainment spent", totalCategoryAmount.toFloat())
                        editor.apply()
                    }
                    "Miscellaneous" -> {
                        val editor = sharedPref.edit()
                        editor.putFloat("other spent", totalCategoryAmount.toFloat())
                        editor.apply()
                    }
                }
            }
        }


        val savingGoalViewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]
        savingGoalViewModel.clothesGoal.observe(viewLifecycleOwner){
            clothesAmount.text = "$$it"//Update goal amount

            if(it > 0.0){
                //Update amount left
                val amountLeft = it - clothesSpent
                if(amountLeft <= 0.0){
                    clothesDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
                }
                else{
                    clothesDetail.text = "You have $$amountLeft left in your budget !"
                }
                //Update progress
                clothesProgress.progress = ((clothesSpent / it) * 100).toInt()            }
        }

        savingGoalViewModel.entertainmentGoal.observe(viewLifecycleOwner){
            entertainmentAmount.text = "$$it"//Update goal amount

            if(it > 0.0){
                //Update amount left
                val amountLeft = it - entertainmentSpent
                if(amountLeft <= 0.0)
                    entertainmentDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
                else
                    entertainmentDetail.text = "You have $$amountLeft left in your budget !"

                //Update progress
                entertainmentProgress.progress = ((clothesSpent / it) * 100).toInt()
            }
        }

        savingGoalViewModel.uncategorizedGoal.observe(viewLifecycleOwner){
            uncategorizedAmount.text = "$$it"//Update goal amount

            if( it > 0.0){
                //Update amount left
                val amountLeft = it - uncategorizedSpent
                if(amountLeft <= 0.0)
                    uncategorizedDetail.text = "You have exceeded your budget by ${amountLeft.absoluteValue} !"
                else
                    uncategorizedDetail.text = "You have $$amountLeft left in your budget !"

                //Update progress
                uncategorizedProgress.progress = ((uncategorizedSpent / it) * 100).toInt()
            }
        }
        return root
    }

    //Build dialog fragment to change goal amount
    private fun changeGoal(name: String){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_goal, null)
        val newGoal = dialogView.findViewById<EditText>(R.id.newGoal)

        //Build a dialog to input new goal
        AlertDialog.Builder(requireContext())
            .setTitle("Change $name goal amount")//Set title
            .setView(dialogView)
            .setPositiveButton("Save"){_, _ ->
                //Save new goal input
                val newGoalInput = newGoal.text.toString().toFloat()
                val sharedPref = requireContext().getSharedPreferences(name, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putFloat(name, newGoalInput)
                editor.apply()

                //Send new update value to View Model
                val savingGoalViewModel = ViewModelProvider(requireActivity())[SavingGoalViewModel::class.java]
                when(name){
                    "clothes" -> savingGoalViewModel.updateClothesGoal(newGoalInput.toDouble())
                    "entertainment" -> savingGoalViewModel.updateEntertainmentGoal(newGoalInput.toDouble())
                    "other" -> savingGoalViewModel.updateUncategorizedGoal(newGoalInput.toDouble())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getClothesGoal(): Double{
        val sharedPref = requireContext().getSharedPreferences("clothes", Context.MODE_PRIVATE)
        val clothesGoal = sharedPref.getFloat("clothes", 0.0F).toDouble()
        return clothesGoal
    }

    private fun getEntertainmentGoal(): Double{
        val sharedPref = requireContext().getSharedPreferences("entertainment", Context.MODE_PRIVATE)
        val entertainmentGoal = sharedPref.getFloat("entertainment", 0.0F).toDouble()
        return entertainmentGoal
    }

    private fun getUncategorizedGoal(): Double{
        val sharedPref = requireContext().getSharedPreferences("other", Context.MODE_PRIVATE)
        val otherGoal = sharedPref.getFloat("other", 0.0F).toDouble()
        return otherGoal
    }
}

