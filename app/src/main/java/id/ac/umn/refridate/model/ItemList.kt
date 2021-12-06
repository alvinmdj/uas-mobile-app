package id.ac.umn.refridate.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemList(
        var id: String = "",
        var name: String = "",
        var place: String = "",
        var exp_date_time: String = "",
        var image: String = ""
): Parcelable