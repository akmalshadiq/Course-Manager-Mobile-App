package com.example.coursemanager

data class Course(
    var courseName: String = "",
    var courseCode: String = "",
    var instructor: String = "",
    var schedule: String = "",
    var id: String = "" // Change from `val` to `var`
)
