/*

 *
 */

@file:JvmName("PlaceExtensions")


package com.example.smoke

data class Place(
    val title: String,
    val id: String,
    val placeType: PlaceType,
    val latitude: Double,
    val longitude: Double,
    val distanceInMeters: Int? = null,
    val politicalView: String? = null
) {


    external fun serializeCompact() : String


    companion object {

        @JvmStatic external fun deserialize(serializedPlace: String) : Place
    }
}
