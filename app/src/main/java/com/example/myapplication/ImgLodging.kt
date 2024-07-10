package com.example.myapplication

class ImgLodging {
    constructor(
        img_lodging_id: Int,
        url: String,
        lodging_id: Int?,
        category_id: Int?)
    {
        this.img_lodging_id = img_lodging_id
        this.url = url

        if (lodging_id != null) {
            this.lodging_id = lodging_id
        }
        if (category_id != null) {
            this.category_id = category_id
        }
    }


    var img_lodging_id = -1
    var url = ""
    var lodging_id = -1
    var category_id = -1
}