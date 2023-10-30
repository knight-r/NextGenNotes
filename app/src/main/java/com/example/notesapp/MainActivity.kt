package com.example.notesapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), View.OnClickListener, NoteListAdapter.onItemClickListener {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter :NoteListAdapter
    private lateinit var mAuth: FirebaseAuth
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mAuth = FirebaseAuth.getInstance()
        noteViewModel = ViewModelProvider(this, NoteViewModelFactory(NoteRepository()))[NoteViewModel::class.java]
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")
        progressDialog!!.setCancelable(false)
        initOnClickListener()
        updateDataToUI()

        updateUIFromFirebaseDatabase()

    }
    private fun updateUIFromFirebaseDatabase() {
        progressDialog!!.show()
        val reference = FirebaseDatabase.getInstance().reference
             .child(mAuth.uid.toString()).child("text_notes")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for(child in snapshot.children) {
                        val note = child.getValue(Note::class.java)
                        noteViewModel.addNote(note!!)
                    }
                }
                progressDialog!!.cancel()
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog!!.cancel()
                Log.e("MainActivity","Error in updating UI")
            }

        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDataToUI() {
        noteViewModel.notesList.observe(this) {
            mainBinding.rvNoteList.layoutManager = LinearLayoutManager(this)
            noteAdapter = NoteListAdapter()
            noteAdapter.setOnItemClickListener(this)
            mainBinding.rvNoteList.adapter = noteAdapter
            noteAdapter.setList(it)
            noteAdapter.notifyDataSetChanged()
        }
    }

    private fun initOnClickListener() {
        mainBinding.apply {
            btnSaveNote.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            mainBinding.btnSaveNote.id -> {
                val noteContent = mainBinding.etNoteContent.text.toString()
                val noteTitle = mainBinding.etNoteTitle.text.toString()
                addNoteToDatabaseForCurrentUser(noteTitle,noteContent)
            }
        }

    }

    private fun addNoteToDatabaseForCurrentUser(noteTitle: String, noteContent: String) {
        val userId = mAuth.uid
        if (isValidNote(noteContent,noteTitle)) {
            val currentDate:String = LocalDate.now().toString()
            val currentTime = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            var formattedTime = currentTime.format(formatter)

            formattedTime += if(formattedTime.substring(0,2).toInt() >= 12) " PM"  else " AM"
            val newNote = Note(currentDate,noteTitle,noteContent,formattedTime)
            val primaryKey = System.currentTimeMillis().toString()
            val databaseReference: DatabaseReference  = FirebaseDatabase.getInstance().reference
            databaseReference.child(userId.toString()).child("text_notes").push().setValue(newNote)
            noteViewModel.addNote(newNote)
        } else {
            Toast.makeText(this, "Enter valid input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidNote(noteContent: String, noteTitle: String): Boolean {
        return noteContent.isNotBlank() && noteTitle.isNotBlank()
    }


    override fun onNewIntent(intent: Intent?) {
       noteViewModel.resetViewModel()
        super.onNewIntent(intent)
    }

    override fun onItemCLicked(position: Int) {
        Toast.makeText(this, "ayaa", Toast.LENGTH_SHORT).show()
        mainBinding.etNoteTitle.setText(noteViewModel.notesList.value!![position].title)
        mainBinding.etNoteContent.setText(noteViewModel.notesList.value!![position].content)

    }
}