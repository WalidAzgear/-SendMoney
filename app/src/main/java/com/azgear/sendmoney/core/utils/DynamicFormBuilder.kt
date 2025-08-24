package com.azgear.sendmoney.core.utils

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.azgear.sendmoney.R
import com.azgear.sendmoney.modules.sendmoney.data.FormField

class DynamicFormBuilder(
    private val context: Context,
    private val onFieldValueChanged: (fieldName: String, value: String) -> Unit,
    private val onValidationError: (fieldName: String, error: String?) -> Unit
) {
    
    fun buildFormFields(
        fields: List<FormField>,
        container: ViewGroup,
        initialData: Map<String, String> = emptyMap()
    ): Map<String, View> {
        container.removeAllViews()
        val fieldViews = mutableMapOf<String, View>()
        
        fields.forEach { field ->
            val fieldView = createFieldView(field, initialData[field.name] ?: "")
            fieldViews[field.name] = fieldView
            container.addView(fieldView)
            
            // Add margin between fields
            (fieldView.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = context.resources.getDimensionPixelSize(R.dimen.field_margin_top)
            }
        }
        
        return fieldViews
    }
    
    private fun createFieldView(field: FormField, initialValue: String): View {
        return when (field.type) {
            "text", "msisdn" -> createTextInputField(field, initialValue)
            "number" -> createNumberInputField(field, initialValue)
            "option" -> createOptionField(field, initialValue)
            "date" -> createDateInputField(field, initialValue)
            else -> createTextInputField(field, initialValue) // Default to text
        }
    }
    
    private fun createTextInputField(field: FormField, initialValue: String): View {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48 // 20dp in pixels approximately
            }
            orientation = LinearLayout.VERTICAL
        }
        
        // Add label
        val labelText = TextView(context).apply {
            text = LanguageManager.getLocalizedText(field.label)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24 // 8dp
            }
        }
        container.addView(labelText)
        
        // Create frame layout for input with background
        val inputFrame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                168 // 56dp in pixels
            )
            background = ContextCompat.getDrawable(context, R.drawable.input_background)
        }
        
        val editText = EditText(context).apply {
            setText(initialValue)
            background = null
            tag = field.name // Store field name for validation
            
            // Set placeholder if available
            val placeholder = LanguageManager.getLocalizedText(field.placeholder)
            if (placeholder.isNotEmpty()) {
                this.hint = placeholder
            }
            
            // Set input type based on field type
            when (field.type) {
                "msisdn" -> inputType = InputType.TYPE_CLASS_PHONE
                else -> inputType = InputType.TYPE_CLASS_TEXT
            }
            
            // Set max length
            val maxLength = field.getMaxLengthInt()
            maxLength?.let { length ->
                if (length > 0) {
                    filters = arrayOf(android.text.InputFilter.LengthFilter(length))
                }
            }
            
            // Styling
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setPadding(48, 0, 48, 0) // 16dp horizontal padding
            
            // Add text change listener
            addTextChangedListener { text ->
                val value = text.toString()
                onFieldValueChanged(field.name, value)
                
                // Validate on text change
                val validationResult = FormValidationEngine.validateField(field, value)
                if (!validationResult.isValid) {
                    onValidationError(field.name, validationResult.errorMessage)
                } else {
                    onValidationError(field.name, null)
                }
            }
        }
        
        inputFrame.addView(editText)
        container.addView(inputFrame)
        
        // Add error message TextView
        val errorText = TextView(context).apply {
            text = ""
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                leftMargin = 16
            }
            visibility = View.GONE
            tag = "${field.name}_error" // Tag for finding error text
        }
        container.addView(errorText)
        
        return container
    }
    
    private fun createNumberInputField(field: FormField, initialValue: String): View {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48 // 20dp in pixels approximately
            }
            orientation = LinearLayout.VERTICAL
        }
        
        // Add label
        val labelText = TextView(context).apply {
            text = LanguageManager.getLocalizedText(field.label)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24 // 8dp
            }
        }
        container.addView(labelText)
        
        // Create frame layout for input with background
        val inputFrame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                168 // 56dp in pixels
            )
            background = ContextCompat.getDrawable(context, R.drawable.input_background)
        }
        
        val editText = EditText(context).apply {
            setText(initialValue)
            background = null
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            
            val placeholder = LanguageManager.getLocalizedText(field.placeholder)
            if (placeholder.isNotEmpty()) {
                this.hint = placeholder
            }
            
            // Styling
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setPadding(48, 0, 48, 0) // 16dp horizontal padding
            
            addTextChangedListener { text ->
                val value = text.toString()
                onFieldValueChanged(field.name, value)
                
                val validationResult = FormValidationEngine.validateField(field, value)
                if (!validationResult.isValid) {
                    onValidationError(field.name, validationResult.errorMessage)
                } else {
                    onValidationError(field.name, null)
                }
            }
        }
        
        inputFrame.addView(editText)
        container.addView(inputFrame)
        
        // Add error message TextView
        val errorText = TextView(context).apply {
            text = ""
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                leftMargin = 16
            }
            visibility = View.GONE
            tag = "${field.name}_error" // Tag for finding error text
        }
        container.addView(errorText)
        
        return container
    }
    
    private fun createOptionField(field: FormField, initialValue: String): View {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48 // 20dp in pixels approximately
            }
            orientation = LinearLayout.VERTICAL
        }
        
        // Add label
        val labelText = TextView(context).apply {
            text = LanguageManager.getLocalizedText(field.label)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24 // 8dp
            }
        }
        container.addView(labelText)
        
        // Create frame layout for spinner with background
        val spinnerFrame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                168 // 56dp in pixels
            )
            background = ContextCompat.getDrawable(context, R.drawable.input_background)
        }
        
        // Create spinner for options
        val spinner = Spinner(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = null
            setPadding(48, 0, 48, 0) // 16dp horizontal padding
        }
        
        // Prepare options
        val options = mutableListOf<String>()
        val optionValues = mutableListOf<String>()
        
        // Add empty option
        options.add("Select ${LanguageManager.getLocalizedText(field.label)}")
        optionValues.add("")
        
        // Add actual options
        field.options?.forEach { option ->
            options.add(option.label)
            optionValues.add(option.name)
        }
        
        // Set adapter
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Set initial selection
        val initialIndex = optionValues.indexOf(initialValue)
        if (initialIndex >= 0) {
            spinner.setSelection(initialIndex)
        }
        
        // Set listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = if (position > 0) optionValues[position] else ""
                onFieldValueChanged(field.name, selectedValue)
                
                val validationResult = FormValidationEngine.validateField(field, selectedValue)
                if (!validationResult.isValid) {
                    onValidationError(field.name, validationResult.errorMessage)
                } else {
                    onValidationError(field.name, null)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                onFieldValueChanged(field.name, "")
            }
        }
        
        spinnerFrame.addView(spinner)
        container.addView(spinnerFrame)
        
        // Add error message TextView
        val errorText = TextView(context).apply {
            text = ""
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                leftMargin = 16
            }
            visibility = View.GONE
            tag = "${field.name}_error" // Tag for finding error text
        }
        container.addView(errorText)
        
        return container
    }
    
    private fun createDateInputField(field: FormField, initialValue: String): View {
        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 48 // 20dp in pixels approximately
            }
            orientation = LinearLayout.VERTICAL
        }
        
        // Add label
        val labelText = TextView(context).apply {
            text = LanguageManager.getLocalizedText(field.label)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24 // 8dp
            }
        }
        container.addView(labelText)
        
        // Create frame layout for input with background
        val inputFrame = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                168 // 56dp in pixels
            )
            background = ContextCompat.getDrawable(context, R.drawable.input_background)
        }
        
        val editText = EditText(context).apply {
            setText(initialValue)
            background = null
            inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
            
            val placeholder = LanguageManager.getLocalizedText(field.placeholder)
            if (placeholder.isNotEmpty()) {
                this.hint = placeholder
            }
            
            // Styling
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setPadding(48, 0, 48, 0) // 16dp horizontal padding
            
            addTextChangedListener { text ->
                val value = text.toString()
                onFieldValueChanged(field.name, value)
                
                val validationResult = FormValidationEngine.validateField(field, value)
                if (!validationResult.isValid) {
                    onValidationError(field.name, validationResult.errorMessage)
                } else {
                    onValidationError(field.name, null)
                }
            }
        }
        
        inputFrame.addView(editText)
        container.addView(inputFrame)
        
        // Add error message TextView
        val errorText = TextView(context).apply {
            text = ""
            textSize = 12f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                leftMargin = 16
            }
            visibility = View.GONE
            tag = "${field.name}_error" // Tag for finding error text
        }
        container.addView(errorText)
        
        return container
    }
} 