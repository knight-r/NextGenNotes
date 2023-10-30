package com.example.notesapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _notesList = MutableLiveData<MutableList<Note>>()
    val notesList: LiveData<MutableList<Note>> = _notesList

    init {
        _notesList.value = mutableListOf()
    }

    fun addNote(note: Note) {
        _notesList.value?.apply {
            add(note)
            _notesList.postValue(this)
        }
    }

    fun resetViewModel() {
        _notesList.postValue(mutableListOf())
    }
}