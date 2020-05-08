package com.raywenderlich.android.creaturemon.data.model.allcreatures

import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesIntent
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesProcessorHolder
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesViewModel
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesViewState
import com.raywenderlich.android.creaturemon.data.model.Creature
import com.raywenderlich.android.creaturemon.data.model.CreatureAttributes
import com.raywenderlich.android.creaturemon.data.model.CreatureGenerator
import com.raywenderlich.android.creaturemon.data.repository.CreatureRepository
import com.raywenderlich.android.creaturemon.util.schedulers.BaseSchedulerProvider
import com.raywenderlich.android.creaturemon.util.schedulers.ImmediateSchedulerProvider
import io.reactivex.Flowable.just
import io.reactivex.Maybe.error
import io.reactivex.Maybe.just
import io.reactivex.Observable.error
import io.reactivex.Observable.just
import io.reactivex.Single.just
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.*

class AllCreaturesViewModelTest {
    @Mock
    private lateinit var creatureRepository: CreatureRepository
    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var generator: CreatureGenerator
    private lateinit var viewModel: AllCreaturesViewModel
    private lateinit var testObserver: TestObserver<AllCreaturesViewState>
    private lateinit var creatures: List<Creature>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        schedulerProvider = ImmediateSchedulerProvider

        generator = CreatureGenerator()

        viewModel = AllCreaturesViewModel(AllCreaturesProcessorHolder(creatureRepository,
        schedulerProvider))

        creatures = listOf(
                generator.generateCreature(CreatureAttributes(3,7,10), "Creature 1", 1),
                generator.generateCreature(CreatureAttributes(7,10,3), "Creature 2", 1),
                generator.generateCreature(CreatureAttributes(10,3,7), "Creature 3", 1)
        )

        testObserver = viewModel.states().test()
    }

    @Test
    fun loadAllCreaturesFromRepositoryAndLoadIntoView() {
        `when`(creatureRepository.getAllCreatures()).thenReturn(just(creatures))

        viewModel.processIntents(just(AllCreaturesIntent.LoadAllCreaturesIntent))
    testObserver.assertValueAt(1, AllCreaturesViewState::isLoading)
        testObserver.assertValueAt(2) { allCreaturesViewState: AllCreaturesViewState ->
            !allCreaturesViewState.isLoading
        }
    }

    @Test
    fun errorLoadingCreaturesShowsError() {
        `when` (creatureRepository.getAllCreatures()).thenReturn(error(Exception()))

        viewModel.processIntents(just(AllCreaturesIntent.LoadAllCreaturesIntent))
        testObserver.assertValueAt(2) {state -> state.error != null }
    }
}