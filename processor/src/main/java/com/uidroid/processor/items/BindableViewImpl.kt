package com.uidroid.processor.items

import java.util.*

/**
 * Class which represents a @BindableView annotated android view.
 */
class BindableViewImpl {
    var className: String

    // Action for each object which can bind this view for this view
    var actions: MutableMap<String, BindableActionImpl>

    // maps the simple class name of object which is binding  this view to the fields which path derives from this object
    var bindableViewFields: MutableMap<String, MutableList<BindableViewFieldImpl>>
    var bindableActionFields: MutableMap<String, MutableList<BindableActionFieldImpl>>
    var implementIView = false

    constructor(className: String) {
        this.className = className
        bindableViewFields = LinkedHashMap()
        actions = LinkedHashMap()
        bindableActionFields = LinkedHashMap()
    }

    constructor(className: String, implementIView: Boolean) {
        this.className = className
        bindableViewFields = LinkedHashMap()
        actions = LinkedHashMap()
        this.implementIView = implementIView
        bindableActionFields = LinkedHashMap()
    }
}