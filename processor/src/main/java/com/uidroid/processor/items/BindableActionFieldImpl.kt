package com.uidroid.processor.items

class BindableActionFieldImpl(// field name
    var fieldName: String, // path without class names (the parent class name is held in the map of bindableViewFields)
    var objectPath: String, // full class name of the view held in the parent view
    var fieldViewClassName: String
) {
    var fieldObjectClassName // simple class name of the object held in the parent data
            : String? = null
}