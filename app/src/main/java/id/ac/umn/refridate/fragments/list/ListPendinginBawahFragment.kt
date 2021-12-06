package id.ac.umn.refridate.fragments.list

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import id.ac.umn.refridate.HomeScreen
import id.ac.umn.refridate.R
import id.ac.umn.refridate.model.ItemList
import kotlinx.android.synthetic.main.fragment_pendinginbawah_list.view.*
import kotlinx.android.synthetic.main.fragment_pendinginbawah_list.view.floatingActionButton
import java.lang.Exception

class ListPendinginBawahFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var itemList: List<ItemList>

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListPendinginBawahAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pendinginbawah_list, container, false)

        database = FirebaseDatabase.getInstance().getReference("ItemList")
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val itemListQuery = database.child(userId).orderByChild("place").equalTo("Pendingin Bawah")

        itemList = ArrayList()
        recyclerView = view.recyclerViewPendinginBawah
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ListPendinginBawahAdapter()

        itemListQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    itemList = ArrayList()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(ItemList::class.java)
                        item?.let { (itemList as ArrayList<ItemList>).add(it) }
                        Log.d(ContentValues.TAG, "onDataChange (KEY) : " + itemSnapshot.key!!)
                    }
                    adapter.setData(itemList)
                    recyclerView.adapter = adapter
                } catch (e: Exception) {
                    Log.e("List Data Fragment", e.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "itemList:onCancelled", error.toException())
                Toast.makeText(context, "Gagal memuat data.", Toast.LENGTH_SHORT).show()
            }
        })

        view.floatingActionButton.setOnClickListener{
            findNavController().navigate(R.id.action_listPendinginBawahFragment_to_addPendinginBawahFragment)
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        inflater.inflate(R.menu.navigation_drawer,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.Delete_all)
        {
            deleteAllItems()
        }
        if(item.itemId == R.id.Home)
        {
            val intent = Intent (activity, HomeScreen::class.java)
            activity?.startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


    private fun deleteAllItems(){
        val builder = AlertDialog.Builder(requireContext())
        database = FirebaseDatabase.getInstance().getReference("ItemList")
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val itemListQuery = database.child(userId).orderByChild("place").equalTo("Pendingin Bawah")
        builder.setPositiveButton("Iya"){_, _ ->
            itemListQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        for (itemSnapshot in snapshot.children) {
                            itemSnapshot.ref.removeValue()
                        }
                        Toast.makeText(requireContext(),"Semua berhasil di hapus", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("List Data Fragment", e.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "itemList:onCancelled", error.toException())
                    Toast.makeText(context, "Gagal menghapus seluruh data.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        builder.setNegativeButton("Tidak"){_, _ -> }
        builder.setTitle("Hapus semua?")
        builder.setMessage("Apakah anda yakin menghapus semua?")
        builder.create().show()
    }


}