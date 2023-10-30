package com.example.notesapp

data class Note(
    val date: String,
    val title: String,
    val content:String,
    val time: String
) {
    constructor() : this("", "","","")
}
