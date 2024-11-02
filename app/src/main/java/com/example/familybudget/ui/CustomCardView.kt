package com.example.familybudget.ui

import com.example.familybudget.R

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class CustomCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val labelTextView: TextView
    private val valueTextView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        labelTextView = view.findViewById(R.id.Cardlabel)
        valueTextView = view.findViewById(R.id.Cardvalue)

        val cardCornerRadius = context.resources.getDimension(R.dimen.card_corner_radius)
        radius = cardCornerRadius.toFloat()
        elevation = context.resources.getDimension(R.dimen.card_elevation)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomCardView)
        try {
            val backgroundColor = attributes.getColor(
                R.styleable.CustomCardView_cardBackgroundColor,
                context.getColor(R.color.primary)
            )
            val textColor = attributes.getColor(
                R.styleable.CustomCardView_cardTextColor,
                context.getColor(R.color.white)
            )
            val labelText = attributes.getString(R.styleable.CustomCardView_cardLabelText) ?: "0"
            val valueText = attributes.getString(R.styleable.CustomCardView_cardValueText) ?: "0"

            setCardBackgroundColor(backgroundColor)
            labelTextView.setTextColor(textColor)
            valueTextView.setTextColor(textColor)
            labelTextView.text = labelText
            valueTextView.text = valueText
        } finally {
            attributes.recycle()
        }
    }

    fun setValueText(value: String) {
        valueTextView.text = value
    }

    fun setLabelText(label: String) {
        labelTextView.text = label
    }

    override fun setCardBackgroundColor(color: Int) {
        super.setCardBackgroundColor(color)
    }
}