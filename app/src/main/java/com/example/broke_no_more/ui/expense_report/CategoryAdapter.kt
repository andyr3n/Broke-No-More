import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categoryData: List<Pair<String, List<Pair<String, Double>>>> = emptyList()

    fun setCategories(categories: List<Pair<String, List<Pair<String, Double>>>>) {
        this.categoryData = categories
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
        val totalTextView: TextView = view.findViewById(R.id.totalTextView)
        val subCategoryLayout: LinearLayout = view.findViewById(R.id.subCategoryLayout)
        val imageView: ImageView = view.findViewById((R.id.expenseIcon))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, subcategories) = categoryData[position]
        val totalAmount = subcategories.sumOf { it.second }
        val imageView = holder.imageView


        holder.categoryTextView.text = category
        holder.totalTextView.text = String.format("$%.2f", totalAmount)


        when (holder.categoryTextView.text) {
            "Housing" -> imageView.setImageResource(R.drawable.ic_housing)
            "Grocery" -> imageView.setImageResource(R.drawable.ic_grocery)
            "Clothes" -> imageView.setImageResource(R.drawable.ic_clothes)
            "Entertainment" -> imageView.setImageResource(R.drawable.ic_entertainment)
            "Subscription" -> imageView.setImageResource(R.drawable.ic_subscription)
            "Miscellaneous" -> imageView.setImageResource(R.drawable.ic_miscellaneous)
            else -> imageView.setImageResource(R.drawable.ic_miscellaneous)
        }

        // Populate subcategories
//        if (subcategories.isNotEmpty()) {
//            holder.subCategoryLayout.visibility = View.VISIBLE
//            holder.subCategoryLayout.removeAllViews()
//
//            subcategories.forEach { (subCategory, amount) ->
//                val textView = TextView(holder.itemView.context)
//                textView.text = "• $subCategory: $${String.format("%.2f", amount)}"
//                textView.setTextColor(holder.itemView.context.getColor(R.color.gray))
//                textView.textSize = 14f
//                holder.subCategoryLayout.addView(textView)
//            }
//        } else {
            holder.subCategoryLayout.visibility = View.GONE

    }

    override fun getItemCount(): Int {
        return categoryData.size
    }
}

