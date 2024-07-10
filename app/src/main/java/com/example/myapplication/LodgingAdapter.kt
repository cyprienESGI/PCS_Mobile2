package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso

class LodgingAdapter : BaseAdapter {
        var context: Context
        var ListLodging: MutableList<Lodging>

        constructor(context: Context, ListLodgings: MutableList<Lodging>):super() {
            this.context = context
            this.ListLodging = ListLodgings
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
        v.findViewById<TextView>(R.id.lodging_title).text = current.title
        v.findViewById<TextView>(R.id.lodging_address).text = "Adresse : " + current.address
        v.findViewById<TextView>(R.id.lodging_price).text = "Prix : " + current.price + "€ / Jour"
        v.findViewById<TextView>(R.id.lodging_capacity).text = "Capacité : " + current.capacity.toString()
//        if (-1 == current.img_lodging_id) {
            val imageView = v.findViewById<ImageView>(R.id.lodging_image)
            imageView.setImageResource(R.drawable.default_image_mainson)
//        }else{
//            val imageView = v.findViewById<ImageView>(R.id.lodging_image)
//            var url = MyApp.URL_WEB + current.url
//            Picasso.get().load("$url").into(imageView);
//        }
//        val ratingStars = v.findViewById<RatingStars>(R.id.ratingStars)
//        ratingStars.setNoteValue(0)

        val LinearLayout = v.findViewById<LinearLayout>(R.id.row_lodging)

        LinearLayout.setOnClickListener {
            Log.e("MyLog2", current.lodging_id.toString())
            val intent = Intent(context, SettingLodging::class.java).apply {
                putExtra("lodgingId", current.lodging_id)
            }
            context.startActivity(intent)
        }

        return v
    }


}