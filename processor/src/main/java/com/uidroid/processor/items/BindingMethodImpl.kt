package com.uidroid.processor.items

class BindingMethodImpl {

    var enclosingClass: String
    var methodName: String
    var viewClass: String
    var dataClass: String
    var dependencies: MutableList<String> = ArrayList()

    constructor(enclosingClass: String, methodName: String, viewClass: String, dataClass: String) {
        this.enclosingClass = enclosingClass
        this.methodName = methodName
        this.viewClass = viewClass
        this.dataClass = dataClass
    }

    constructor(
        enclosingClass: String,
        methodName: String,
        viewClass: String,
        dataClass: String,
        dependencies: MutableList<String>
    ) {
        this.enclosingClass = enclosingClass
        this.methodName = methodName
        this.viewClass = viewClass
        this.dataClass = dataClass
        this.dependencies = dependencies
    }
}