package com.raywenderlich.android.creaturemon.allcreatures

import com.raywenderlich.android.creaturemon.mvibase.MviIntent

sealed class AllCreaturesIntent : MviIntent {
    object LoadAllCreaturesIntent: AllCreaturesIntent()
    object ClearAllCreaturesIntent: AllCreaturesIntent()
}