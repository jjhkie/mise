package com.work.mise.data.model.tmcoordinates


import com.google.gson.annotations.SerializedName

data class TmCoordinatestResponse(
    @SerializedName("documents")
    val documents: List<Document>?,
    @SerializedName("meta")
    val meta: Meta?
)