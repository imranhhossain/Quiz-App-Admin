package com.company.quizappadmin

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.company.quizappadmin.Models.CategoryModel
import com.company.quizappadmin.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private lateinit var database : FirebaseDatabase
    private lateinit var storege : FirebaseStorage
    private lateinit var dialog: Dialog

    private lateinit var CategoryImage : CircleImageView
    private lateinit var InputCategoryName : EditText
    private lateinit var AddCategorybtn : Button
    private lateinit var fetchImage : View

    private lateinit var imageUri : Uri

    private var i =0

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar!!.hide()

        database = FirebaseDatabase.getInstance()
        storege = FirebaseStorage.getInstance()

        dialog = Dialog(this)
        dialog.setContentView(R.layout.item_add_catagory_dialog)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)
        }
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.setMessage("Please wait")

        CategoryImage = dialog.findViewById(R.id.CategoryImage)
        InputCategoryName = dialog.findViewById(R.id.InputCategoryName)
        AddCategorybtn = dialog.findViewById(R.id.AddCategorybtn)
        fetchImage = dialog.findViewById(R.id.fetchImage)
        binding.addCatagory.setOnClickListener {
            dialog.show()
        }

        fetchImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
        AddCategorybtn.setOnClickListener{
            var name= InputCategoryName.text.toString()
            if (imageUri == null){
                Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
            }else if (name.isEmpty()){
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
            }
            else{
                progressDialog.show()
                uploadData()

            }
        }
    }

    private fun uploadData() {
        val storageRef = storege.reference.child("Category").child(System.currentTimeMillis().toString())
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                val categoryModel = CategoryModel(InputCategoryName.text.toString(), it.toString(), "", 0)
                database.reference.child("categories").child("category"+i++)
                    .setValue(categoryModel).addOnSuccessListener(object : OnSuccessListener<Void?> {
                        override fun onSuccess(p0: Void?) {
                            Toast.makeText(this@MainActivity, "Category Added", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: Exception) {
                            Toast.makeText(this@MainActivity, "Something went wrong" + p0.message.toString(), Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            if (data != null){
                if (data.data != null){
                    imageUri = data.data!!
                    CategoryImage.setImageURI(imageUri)
                }
            }
        }
    }
}