package com.university.MarathonOnlineAPI.mapper

interface Mapper<D, E> {
    fun toDto(entity: E): D
    fun toEntity(dto: D): E
}