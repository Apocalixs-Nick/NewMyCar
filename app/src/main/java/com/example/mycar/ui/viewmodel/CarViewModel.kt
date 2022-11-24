package com.example.mycar.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.mycar.data.MyCarDao
import com.example.mycar.model.MyCar
import com.example.mycar.network.MyCarApi
import com.example.mycar.network.MyCarApiService
import com.example.mycar.network.MyCarInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

class CarViewModel(private val myCarDao: MyCarDao) : ViewModel() {

    //Variable for the acquisition of all the cars of the database
    val allCar: LiveData<List<MyCar>> = myCarDao.getCars().asLiveData()

    private val _brand = MutableLiveData<List<MyCarInfo>>()

    val brand: LiveData<List<MyCarInfo>>
        get() = _brand

    var checkedItemBrand = -1

    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    fun isValidEntry(
        name: String,
        brand: String,
        power: String,
        numberDoors: String,
        fuel: String,
        productionYear: String,
        places: String,
        color: String
    ): Boolean {

        if ((name.isBlank() || brand.isBlank() || power.isBlank() || fuel.isBlank() || numberDoors.isBlank() || productionYear.isBlank() || places.isBlank() || color.isBlank())) {
            return false
        }
        return true
    }

    /**
     * Function to recover a car via id
     */
    fun retrieveCar(id: Long): LiveData<MyCar> {
        return myCarDao.getCar(id).asLiveData()
    }

    /**
     * Function for add a new car
     */
    fun addCar(
        name: String,
        brand: String,
        power: String,
        fuel: String,
        secondFuel: String?,
        numberDoors: String,
        productionYear: String,
        image: Bitmap,
        places: String,
        color: String
    ) {
        val car = MyCar(
            name = name,
            brand = brand,
            power = power.toInt(),
            fuel = fuel,
            secondFuel = secondFuel,
            image = image.toByteArray(),
            numberDoors = numberDoors.toInt(),
            productionYear = productionYear.toInt(),
            places = places.toInt(),
            color = color
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                myCarDao.insert(car)
            } catch (e: Exception) {
                Log.e("NOT ADD CAR", e.toString())
            }
        }
    }

    //can serve
    /*fun controlFuel(car: MyCar): Boolean {

       if (car.fuel != car.secondFuel) {
           return true
       }
       return false
   }*/

    /**
     * Function for update a car
     */
    fun updateCar(
        id: Long,
        name: String,
        brand: String,
        power: String,
        fuel: String,
        secondFuel: String?,
        numberDoors: String,
        productionYear: String,
        image: Bitmap,
        places: String,
        color: String
    ) {
        val car = MyCar(
            id = id,
            name = name,
            brand = brand,
            power = power.toInt(),
            fuel = fuel,
            secondFuel = secondFuel,
            numberDoors = numberDoors.toInt(),
            productionYear = productionYear.toInt(),
            image = image.toByteArray(),
            places = places.toInt(),
            color = color
        )

        viewModelScope.launch(Dispatchers.IO) {
            myCarDao.update(car)
        }
    }

    /**
     * Function for delete a car
     */
    fun deleteCar(car: MyCar) {
        viewModelScope.launch(Dispatchers.IO) {
            myCarDao.delete(car)
        }
    }

    fun brandAcquisition() = CoroutineScope(Dispatchers.Main).launch {
        try {
            if (_brand.value == null) {
                //_brand.value = MyCarApi.retrofitService.getMyCarInfo()
                _brand.postValue(MyCarApi.retrofitService.getMyCarInfo())
            }

            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false

        } catch (networkError: IOException) {
            _eventNetworkError.value = true
        }
    }

    fun getBrand(): List<String> {
        return _brand.value!!.map { e -> e.maker }.distinct()
    }


}


private fun Bitmap.toByteArray(quality: Int = 50): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

class CarViewModelFactory(private val myCarDao: MyCarDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarViewModel(myCarDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}