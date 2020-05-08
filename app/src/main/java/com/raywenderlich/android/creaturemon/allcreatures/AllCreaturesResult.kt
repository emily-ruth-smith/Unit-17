package com.raywenderlich.android.creaturemon.allcreatures

import com.raywenderlich.android.creaturemon.data.model.Creature
import com.raywenderlich.android.creaturemon.mvibase.MviResult

sealed class AllCreaturesResult : MviResult {
    sealed class LoadAllCreaturesResult : AllCreaturesResult() {
        object Loading : LoadAllCreaturesResult()
        data class Success(val creatures: List<Creature>) : LoadAllCreaturesResult()
        data class Failure(val error: Throwable) : LoadAllCreaturesResult()
    }
    sealed class ClearAllCreaturesResult : AllCreaturesResult() {
        object Clearing : ClearAllCreaturesResult()
        object Success : ClearAllCreaturesResult()
        data class  Failure(val error: Throwable) : ClearAllCreaturesResult()

    }
}