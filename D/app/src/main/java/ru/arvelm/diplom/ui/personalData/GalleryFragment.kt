package ru.arvelm.diplom.ui.personalData

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.fragment_gallery.view.*
import ru.arvelm.diplom.R
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class GalleryFragment : Fragment() {

    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA=="
    private val sk = "sicretKeyForId1029384756"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        val root = inflater.inflate(R.layout.fragment_gallery, container, false)

        root.block_code.visibility = View.INVISIBLE

        FirebaseDatabase
            .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("personalData")
            .child("email")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    root.mail.text = dataSnapshot.value.toString()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        FirebaseDatabase
            .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("personalData")
            .child("phone")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    root.phone.text = dataSnapshot.value.toString()
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        root.agree_doc.setOnClickListener {
            val uri = "https://yadi.sk/i/qezv0wNPpus5uQ"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(uri)
            startActivity(intent)
        }


        FirebaseDatabase
            .getInstance()
            .reference
            .child("clientId")
            .child(decrypt(id, sk))
            .child("agree")
            .child("check")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                        root.agree.setOnClickListener {
                            if (dataSnapshot.value.toString() == "0") {
                            root.block_code.visibility = View.VISIBLE

                            val random = Random.nextInt(100000, 999999)

                            val database = FirebaseDatabase.getInstance()
                            val myRef: DatabaseReference = database
                                .reference
                                .child("clientId")
                                .child(decrypt(id, sk))
                                .child("agree")
                                .child("code")

                            myRef.setValue(random.toString())

                                 FirebaseDatabase
                                        .getInstance()
                                        .reference
                                        .child("clientId")
                                        .child(decrypt(id, sk))
                                        .child("personalData")
                                        .child("phone")
                                        .addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                // отправка смс

                                                /*Twilio.init("ACd5f886f0b6e52b11ed2b9ba0a9134bce", "bfbd908a81a2d3d718c329d2a8860294")
                                                     val message = Message.creator(
                                                     PhoneNumber(dataSnapshot.value.toString()),
                                                     PhoneNumber(dataSnapshot.value.toString()),
                                                     random.toString
                                                      ).create()*/

                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })

                            FirebaseDatabase
                                .getInstance()
                                .reference
                                .child("clientId")
                                .child(decrypt(id, sk))
                                .child("agree")
                                .child("code")
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        root.button_change.setOnClickListener {
                                            val code = root.edit_code.text.toString()
                                            if (code == dataSnapshot.value.toString()){
                                                val database = FirebaseDatabase.getInstance()
                                                val myRef: DatabaseReference = database
                                                    .reference
                                                    .child("clientId")
                                                    .child(decrypt(id, sk))
                                                    .child("agree")
                                                    .child("check")

                                                myRef.setValue("1")
                                                Toast.makeText(context, "Соглашение успешно принято", Toast.LENGTH_SHORT).show()
                                                root.block_code.visibility = View.INVISIBLE
                                            } else {
                                                Toast.makeText(context, "Неверный код", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                        } else {
                                Toast.makeText(context, "Соглашение уже принято", Toast.LENGTH_SHORT).show()
                            }

                }
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