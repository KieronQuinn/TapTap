package com.kieronquinn.app.taptap.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> MutableLiveData<T>.update(newValue: T) {
    if(value != newValue) postValue(newValue)
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit){
    observe(lifecycleOwner, Observer {
        observer.invoke(it)
    })
}