package com.example.mycar.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.mycar.data.MyCarDao
import com.example.mycar.model.MyCar
import com.example.mycar.network.car.MyCarApi
import com.example.mycar.network.car.MyCarInfo
import com.example.mycar.network.fuel.FuelApi
import com.example.mycar.network.fuel.FuelInfo
import com.example.mycar.network.logo.MyCarApiLogo
import com.example.mycar.network.logo.MyCarLogo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

enum class LogoApiStatus {
    LOADING,
    ERROR,
    DONE
}

class CarViewModel(private val myCarDao: MyCarDao) : ViewModel() {

    //Variable for the acquisition of all the cars of the database
    val allCar: LiveData<List<MyCar>> = myCarDao.getCars().asLiveData()

    private val _brand = MutableLiveData<List<MyCarInfo>>()

    val brand: LiveData<List<MyCarInfo>>
        get() = _brand

    var checkedItemBrand = -1

    var checkedItemModel = -1

    var checkedItemFuel = -1

    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    private val _fuel = MutableLiveData<List<FuelInfo>>()

    val fuel: LiveData<List<FuelInfo>>
        get() = _fuel

    // Status Logo Api
    private val _statusLogApi = MutableLiveData<LogoApiStatus>()
    val statusLogApi: LiveData<LogoApiStatus> = _statusLogApi

    private val _logoDataApi = MutableLiveData<List<MyCarLogo>>()
    val logoDataApi: LiveData<List<MyCarLogo>> = _logoDataApi

    init {
        getLogo()
    }

    /**
     * Function for checking the entered fields that they are not blank
     */
    fun isValidEntry(
        name: String,
        brand: String,
        power: String,
        numberDoors: String,
        fuel: String,
        productionYear: String,
        places: String,
        color: String,
        km: String
    ): Boolean {
        if ((name.isBlank() || brand.isBlank() || power.isBlank() || fuel.isBlank() || numberDoors.isBlank() || productionYear.isBlank() || places.isBlank() || color.isBlank() || km.isBlank())) {
            return false
        }
        if (power.toInt() < 1 || power.toInt() > 9999) {
            return false
        }
        if (numberDoors.toInt() < 1 || numberDoors.toInt() > 9) {
            return false
        }
        if (productionYear.toInt() < 1800 || productionYear.toInt() > 2050) {
            return false
        }
        if (places.toInt() < 1 || places.toInt() > 9) {
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
        color: String,
        km: String
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
            color = color,
            kM = km.toInt()
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
        color: String,
        km: String
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
            color = color,
            kM = km.toInt()
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

    /**
     * Function for capturing car data via API
     */
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

    /**
     * Function for brand acquisition via API
     */
    fun getBrand(): List<String> {
        return _brand.value!!.map { e -> e.maker }.distinct()
    }

    /**
     * Function for capturing patterns by brand via API
     */
    fun getModel(maker: String): List<String> {
        val makerList = _brand.value!!.filter { e -> e.maker == maker }
        return makerList.map { e -> e.model }.distinct().sorted()
    }

    /**
     * Gets Logo information from the Vehicle API Retrofit service and updates the
     * [_logoDataApi] [List] [LiveData].
     */
    fun getLogo() {
        viewModelScope.launch {
            _statusLogApi.value = LogoApiStatus.LOADING
            try {
                _logoDataApi.value = MyCarApiLogo.retrofitService.getMyCarLogo()
                _statusLogApi.value = LogoApiStatus.DONE
            } catch (e: java.lang.Exception) {
                _statusLogApi.value = LogoApiStatus.ERROR
                _logoDataApi.value = listOf()
            }
        }
    }

    /**
     * Function for acquiring fuel data via API
     */
    fun fuelAcquisition() = CoroutineScope(Dispatchers.Main).launch {
        try {
            if (_fuel.value == null) {
                _fuel.postValue(FuelApi.retrofitService.getFuelInfo())
            }

            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false

        } catch (networkError: IOException) {
            _eventNetworkError.value = true
        }
    }

    /**
     * Function to save fuel fuels in a list by API
     */
    fun getFuel(): List<String> {
        return _fuel.value!!.map { e -> e.nameFuel }.distinct()
    }

    /**
     * Function for refresh information from network
     */
    fun refreshDataFromNetwork() = viewModelScope.launch {
        try {
            _brand.value = MyCarApi.retrofitService.getMyCarInfo()
            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false

        } catch (networkError: IOException) {
            _eventNetworkError.value = true
        }
    }
}


private fun Bitmap.toByteArray(quality: Int = 100): ByteArray {
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