package ru.arvelm.diplom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.util.Base64
import android.util.Base64.encodeToString

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_pin.*
import kotlinx.android.synthetic.main.activity_pin.buttonVerifyPhone
import kotlinx.android.synthetic.main.activity_pin.fieldVerificationCode
import ru.arvelm.diplom.ui.home.HomeFragment

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec

import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

class PinActivity : AppCompatActivity(), View.OnClickListener {

    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA=="
    private val sk = "sicretKeyForId1029384756"

    private lateinit var auth: FirebaseAuth

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    var flag: Int = 0
    var password = ""
    var newPassword = ""
    var data: String = ""
    var signInId = ""
    var firstEnter = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        data = intent.getStringExtra("DATA")

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        layout_sms.visibility = View.GONE
        auth = FirebaseAuth.getInstance()

        /*  if (count == 0){
              textViewTitle.text = "Введите PIN-код"
          } else {
              textViewTitle.text = "Повторите PIN-код"
          }*/

        textViewChange.setOnClickListener(this)
        buttonOne.setOnClickListener(this)
        buttonOne.setOnClickListener(this)
        buttonTwo.setOnClickListener(this)
        buttonThree.setOnClickListener(this)
        buttonFour.setOnClickListener(this)
        buttonFive.setOnClickListener(this)
        buttonSix.setOnClickListener(this)
        buttonSeven.setOnClickListener(this)
        buttonEight.setOnClickListener(this)
        buttonNine.setOnClickListener(this)
        buttonZero.setOnClickListener(this)
        buttonDel.setOnClickListener(this)
        buttonCall.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verificationInProgress = false

                layout_sms.visibility = View.VISIBLE
                updateUI(STATE_VERIFY_SUCCESS, auth.currentUser,  credential)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                verificationInProgress = false

                layout_sms.visibility = View.VISIBLE

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(baseContext, "Неверный номер телефона", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(baseContext, "Quota exceeded", Toast.LENGTH_SHORT).show()
                }

                layout_sms.visibility = View.VISIBLE
                fieldVerificationCode.isEnabled = true
                buttonVerifyPhone.isEnabled = true
                buttonResend.isEnabled = true
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken){
                storedVerificationId = verificationId
                resendToken = token
                layout_sms.visibility = View.VISIBLE
            }
        }
    }

    private fun signIn(authEmail: String, authPassword: String) {

        auth.signInWithEmailAndPassword(authEmail, authPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    signInId += auth.currentUser?.uid
                    layout_pin.visibility = View.GONE
                    layout_sms.visibility = View.VISIBLE
                    getPhoneNumberFromDatabase()

                } else {
                    Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun encrypt(strToEncrypt: String?, secretKey: String) :  String
    {
        try
        {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return encodeToString(cipher.doFinal(strToEncrypt?.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            println("Error while encrypting: $e")
        }
        return ""
    }

    private fun decrypt(strToDecrypt : String?, secretKey: String) : String {
        try
        {
            val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey =  SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            return  String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
        }
        catch (e : Exception) {
            println("Error while decrypting: $e")
        }
        return ""
    }

    private fun delOnClick(): Int {
        if(flag == 1){

            numOne.setImageResource(R.drawable.ic_ring)
            password = password.substring(0, password.length - 1)
            return flag--
        }
        if(flag == 2){
            numTwo.setImageResource(R.drawable.ic_ring)
            password = password.substring(0, password.length - 1)
            return flag--
        } else{

            numThree.setImageResource(R.drawable.ic_ring)
            password = password.substring(0, password.length - 1)

            return flag--
        }
    }


    private fun getPhoneNumberFromDatabase() {
        val database = FirebaseDatabase.getInstance()

        database.reference
            .child("phoneAuth")
            .child("phoneId")
            .child(auth.currentUser?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    startPhoneNumberVerification(dataSnapshot.value.toString())
                    auth.signOut()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun onNumClick(num: Int): Int{
        if(flag == 0){
            numOne.setImageResource(R.drawable.ic_round)
            password += num.toString()
            return flag++
        }
        if(flag == 1){
            numTwo.setImageResource(R.drawable.ic_round)
            password += num.toString()
            return flag++
        }
        if(flag == 2){
            numThree.setImageResource(R.drawable.ic_round)
            password += num.toString()
            return flag++
        } else {
            numFour.setImageResource(R.drawable.ic_round)
            password += num.toString()

            if (data == "") {

                val path = baseContext.getExternalFilesDir(null)
                val letDirectory = File(path, "LET")
                letDirectory.mkdirs()

                var authData: String? = null
                val file = File(letDirectory, "Data.txt")

                try {
                    authData = FileInputStream(file).bufferedReader().use {
                        it.readText()
                    }
                } catch (e: IOException) {}

                val decryptData = decrypt(authData, password)

                if (decryptData != "") {
                    signIn(decryptData.split(" ")[0].trim(), decryptData.split(" ")[1].trim())
                } else {
                    Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                }
            } else {

                if (firstEnter){
                    newPassword = password
                    firstEnter = false
                    password = ""
                    flag = 0

                    //textview

                    numOne.setImageResource(R.drawable.ic_ring)
                    numTwo.setImageResource(R.drawable.ic_ring)
                    numThree.setImageResource(R.drawable.ic_ring)
                    numFour.setImageResource(R.drawable.ic_ring)
                } else{
                    if (password == newPassword){

                        val path = baseContext.getExternalFilesDir(null)
                        val letDirectory = File(path, "LET")
                        letDirectory.mkdirs()

                        val file = File(letDirectory, "Data.txt")
                        file.appendText(encrypt(data, password))

                        signIn(data.split(" ")[0], data.split(" ")[1])

                    } else{
                        newPassword = ""
                        password = ""
                        flag = 0
                        firstEnter = true

                        val vibratorService = getSystemService(VIBRATOR_SERVICE) as Vibrator
                        //vibratorService.vibrate(500)

                        numOne.setImageResource(R.drawable.ic_ring)
                        numTwo.setImageResource(R.drawable.ic_ring)
                        numThree.setImageResource(R.drawable.ic_ring)
                        numFour.setImageResource(R.drawable.ic_ring)
                    }
                }
            }
        }
        return flag
    }

    private fun call(){
        Toast.makeText(this, "Звонок в службу тех. поддержки", Toast.LENGTH_SHORT).show()
    }

    private fun textViewChangeUser(){
        textViewChange.setTextColor(Color.parseColor("#e5f5ff"))

        val builder = AlertDialog.Builder(this@PinActivity)
        builder.setMessage("Вы уверены, что хотите сменить пользователя?")
        builder.setPositiveButton("Да"){dialog, which ->
            val path = baseContext.getExternalFilesDir(null)
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()
            val file = File(letDirectory, "Data.txt")
            file.delete()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("Нет"){ _, _ ->
        }

        val dialog: AlertDialog =  builder.create()
        dialog.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            callbacks)
        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            this,
            callbacks,
            token)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    signInId += auth.currentUser?.uid.toString()

                    val intent = Intent(this, AccountActivity::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    val path = baseContext.getExternalFilesDir(null)
                    val letDirectory = File(path, "LET")
                    letDirectory.mkdirs()
                    val file = File(letDirectory, "id.txt")
                    file.appendText(encrypt(signInId, sk))
                    startActivity(intent)
                    finish()
                }else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        fieldVerificationCode.error = "Неверный код"
                    }

                    fieldVerificationCode.isEnabled = true
                    buttonVerifyPhone.isEnabled = true
                    buttonResend.isEnabled = true
                }
            }
    }

    private fun updateUI(uiState: Int, user: FirebaseUser? = auth.currentUser, cred: PhoneAuthCredential? = null){// SMSCode
        when (uiState) {

            STATE_VERIFY_SUCCESS -> {
                if (cred != null) {
                    if (cred.smsCode != null) {
                        fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        fieldVerificationCode.setText("error")
                    }
                }
            }
        }
    }

    override fun onBackPressed(){

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            exitProcess(0)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.textViewChange -> textViewChangeUser()
            R.id.buttonOne -> onNumClick(1)
            R.id.buttonTwo -> onNumClick(2)
            R.id.buttonThree -> onNumClick(3)
            R.id.buttonFour -> onNumClick(4)
            R.id.buttonFive -> onNumClick(5)
            R.id.buttonSix -> onNumClick(6)
            R.id.buttonSeven -> onNumClick(7)
            R.id.buttonEight -> onNumClick(8)
            R.id.buttonNine -> onNumClick(9)
            R.id.buttonZero -> onNumClick(0)
            R.id.buttonCall -> call()
            R.id.buttonDel -> delOnClick()
            R.id.buttonVerifyPhone ->{
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Поле не может быть пустым"
                    return
                }
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
            R.id.buttonResend -> resendVerificationCode("+79179979433", resendToken)
        }
    }
    companion object {
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_VERIFY_SUCCESS = 4
    }
}
