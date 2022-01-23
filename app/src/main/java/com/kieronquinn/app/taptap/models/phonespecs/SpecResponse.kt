package com.kieronquinn.app.taptap.models.phonespecs

import com.google.gson.annotations.SerializedName

class SpecResponse {

    @SerializedName("Success")
    var success: Int? = null
    var data: Data? = null

    inner class Data {

        @SerializedName("product")
        var products: Array<Product>? = null

        inner class Product {

            @SerializedName("Design")
            var design: Design? = null

            @SerializedName("Product")
            var product: Product? = null

            @SerializedName("Image")
            var image: Image? = null

            inner class Design {
                @SerializedName("Height")
                var height: String? = null
            }

            inner class Product {
                @SerializedName("Model")
                var model: String? = null
            }

            inner class Image {
                @SerializedName("Front")
                var front: String? = null
            }

        }

    }

}