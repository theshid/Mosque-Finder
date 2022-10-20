package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.data.model.BeautifulMosques

interface BeautifulMosquesRepository {
    fun getMosquesFromFirebase(): List<BeautifulMosques>
}