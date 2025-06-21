package com.yoopi.ui.login

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Sends the value to each observer once, then forgets it.
 * You don’t have to understand it—just leave it as is.
 */
class EventLiveData<T> : MutableLiveData<T>() {
    private val delivered = HashSet<Observer<in T>>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        delivered.remove(observer)                       // reset when recreated
        super.observe(owner) {
            if (observer !in delivered) {
                delivered += observer
                observer.onChanged(it)
            }
        }
    }
}
