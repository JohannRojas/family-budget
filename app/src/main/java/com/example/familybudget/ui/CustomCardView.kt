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
        // Infla el layout personalizado
        val view = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        labelTextView = view.findViewById(R.id.Cardlabel)
        valueTextView = view.findViewById(R.id.Cardvalue)

        val cardCornerRadius = context.resources.getDimension(R.dimen.card_corner_radius)
        radius = cardCornerRadius.toFloat()
        elevation = context.resources.getDimension(R.dimen.card_elevation)

        // Obtiene los atributos personalizados
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomCardView)
        try {
            val backgroundColor = attributes.getColor(
                R.styleable.CustomCardView_cardBackgroundColor,
                context.getColor(R.color.primary)
            )
            val labelText = attributes.getString(R.styleable.CustomCardView_cardLabelText) ?: ""
            val valueText = attributes.getString(R.styleable.CustomCardView_cardValueText) ?: ""



            // Aplica los atributos
            setCardBackgroundColor(backgroundColor)
            labelTextView.text = labelText
            valueTextView.text = valueText
        } finally {
            attributes.recycle()
        }
    }
}