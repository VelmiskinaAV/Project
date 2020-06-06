package ru.arvelm.diplom

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val path = baseContext.getExternalFilesDir(null)
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()

        var authData: String? = null
        val file = File(letDirectory, "Data.txt")

        try {
            authData = FileInputStream(file).bufferedReader().use{
                it.readText()
            }
        } catch(e: IOException){ }

        if (!authData.isNullOrEmpty()){
        //    file.delete()
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("DATA", "")
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        emailSignInButton.setOnClickListener(this)
    }

    private fun signIn(email: String, password: String) {

        if (!validateForm()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val data: String = editTextEmail.text.toString() + " " + editTextPassword.text.toString()
                    auth.signOut()
                    val intent = Intent(this, PinActivity::class.java)
                    intent.putExtra("DATA", data)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Неверный email или пароль",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = editTextEmail.text.toString()

        if (TextUtils.isEmpty(email)) {
            editTextEmail.error = "Данное поле необходимо заполнить"
            valid = false
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                editTextEmail.error = "Некорректно введенный email"
            } else{
                editTextEmail.error = null
            }
        }

        val password = editTextPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            editTextPassword.error = "Данное поле необходимо заполнить"
            valid = false
        } else {
            editTextPassword.error = null
        }

        return valid
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.emailSignInButton -> signIn(editTextEmail.text.toString(), editTextPassword.text.toString())
        }
    }

    override fun onStop() {
        super.onStop()
        auth.signOut()
    }

    override fun onBackPressed(){

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        exitProcess(0)
    }
}

