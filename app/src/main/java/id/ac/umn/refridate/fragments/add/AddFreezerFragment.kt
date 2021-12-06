package id.ac.umn.refridate.fragments.add

import android.app.*
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import id.ac.umn.refridate.R
import kotlinx.android.synthetic.main.fragment_freezer_add.*
import kotlinx.android.synthetic.main.fragment_freezer_add.view.*
import java.util.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import id.ac.umn.refridate.ReminderBroadcast
import id.ac.umn.refridate.model.ItemList
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

import com.google.firebase.storage.UploadTask

import com.google.firebase.storage.FirebaseStorage

import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

import android.util.Log
import android.widget.ImageView
import androidx.core.content.FileProvider

import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.tasks.*
import java.io.File


private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class AddFreezerFragment : Fragment() {
    private lateinit var storageRef: StorageReference
    private lateinit var imageRef: StorageReference
    private lateinit var ref: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_freezer_add, container, false)

        view.Tanggal.setOnClickListener{
            date()
        }

        view.img.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(requireContext(), "id.ac.umn.refridate.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if(takePictureIntent.resolveActivity(requireContext().packageManager) != null){
                startActivityForResult(takePictureIntent, REQUEST_CODE)
            } else {
                Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }

        view.confirm_add.setOnClickListener {
            insertDataToDatabase()
        }
        return view
    }

    private fun insertDataToDatabase() {

        val nama = Nama.text.toString()
        val tanggal = dateTv.text.toString()
        val image = imageView

        if(inputCheck(nama, tanggal, image))
        {
            val drawable = imageView.drawable as BitmapDrawable
            val bitmap = drawable.bitmap

            ref = FirebaseDatabase.getInstance().getReference("ItemList")
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val itemId = ref.push().key

            if(itemId != null) {
                uploadImage(bitmap, itemId);

                val item = ItemList(itemId, nama, "Freezer", tanggal)

                ref.child(userId).child(itemId).setValue(item)
                        .addOnCompleteListener {
                    Toast.makeText(requireContext(), "Berhasil ditambahkan!", Toast.LENGTH_LONG).show()
                }
            }

            createNotification(tanggal)

            findNavController().navigate(R.id.action_addFreezerFragment_to_listFreezerFragment)
        }
        else
        {
            Toast.makeText(requireContext(), "Mohon mengisi semua!", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImage(bitmap: Bitmap, itemId: String) {
        val storage = FirebaseStorage.getInstance()
        storageRef = storage.getReference();
        imageRef = storageRef.child("freezer/$itemId.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener { exception -> Log.i("Upload Task Fail:", exception.toString()) }.addOnSuccessListener {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.i("problem", task.getException().toString())
                }
                imageRef.downloadUrl
            }.addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                if (task.isSuccessful) {
                    val downloadUri: Uri? = task.result
                    val ref = FirebaseDatabase.getInstance().reference.child("ItemList").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    Log.i("seeThisUri", downloadUri.toString())
                    ref.child(itemId).child("image").setValue(downloadUri.toString())
                } else {
                    Log.i("wentWrong", "downloadUri failure")
                }
            })
        }
    }

    private fun createNotification(tanggal: String) {
        // Create the NotificationChannel
        val name = "Reminder"
        val descriptionText = "Hello! Dalam waktu 3 hari, beberapa barang di kulkas mu akan expired, segera cek mereka!"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification", name, importance)
        val notificationManager = requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        val intent = Intent(requireActivity(), ReminderBroadcast::class.java)
        intent.putExtra("title", name)
        intent.putExtra("description", descriptionText)
        val pendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, 0)
        val alarmManager = requireActivity().getSystemService(ALARM_SERVICE) as AlarmManager?
        val currentTime = System.currentTimeMillis()
        val date = SimpleDateFormat("dd/MM/yyyy").parse(tanggal)
        val expiredDate = date.time
        val minimumDay = TimeUnit.DAYS.toMillis(3)
        val expiredDateInMillis = expiredDate - currentTime - minimumDay // notification pada H-3 sebelum kedaluwarsa
        if(expiredDateInMillis < 0) alarmManager!![AlarmManager.RTC_WAKEUP, currentTime] = pendingIntent
        alarmManager!![AlarmManager.RTC_WAKEUP, currentTime + expiredDateInMillis] = pendingIntent
    }

    private fun date () {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog( requireContext(), DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
            val realMonth = mMonth + 1
            dateTv.text = ""+ mDay +"/"+ realMonth +"/"+ mYear
        }, year, month, day)
        dpd.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView.setImageBitmap(takenImage)
            imageView.tag = "updated"

        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    private fun inputCheck(nama: String, tanggal: String, imageView: ImageView): Boolean {
        return (!TextUtils.isEmpty(nama) && !TextUtils.isEmpty(tanggal) && tanggal.isNotEmpty() && !imageView.tag.equals("default"))
    }

}