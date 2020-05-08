package com.raywenderlich.android.creaturemon.addcreature

import com.raywenderlich.android.creaturemon.data.model.AttributeStore
import com.raywenderlich.android.creaturemon.data.model.CreatureAttributes
import com.raywenderlich.android.creaturemon.data.model.CreatureGenerator
import com.raywenderlich.android.creaturemon.data.repository.CreatureRepository
import com.raywenderlich.android.creaturemon.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.util.*

class AddCreatureProcessorHolder(
        private val creatureRepository: CreatureRepository,
        private val creatureGenerator: CreatureGenerator,
        private val schedulerProvider: BaseSchedulerProvider
) {
    private val avatarProcessor =
            ObservableTransformer<AddCreatureAction.AvatarAction, AddCreatureResult.AvatarResult> { actions ->
                actions
                        .map { action -> AddCreatureResult.AvatarResult.Success(action.drawable)}
                        .cast(AddCreatureResult.AvatarResult::class.java)
                        .onErrorReturn(AddCreatureResult.AvatarResult::Failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddCreatureResult.AvatarResult.Processing)

            }
    private val nameProcessor =
            ObservableTransformer<AddCreatureAction.NameAction, AddCreatureResult.NameResult> { actions ->
                actions
                        .map {action -> AddCreatureResult.NameResult.Success(action.name)}
                        .cast(AddCreatureResult.NameResult::class.java)
                        .onErrorReturn(AddCreatureResult.NameResult::Failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddCreatureResult.NameResult.Processing)
            }
    private val intelligenceProcessor =
            ObservableTransformer<AddCreatureAction.IntelligenceAction, AddCreatureResult.IntelligenceResult> { actions ->
                actions
                        .map { action ->
                            AddCreatureResult.IntelligenceResult.Success(
                                    AttributeStore.INTELLIGENCE[action.intelligenceIndex].value
                            )
                        }
                        .cast(AddCreatureResult.IntelligenceResult::class.java)
                        .onErrorReturn(AddCreatureResult.IntelligenceResult::Failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddCreatureResult.IntelligenceResult.Processing)
            }
    private val strengthProcessor =
            ObservableTransformer<AddCreatureAction.StrengthAction, AddCreatureResult.StrengthResult> { actions ->
                actions
                        .map { action ->
                            AddCreatureResult.StrengthResult.Success(
                                    AttributeStore.STRENGTH[action.strengthIndex].value
                            )
                        }
                        .cast(AddCreatureResult.StrengthResult::class.java)
                        .onErrorReturn(AddCreatureResult.StrengthResult::Failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddCreatureResult.StrengthResult.Processing)
            }
    private val enduranceProcessor =
            ObservableTransformer<AddCreatureAction.EnduranceAction, AddCreatureResult.EnduranceResult> { actions ->
                actions
                        .map { action ->
                            AddCreatureResult.EnduranceResult.Success(
                                    AttributeStore.ENDURANCE[action.enduranceIndex].value
                            )
                        }
                        .cast(AddCreatureResult.EnduranceResult::class.java)
                        .onErrorReturn(AddCreatureResult.EnduranceResult::Failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(AddCreatureResult.EnduranceResult.Processing)
            }
    private val saveProcessor =
            ObservableTransformer<AddCreatureAction.SaveAction, AddCreatureResult.SaveResult> { actions ->
                actions.flatMap { action ->
                    val attributes = CreatureAttributes(
                            AttributeStore.INTELLIGENCE[action.intelligenceIndex].value,
                            AttributeStore.STRENGTH[action.strengthIndex].value,
                            AttributeStore.ENDURANCE[action.enduranceIndex].value
                    )
                    val creature = creatureGenerator.generateCreature(attributes, action.name, action.drawable)
                    creatureRepository.saveCreature(creature)
                            .map { AddCreatureResult.SaveResult.Success }
                            .cast(AddCreatureResult.SaveResult::class.java)
                            .onErrorReturn(AddCreatureResult.SaveResult::Failure)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .startWith(AddCreatureResult.SaveResult.Processing)
                }
            }
    internal var actionProcessor =
            ObservableTransformer<AddCreatureAction, AddCreatureResult> { actions ->
                actions.publish { shared ->
                    Observable.merge(
                            shared.ofType(AddCreatureAction.AvatarAction::class.java).compose(avatarProcessor),
                            shared.ofType(AddCreatureAction.NameAction::class.java).compose(nameProcessor),
                            shared.ofType(AddCreatureAction.IntelligenceAction::class.java).compose(intelligenceProcessor),
                            shared.ofType(AddCreatureAction.StrengthAction::class.java).compose(strengthProcessor))
                            .mergeWith(shared.ofType(AddCreatureAction.EnduranceAction::class.java).compose(enduranceProcessor))
                            .mergeWith(shared.ofType(AddCreatureAction.SaveAction::class.java).compose(saveProcessor))
                            .mergeWith(
                                    shared.filter { v ->
                                        v !is AddCreatureAction.AvatarAction
                                        && v !is AddCreatureAction.NameAction
                                                && v !is AddCreatureAction.IntelligenceAction
                                                && v !is AddCreatureAction.StrengthAction
                                                && v !is AddCreatureAction.EnduranceAction
                                                && v !is AddCreatureAction.SaveAction
                                    }.flatMap { w ->
                                        Observable.error<AddCreatureResult>(
                                                IllegalArgumentException("Unknown Action type: $w")
                                        )
                                    }
                            )
                }
            }
}