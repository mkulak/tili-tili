package com.github.mkulak

import react.dom.render
import kotlin.browser.document
import kotlin.browser.window

fun main(args: Array<String>) {
//    require("narrow-jumbotron.css")
    module.hot?.let { hot ->
        hot.accept() // accept hot reload
    }
    window.onload = {
        render(document.getElementById("content")) {
            keynotedexApp {}
        }
    }
}
