package rs.smobile.catsvsdogs.data.repository

import rs.smobile.catsvsdogs.data.local.model.Image


interface ImageRepository {

    suspend fun getRandomImage(): Image

}