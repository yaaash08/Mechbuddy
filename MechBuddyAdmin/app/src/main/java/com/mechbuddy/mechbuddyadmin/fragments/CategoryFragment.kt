package com.mechbuddy.mechbuddyadmin.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mechbuddy.mechbuddyadmin.R
import com.mechbuddy.mechbuddyadmin.databinding.FragmentCategoryBinding
import java.util.*

class CategoryFragment : Fragment() {

    private lateinit var binding : FragmentCategoryBinding
    private var imageUrl : Uri? = null

    private var launchGalleryActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl = it.data!!.data
            binding.imageView1.setImageURI(imageUrl)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        binding.imageView1.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type="image/*"
            launchGalleryActivity.launch(intent)
        }
        binding.button1.setOnClickListener {
            validateData(binding.categoryName.text.toString())
        }

        return binding.root
    }

    private fun validateData(categoryName:String) {
        if(categoryName.isEmpty()){
            Toast.makeText(requireContext(), "Category Name cannot be empty !", Toast.LENGTH_SHORT).show()
        }
        else if (imageUrl==null){
            Toast.makeText(requireContext(), "Please select image !", Toast.LENGTH_SHORT).show()
        }
        else
            uploadImage(categoryName)
    }

    private fun uploadImage(categoryName: String) {
        val filename = UUID.randomUUID().toString()+".jpg"
        val imageRef = FirebaseStorage.getInstance().reference.child("category/$filename")
        imageRef.putFile(imageUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl
                    .addOnSuccessListener{
                        downloadImageUrl ->
                        storeData(categoryName,downloadImageUrl.toString())
                    }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(categoryName: String, url: String) {
        val db = Firebase.firestore
        val data = hashMapOf<String,Any>(
            "cat" to categoryName,
            "img" to url
        )
        db.collection("Categories").add(data)
            .addOnSuccessListener {
                binding.imageView1.setImageDrawable(ResourcesCompat.getDrawable(requireContext().resources,R.drawable.upload,requireContext().theme))
                binding.categoryName.text=null
                Toast.makeText(requireContext(), "Category Added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Something went wrong with Storage !", Toast.LENGTH_SHORT).show()
            }
    }
}