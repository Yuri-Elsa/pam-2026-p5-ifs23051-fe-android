package org.delcom.pam_p5_ifs23051.helper

class ConstHelper {
    // Route Names
    enum class RouteNames(val path: String) {
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        Home(path = "home"),

        Profile(path = "profile"),
        Todos(path = "todos"),
        TodosAdd(path = "todos/add"),
        TodosDetail(path = "todos/{todoId}"),
        TodosEdit(path = "todos/{todoId}/edit"),
    }
}