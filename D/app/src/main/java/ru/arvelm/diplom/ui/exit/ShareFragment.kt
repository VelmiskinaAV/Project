package ru.arvelm.diplom.ui.exit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_share.view.*
import ru.arvelm.diplom.MainActivity
import ru.arvelm.diplom.PinActivity
import ru.arvelm.diplom.R
import java.io.File

class ShareFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_share, container, false)

        root.button_exit.setOnClickListener{
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            auth.signOut()

            val path = context?.getExternalFilesDir(null)
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()
            val file = File(letDirectory, "id.txt")
            file.delete()

            val intent = Intent(context, PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        root.button_change.setOnClickListener {

            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            auth.signOut()

            val path = context?.getExternalFilesDir(null)
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()
            val file = File(letDirectory, "Data.txt")
            file.delete()

            val fileId = File(letDirectory, "id.txt")
            fileId.delete()

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        return root
    }


}