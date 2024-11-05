package com.example.broke_no_more.ui.SavingsGoal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.broke_no_more.R

class SavingsGoalFragment : Fragment() {

    private val savingsGoals = mutableListOf<SavingsGoal>()
    private lateinit var totalSavingAmount: TextView
    private lateinit var totalSavingProgress: ProgressBar
    private lateinit var savingDetails: TextView
    private lateinit var savingsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_savings_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        totalSavingAmount = view.findViewById(R.id.totalSavingAmount)
        totalSavingProgress = view.findViewById(R.id.totalSavingProgress)
        savingDetails = view.findViewById(R.id.savingDetails)
        savingsContainer = view.findViewById(R.id.savingsContainer)
        val addMoreGoals: TextView = view.findViewById(R.id.addMoreGoals)

        // Set up add more goals click listener
        addMoreGoals.setOnClickListener {
            showAddGoalDialog()
        }

        // Initialize with no goals
        updateTotalSavings()
    }

    private fun showAddGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null)
        val goalNameInput = dialogView.findViewById<EditText>(R.id.goalNameInput)
        val goalTargetInput = dialogView.findViewById<EditText>(R.id.goalTargetInput)
        val goalSavedInput = dialogView.findViewById<EditText>(R.id.goalSavedInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = goalNameInput.text.toString()
                val target = goalTargetInput.text.toString().toDoubleOrNull() ?: 0.0
                val saved = goalSavedInput.text.toString().toDoubleOrNull() ?: 0.0

                val newGoal = SavingsGoal(name, target, saved)
                savingsGoals.add(newGoal)
                addGoalToUI(newGoal)
                updateTotalSavings()  // Update totals when a new goal is added
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addGoalToUI(goal: SavingsGoal) {
        val goalView = LayoutInflater.from(requireContext()).inflate(R.layout.item_saving_goal, savingsContainer, false)
        val goalTitle: TextView = goalView.findViewById(R.id.savingPlanTitle)
        val goalProgress: ProgressBar = goalView.findViewById(R.id.savingPlanProgress)
        val goalDetails: TextView = goalView.findViewById(R.id.savingPlanDetails)

        goalTitle.text = "${goal.name}: $${goal.target}"
        goalProgress.progress = ((goal.saved / goal.target) * 100).toInt()
        goalDetails.text = "Saved: $${goal.saved}, Left: $${goal.target - goal.saved}"

        savingsContainer.addView(goalView)
    }

    private fun updateTotalSavings() {
        val totalTarget = savingsGoals.sumOf { it.target }
        val totalSaved = savingsGoals.sumOf { it.saved }

        totalSavingAmount.text = "$${totalTarget}"  // Display total target amount
        totalSavingProgress.progress = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt() else 0
        savingDetails.text = "You have saved $${totalSaved} in total"  // Update the saving details text
    }

    data class SavingsGoal(val name: String, val target: Double, val saved: Double)
}

