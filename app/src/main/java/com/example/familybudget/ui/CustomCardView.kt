package com.example.familybudget.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.familybudget.R
import com.google.android.material.card.MaterialCardView

class CustomCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val labelTextView: TextView
    private val valueTextView: TextView
    private val dropdownMenuButton: ImageButton

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        labelTextView = view.findViewById(R.id.Cardlabel)
        valueTextView = view.findViewById(R.id.Cardvalue)
        dropdownMenuButton = view.findViewById(R.id.dropdownMenuButton)

        dropdownMenuButton.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.card_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClickListener?.onClick(this)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClickListener?.onClick(this)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun setValueText(value: String) {
        valueTextView.text = "$$value"
    }

    fun setLabelText(label: String) {
        labelTextView.text = label
    }

    fun setLabelTextColor(color: Int) {
        labelTextView.setTextColor(color)
    }

    fun setValueTextColor(color: Int) {
        valueTextView.setTextColor(color)
    }

    fun showOptionsButton(show: Boolean) {
        dropdownMenuButton.visibility = if (show) View.VISIBLE else View.GONE
        val params = valueTextView.layoutParams as ConstraintLayout.LayoutParams
        params.marginEnd = if (show) {
            context.resources.getDimensionPixelSize(R.dimen.margin_with_button)
        } else {
            context.resources.getDimensionPixelSize(R.dimen.margin_without_button)
        }
        valueTextView.layoutParams = params
    }

    private var onEditClickListener: OnClickListener? = null
    private var onDeleteClickListener: OnClickListener? = null

    fun setOnEditClickListener(listener: OnClickListener) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: OnClickListener) {
        onDeleteClickListener = listener
    }

    override fun setCardBackgroundColor(color: Int) {
        super.setCardBackgroundColor(color)
    }

    fun getValueText(): String {
        return valueTextView.text.toString()
    }
}