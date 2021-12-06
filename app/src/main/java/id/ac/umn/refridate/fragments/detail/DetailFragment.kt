package id.ac.umn.refridate.fragments.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import coil.load
import id.ac.umn.refridate.R

import kotlinx.android.synthetic.main.fragment_detail.view.*


class detailFragment : Fragment() {

    private val args by navArgs<detailFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        view.tvName.setText(args.currItem.name)
        view.tvLocation.setText(args.currItem.place)
        view.tvDate.setText(args.currItem.exp_date_time)
        view.ivItemImage.load(args.currItem.image)

        return view
    }
}