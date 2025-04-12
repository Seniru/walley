package com.seniru.walley.persistence

interface DataStorable<E> {
    fun push(item: E)
    fun delete(index: Int)
    fun readAll(): ArrayList<E>
    fun save()
}