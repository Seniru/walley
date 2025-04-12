package com.seniru.walley.persistence

interface DataStorable<E> {
    fun get(index: Int): E
    fun push(item: E)
    fun replace(index: Int, item: E)
    fun delete(index: Int)
    fun readAll(): ArrayList<E>
    fun save()
}