package com.example.broke_no_more.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.FragmentHomeBinding
import com.example.broke_no_more.ui.SavingsGoal.SavingsGoalFragment
import com.example.broke_no_more.ui.add_expense.AddExpenseFragment
import com.example.broke_no_more.ui.subscription.SubscriptionFragment
import org.w3c.dom.Text
import java.util.Locale

class HomeFragment : Fragment(){

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var editSpending: TextView
    private lateinit var spendingGoalAmount: TextView
    private lateinit var spendingGoalProcess: ProgressBar
    private lateinit var haveSpentAmount: TextView
    private lateinit var moreSaving: TextView
    private lateinit var addExpenseButton: Button
    private lateinit var addSubsriptionButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Edit Spending Goal
        editSpending = binding.editSpendingGoal
        editSpending.setOnClickListener(){
           //Open Dialog Fragment to Edit
            val spendingDialog = MyDialog()
            val bundle = Bundle()
            bundle.putInt(MyDialog.DIALOG_KEY, MyDialog.SPENDING_GOAL)
            spendingDialog.arguments = bundle
            spendingDialog.show(requireActivity().supportFragmentManager, "Edit Spending Dialog")
        }

        val spentAmount = getSpentAmount();//Get saved spent amount
        val spendGoal = getSpendingGoal();//Get saved spending goal

        //Observe and change live data for spending goal
        spendingGoalAmount = binding.spendingGoalAmount
//        homeViewModel.spendingGoal.observe(viewLifecycleOwner) {
//            spendingGoalAmount.text = it.toString()
//        }

        spendingGoalAmount.text = "$" + spendGoal

        //Process bar
        spendingGoalProcess = binding.spendingProgress

        //Calculate percentage of spending goal (How much user spent compared to goal)
        val spendPercentage = (( spentAmount/ spendGoal) * 100).toInt()

        //Set process to correct percentage
        spendingGoalProcess.progress = spendPercentage

        //Update Amount have spent
        haveSpentAmount = binding.haveSpentText
        haveSpentAmount.text = "You have spent $$spentAmount/ $$spendGoal this month"

        moreSaving = binding.savingMoreDetails
        moreSaving.setOnClickListener(){

            //Swap Fragment to SavingGoal
            val savingGoalFragment = SavingsGoalFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, savingGoalFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        addExpenseButton = binding.addExpenseBtn
        addExpenseButton.setOnClickListener(){
            //Swap Fragment to addExpense
            val addExpenseFragment = AddExpenseFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, addExpenseFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        //Swap to SubscriptionFragment
        addSubsriptionButton = binding.addSubscriptionBtn
        addSubsriptionButton.setOnClickListener(){
            val subscriptionFragment = SubscriptionFragment()
            val manager = requireActivity().supportFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_home, subscriptionFragment)
            val homeFragment = manager.findFragmentByTag(HomeFragment::class.java.name)
            if(homeFragment != null)
                transaction.remove(HomeFragment())
            transaction.commit()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Get saved spending goal
    private fun getSpendingGoal(): Double {
        val sharedPref = requireContext().getSharedPreferences("SPENDING GOAL", Context.MODE_PRIVATE)
        val goal = sharedPref.getString("spendgoal", "")
        var goalDouble = 0.0
        if (goal != null) {
            if(goal.isNotEmpty())//Only if user entered a value and saved
                goalDouble = goal.toDouble()//Convert from string to double
        }
        return goalDouble
    }

    //Get saved spent amount
    private fun getSpentAmount(): Double{
        val sharedPref = requireContext().getSharedPreferences("SPENDING GOAL", Context.MODE_PRIVATE)
        val spent = sharedPref.getString("spent amount", "")
        var spentDouble = 0.0
        if (spent != null) {
            if(spent.isNotEmpty())//Only if user entered a value and saved
                spentDouble = spent.toDouble()//Convert from string to double
        }
        return spentDouble
    }
}