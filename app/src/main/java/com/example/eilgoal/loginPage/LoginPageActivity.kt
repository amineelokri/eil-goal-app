package com.example.eilgoal.loginPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eilco.RegisterActivity
import com.example.eilgoal.HomePageActivity
import com.example.eilgoal.MatchDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.eilgoal.R
import com.google.firebase.FirebaseApp

class LoginPageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        // Ensure FirebaseApp is initialized
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Login with email and password
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            navigateToHomePage() // Navigate to the next activity
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener { signInWithGoogle() }

        // Navigate to Register
        val registerText = findViewById<TextView>(R.id.registerText)
        registerText.setOnClickListener {
            navigateToRegisterPage() // Start RegisterActivity
        }
    }

    private fun signInWithGoogle() {
        // Google Sign-In implementation here
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToHomePage() {
        // Navigate to home page activity
        val intent = Intent(this, MatchDetailsActivity::class.java)
        startActivity(intent)
        finish() // Optionally finish the current activity
    }

    private fun navigateToRegisterPage() {
        // Navigate to register page activity
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
//        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
