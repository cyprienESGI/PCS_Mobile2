package com.example.myapplication

class Lodging {
    constructor(
        apartments_id: Int,
        address: String,
        price: Int,
        owner_email: String,
        capacity: Int,
        surface: Int,
        apartment_type: String,
        name: String,

    ) {
        this.lodging_id = apartments_id
        this.address = address
//        this.city = city
//        this.zip_code_lodging = zip_code_lodging
        this.price = price
//        this.description = description
        this.owner_email = owner_email
//        this.validate = validate
        this.capacity = capacity
        this.surface = surface
        this.property_type = apartment_type
        this.title = name
//        this.rating = rating
//        if (img_lodging_id != null) {
//            this.img_lodging_id = img_lodging_id
//        }
//        if (url != null) {
//            this.url = url
//        }

    }

    var lodging_id = 0
    var address = ""
//    var city = ""
//    var zip_code_lodging = ""
    var price = 0
//    var description = ""
    var owner_email = ""
//    var validate = 0
    var capacity = 0
    var surface = 0
    var property_type = ""
    var title = ""
//    var rating = 0
    var img_lodging_id = -1
    var url = ""

}
