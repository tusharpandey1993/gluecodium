/*

 *
 */

@file:JvmName("SuggestionExtensions")


package com.example.smoke

data class Suggestion(
    val title: String,
    val type: SuggestionType,
    val place: Place? = null,
    val id: String? = null,
    val href: String? = null
) {


    external fun getHighlights() : Map<String, List<Int>>


}
