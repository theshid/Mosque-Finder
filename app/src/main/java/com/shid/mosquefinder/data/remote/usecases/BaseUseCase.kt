package com.shid.mosquefinder.data.remote.usecases

interface BaseUseCase <in Parameter, out Result> {
    suspend operator fun invoke(params: Parameter): Result
}