/*

 *
 */

@file:JvmName("PlaceTypeExtensions")


package com.example.smoke

enum class PlaceType(@JvmField val value: Int) {
    POI(0),
    AREA(1),
    STREET(2);
}
