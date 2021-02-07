package com.kieronquinn.app.taptap.utils.extensions

import androidx.lifecycle.*

fun <T> MutableLiveData<T>.update(newValue: T) {
    if(value != newValue) postValue(newValue)
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit){
    observe(lifecycleOwner, Observer {
        observer.invoke(it)
    })
}

fun <T> MutableLiveData<T>.observeOneShot(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit){
    val distinctLiveData = this.distinctUntilChanged()
    observe(lifecycleOwner,
        OneTimeNotNullObserver(
            distinctLiveData
        ) {
            observer.invoke(it)
        })
}

class OneTimeNotNullObserver<T>(private val liveData: LiveData<T>, private val callback: (T) -> Unit): Observer<T> {

    override fun onChanged(t: T) {
        if(t != null) {
            liveData.removeObserver(this)
            callback.invoke(t)
        }
    }

}