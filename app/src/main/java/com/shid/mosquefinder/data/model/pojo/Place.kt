package com.shid.mosquefinder.data.model.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.maps.model.PlacesSearchResult

data class Place (

    @SerializedName("html_attributions")
    @Expose
    var htmlAttributions:List<Any> ,

    @SerializedName("next_page_token")
    @Expose
    var nextPageToken:String ?= null,

    @SerializedName("results")
    @Expose
    var results:List<PlacesSearchResult> ,

    @SerializedName("status")
    @Expose
     var status:String
){

/*

    */
/**
     *
     * @return
     * The htmlAttributions
     *//*

    fun getHtmlAttributions():List<Any>{
        return htmlAttributions
    }


    */
/**
     *
     * @param htmlAttributions
     * The html_attributions
     *//*

    fun setHtmlAttributions(htmlAttributions:List<Any>){
        this.htmlAttributions = htmlAttributions
    }


    */
/**
     *
     * @return
     * The nextPageToken
     *//*

    fun getNextPageToken():String{
        return nextPageToken
    }


    */
/**
     *
     * @param nextPageToken
     * The next_page_token
     *//*

    fun setNextPageToken(nextPageToken:String){
        this.nextPageToken = nextPageToken
    }


    */
/**
     *
     * @return
     * The results
     *//*

    fun getResults():List<Result>{
        return results
    }

    */
/**
     *
     * @param results
     * The results
     *//*

    fun setResults(results:List<Result>){
        this.results = results
    }


    */
/**
     *
     * @return
     * The status
     *//*

    fun getStatus():String{
        return status
    }


    */
/**
     *
     * @param status
     * The status
     *//*

    fun setStatus(status:String){
        this.status = status
    }

*/

}