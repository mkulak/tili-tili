package com.github.mkulak

import kotlinx.html.main
import react.*
import react.dom.*

class Application : RComponent<RProps, ApplicationPageState>() {

    override fun ApplicationPageState.init() {
        currentUser = null
//        checkUserSession()
    }

    override fun RBuilder.render() {
        div {
            main("mt-0 mt-md-2 mt-lg-0") {
                attrs { role = "main" }
                span {
                    +"THIS IS CONTENT 55"
                }
//                switch {
//                    route("/", HomeScreen::class, exact = true)
//                    route("/login", exact = true) {
//                        login {
//                            attrs {
//                                currentUser = state.currentUser
//                                onUserLoggedIn = { user -> userLoggedIn(user) }
//                            }
//                        }
//                    }
//                    route("/conferences", ComingSoonScreen::class)
//                    route("/register", RegisterScreen::class)
//                    route("/speakers", ComingSoonScreen::class)
//                    route("/:userId/:submissionId", SubmissionScreen::class, exact = true)
//                    route("/:userId", UserScreen::class, exact = true)
//                    route(NotFoundScreen::class)
//                }
                footer("container") {
                    p { +"© url shortener ${js("new Date().getFullYear()")}" }
                }
            }
        }
    }

    private fun userLoggedIn(user: User) {
        setState {
            currentUser = user
        }
    }

    private fun checkUserSession() {
//        promise {
//            val user = checkSession()
//            setState {
//                currentUser = user
//            }
//        }.catch {
//            console.error(it)
//            setState {
//                currentUser = null
//            }
//        }
    }
}

class ApplicationPageState(var currentUser: User?) : RState

fun RBuilder.keynotedexApp(handler: RHandler<RProps>) = child(Application::class, handler)

class User