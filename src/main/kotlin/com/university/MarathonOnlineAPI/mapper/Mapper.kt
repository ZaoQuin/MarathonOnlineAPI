package com.university.MarathonOnlineAPI.mapper

interface Mapper<D, E> {
    fun toDTO(entity: E): D
    fun toEntity(dto: D): E
}