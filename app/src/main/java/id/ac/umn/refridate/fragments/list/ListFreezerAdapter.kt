package id.ac.umn.refridate.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import id.ac.umn.refridate.R
import id.ac.umn.refridate.model.ItemList
import kotlinx.android.synthetic.main.display_row.view.*

class ListFreezerAdapter: RecyclerView.Adapter<ListFreezerAdapter.MyViewHolder>() {
    private var itemList = emptyList<ItemList>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.display_row, parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.itemView.nama_txt.text = currentItem.name
        holder.itemView.expDate.text = currentItem.exp_date_time
        holder.itemView.imageView.load(currentItem.image)

        holder.itemView.displayLayout.setOnClickListener{
            val action1 = ListFreezerFragmentDirections.actionListFreezerFragmentToUpdateFreezerFragment(currentItem)
            holder.itemView.findNavController().navigate(action1)
        }
        holder.itemView.displayItem.setOnClickListener{
            val action2 = ListFreezerFragmentDirections.actionListFreezerFragmentToDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action2)
        }
    }

    fun setData(item: List<ItemList>)
    {
        this.itemList = item
        notifyDataSetChanged()
    }
}
