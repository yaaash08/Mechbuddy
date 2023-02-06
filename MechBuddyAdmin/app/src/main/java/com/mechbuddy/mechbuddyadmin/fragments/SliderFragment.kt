package com.mechbuddy.mechbuddyadmin.fragments
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mechbuddy.mechbuddyadmin.databinding.FragmentSliderBinding
import java.util.*

class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding
    private var imageUrl : Uri? = null

    private var launchGalleryActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            imageUrl = it.data!!.data
            binding.imageView.setImageURI(imageUrl)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSliderBinding.inflate(layoutInflater)

        binding.apply {
            imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGalleryActivity.launch(intent)
            }

            uploadButton.setOnClickListener {
                if (imageUrl!=null){
                    uploadImage(imageUrl!!)
                }
                else{
                    Toast.makeText(requireContext(), "Please upload image !", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun uploadImage(uploadImageUrl: Uri ) {
        val filename = UUID.randomUUID().toString()+".jpg"
        val imageRef = FirebaseStorage.getInstance().reference.child("slider/$filename")
        imageRef.putFile(uploadImageUrl)
            .addOnSuccessListener {
                it.storage.downloadUrl
                .addOnSuccessListener{
                        downloadImageUrl->
                    storeData(downloadImageUrl.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(downloadImageUrl: String) {
        val db = Firebase.firestore
        val data = hashMapOf<String,Any>(
            "img" to downloadImageUrl
        )
        db.collection("slider").document("item").set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Slider Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(requireContext(), "Something went Wrong with Storage", Toast.LENGTH_SHORT).show()
            }
    }

}