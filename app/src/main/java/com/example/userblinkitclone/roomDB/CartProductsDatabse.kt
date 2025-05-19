package com.example.userblinkitclone.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartProducts::class], version = 1, exportSchema = false)

abstract class CartProductsDatabse: RoomDatabase() {

    abstract fun cartProductsDao() : CartProductsDao

    companion object {

        @Volatile
        var INSTANCE : CartProductsDatabse? = null

        fun getDatabaseInstance(context: Context) : CartProductsDatabse {
            val tempInstance = INSTANCE
            if(tempInstance != null) return tempInstance

            synchronized(this){
                val roomDb = Room.databaseBuilder(context, CartProductsDatabse::class.java, "CartProducts").allowMainThreadQueries().build()
                INSTANCE = roomDb
                return roomDb
            }
        }
    }

}