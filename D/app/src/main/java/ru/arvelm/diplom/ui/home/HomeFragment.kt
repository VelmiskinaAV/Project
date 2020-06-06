package ru.arvelm.diplom.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.card_name
import ru.arvelm.diplom.AccountActivity
import ru.arvelm.diplom.R
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class HomeFragment : Fragment() {

    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA=="
    private val sk = "sicretKeyForId1029384756"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val path = context?.getExternalFilesDir(null)
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()

        var id = ""
        val file = File(letDirectory, "id.txt")

        try {
            id = FileInputStream(file).bufferedReader().use{
                it.readText()
            }
        } catch(e: IOException){ }

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        FirebaseDatabase
                .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("creditCards")
            .child("card")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    root.card_num.text = "****" + dataSnapshot.value.toString().substring(5, 9)
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        FirebaseDatabase
            .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("creditCards")
            .child("money")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    root.card_money.text = dataSnapshot.value.toString() + " " + "P"
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        FirebaseDatabase
            .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("creditCards")
            .child("type")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    root.card_name.text = dataSnapshot.value.toString()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        return root
    }

    fun decrypt(strToDecrypt : String?, secretKey: String) : String {
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
}
