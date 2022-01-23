package com.kieronquinn.app.taptap.models.phonespecs

import com.google.gson.annotations.SerializedName

class SearchResponse {

    @SerializedName("Success")
    var success: Int? = null
    var data: Data? = null

    inner class Data {

        var results: Array<Product>? = null

        inner class Product {

            @SerializedName("_meta")
            var meta: Meta? = null

            inner class Meta {
                var id: String? = null
            }

        }

    }

}