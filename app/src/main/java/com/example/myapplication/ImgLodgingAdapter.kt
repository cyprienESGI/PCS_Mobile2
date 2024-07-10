package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ImgLodgingAdapter : BaseAdapter {
    var context: Context
    var ListLodging: MutableList<ImgLodging>

    constructor(context: Context, ListImgLodgings: MutableList<ImgLodging>):super() {
        this.context = context
        this.ListLodging = ListImgLodgings
    }

    override fun getCount(): Int {
        return this.ListLodging.size
    }

    override fun getItem(position: Int): Any {
        return this.ListLodging[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v: View
        if (convertView == null) {
            var inflater = LayoutInflater.from(this.context)
            v = inflater.inflate(R.layout.row_lodging, null)
        } else {
            v = convertView
        }
        var current:Lodging = getItem(position) as Lodging
        if (-1 != current.img_lodging_id) {
            val imageView = v.findViewById<ImageView>(R.id.lodging_image)
            Picasso.get().load("http://192.168.1.3:5173/public/images/image-1717769066259.jpg").into(imageView);
        }else{
            val imageView = v.findViewById<ImageView>(R.id.lodging_image)
            Picasso.get().load(R.drawable.default_image_mainson).into(imageView);
        }

        return v
    }
}