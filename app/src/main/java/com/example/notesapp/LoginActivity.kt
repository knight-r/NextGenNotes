package com.example.notesapp

import android.R.attr.name
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call.Details
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private var userName:String = ""
    private var userEmail:String = ""
    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    var progressDialog: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        mAuth = FirebaseAuth.getInstance()
        if(mAuth!!.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        mFirebaseDatabase = FirebaseDatabase.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Please wait...")
        progressDialog!!.setCancelable(false)

        initGoogleSignIn()
        loginBinding.cvSignIn.setOnClickListener(this@LoginActivity)

    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            loginBinding.cvSignIn.id -> {
                performGoogleSignIn()
            }
        }
    }

    private fun performGoogleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                userName = account.displayName.toString()
                userEmail = account.email.toString()

                account.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("MainActivity", "Google sign in failed", e)
                Toast.makeText(this, "error 1 $e", Toast.LENGTH_SHORT).show()
                progressDialog!!.dismiss()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    val reference31 =
                        FirebaseDatabase.getInstance().reference.child("Registered Users")
                    reference31.child(mAuth!!.uid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange( dataSnapshot: DataSnapshot) {
                                // user exists in the database
//                                if (dataSnapshot.exists()) {}
                                    progressDialog!!.dismiss()
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finishAffinity()
                                    Toast.makeText(applicationContext,
                                        "You have successfully signed in.", Toast.LENGTH_SHORT).show()

                            }

                            override fun onCancelled( databaseError: DatabaseError) {
                                progressDialog!!.dismiss()
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error: $databaseError",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                } else {
                    // If sign in fails, display a message to the user.
                    progressDialog!!.dismiss()
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                }
            }

    }

    override fun onResume() {
        if(mAuth!!.currentUser != null) {
            finish()
        }
        super.onResume()
    }
    companion object {
        const val REQUEST_CODE_SIGN_IN = 101
    }
}
