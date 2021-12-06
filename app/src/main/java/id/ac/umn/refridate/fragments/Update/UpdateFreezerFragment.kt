package id.ac.umn.refridate.fragments.Update

import android.app.*

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import id.ac.umn.refridate.R
import id.ac.umn.refridate.ReminderBroadcast
import id.ac.umn.refridate.model.ItemList
import kotlinx.android.synthetic.main.fragment_freezer_update.*

import kotlinx.android.synthetic.main.fragment_freezer_update.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


import com.google.android.gms.tasks.OnFailureListener

class UpdateFreezerFragment : Fragment() {

    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaseInstance: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference
    private val args by navArgs<UpdateFreezerFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_freezer_update, container, false)

        mFirebaseStorage = FirebaseStorage.getInstance()
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseInstance.getReference("ItemList")

        view.Nama_update.setText(args.currItem.name)
        view.dateTv_update.setText(args.currItem.exp_date_time)
        view.imageView.load(args.currItem.image)

        view.confirm_update.setOnClickListener{
            updateItem()
        }

        view.Tanggal_update.setOnClickListener{
            date()
        }

        setHasOptionsMenu(true)

        return view
    }

    private fun updateItem(){
        val name = Nama_update.text.toString()
        val date_time = dateTv_update.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val itemListQuery = mDatabaseReference.child(userId)

        if(inputCheck(name, date_time))
        {
            val updatedItem = ItemList(args.currItem.id, name, "Freezer", date_time, args.currItem.image)
            itemListQuery.child(args.currItem.id)
                    .setValue(updatedItem)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Data berhasil di update !", Toast.LENGTH_LONG).show()
                    }
                    .addOnCanceledListener {
                        Toast.makeText(context, "Gagal memperbarui data.", Toast.LENGTH_SHORT).show()
                    }

           createNotification(date_time)

            findNavController().navigate(R.id.action_updateFreezerFragment_to_listFreezerFragment)
        }
        else
        {
            Toast.makeText(requireContext(), "Mohon mengisi semua!", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(nama: String, tanggal: String): Boolean {
        return (!TextUtils.isEmpty(nama) && !TextUtils.isEmpty(tanggal) && tanggal.isNotEmpty())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.delete,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.delete_one)
        {
            deleteItem()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun date () {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog( requireContext(), DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
            val realMonth = mMonth + 1
            dateTv_update.setText(""+ mDay +"/"+ realMonth +"/"+ mYear)
        }, year, month, day)
        dpd.show()
    }

    private fun deleteItem(){
        val builder = AlertDialog.Builder(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val itemListQuery = mDatabaseReference.child(userId)
        val imageRef: StorageReference = mFirebaseStorage.getReferenceFromUrl(args.currItem.image)

        builder.setPositiveButton("Iya"){_, _ ->
            itemListQuery.child(args.currItem.id)
                    .removeValue().addOnSuccessListener(OnSuccessListener<Void?> {
                        Toast.makeText(requireContext(),"Berhasil di hapus: ${args.currItem.name}", Toast.LENGTH_SHORT).show()
                    })
            imageRef.delete().addOnSuccessListener(OnSuccessListener<Void?> {
                Log.d(TAG, "onSuccess: success delete image file")
            }).addOnFailureListener(OnFailureListener {
                Log.d(TAG, "onFailure:failed to delete image file")
            })

            findNavController().navigate(R.id.action_updateFreezerFragment_to_listFreezerFragment)
        }
        builder.setNegativeButton("Tidak"){_, _ -> }
        builder.setTitle("Hapus ${args.currItem.name}?")
        builder.setMessage("Apakah anda yakin menghapus ${args.currItem.name}")
        builder.create().show()
    }

    private fun createNotification(date_time: String) {
        val name = "Reminder"
        val descriptionText = "Hello! Dalam waktu 3 hari, beberapa barang di kulkas mu akan expired, segera cek mereka!"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification", name, importance)
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val intent = Intent(requireActivity(), ReminderBroadcast::class.java)
        intent.putExtra("title", name)
        intent.putExtra("description", descriptionText)
        val pendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, 0)
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val currentTime = System.currentTimeMillis()
        val date = SimpleDateFormat("dd/MM/yyyy").parse(date_time)
        val expiredDate = date.time
        val minimumDay = TimeUnit.DAYS.toMillis(3);
        val expiredDateInMillis = expiredDate - currentTime - minimumDay // notification pada H-3 sebelum kedaluwarsa
        if(expiredDateInMillis < 0) alarmManager!![AlarmManager.RTC_WAKEUP, currentTime] = pendingIntent
        alarmManager!![AlarmManager.RTC_WAKEUP, currentTime + expiredDateInMillis] = pendingIntent
    }
}