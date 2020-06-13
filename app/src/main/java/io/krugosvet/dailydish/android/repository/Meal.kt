package io.krugosvet.dailydish.android.repository

import io.krugosvet.dailydish.android.db.meal.MealEntity
import io.krugosvet.dailydish.android.service.DateService
import io.krugosvet.dailydish.android.service.IdGenerator
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class Meal private constructor(
  val id: Long,
  val title: String,
  val description: String,
  val imageUri: String,
  val lastCookingDate: Date
) {

  class MealFactory(
    private val dateService: DateService,
    private val idGenerator: IdGenerator
  ) {

    fun create(
      title: String,
      description: String,
      date: String,
      mainImage: String
    ) =
      Meal(
        id = idGenerator.generate(),
        title = title,
        description = description,
        lastCookingDate = dateService.defaultFormatDate(date),
        imageUri = mainImage
      )


    fun from(mealEntity: MealEntity) =
      Meal(
        id = mealEntity.id,
        title = mealEntity.title,
        description = mealEntity.description,
        imageUri = mealEntity.imageUri,
        lastCookingDate = dateService.toDate(mealEntity.lastCookingDate)
      )

    fun toEntity(meal: Meal) =
      MealEntity(
        id = meal.id,
        title = meal.title,
        description = meal.description,
        imageUri = meal.imageUri,
        lastCookingDate = meal.lastCookingDate.time
      )
  }
}