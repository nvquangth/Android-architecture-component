package com.sournary.architecturecomponent.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.observe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * The lazy property that is cleaned up when fragment's view is destroyed.
 * Accessing this property after fragment's view is destroyed thrown NPE.
 */
class AutoClearedValue<T : Any>(fragment: Fragment) : ReadWriteProperty<Fragment, T> {

    private var _value: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) {
            it.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    _value = null
                }
            })
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
        _value ?: throw IllegalStateException("This value has not been initialized")

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }

}
