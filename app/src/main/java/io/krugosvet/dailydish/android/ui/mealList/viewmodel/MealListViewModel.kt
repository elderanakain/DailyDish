package io.krugosvet.dailydish.android.ui.mealList.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.krugosvet.dailydish.android.architecture.extension.OnClick
import io.krugosvet.dailydish.android.architecture.viewmodel.ViewModel
import io.krugosvet.dailydish.android.reminder.notification.ReminderNotificationService
import io.krugosvet.dailydish.android.repository.meal.Meal
import io.krugosvet.dailydish.android.repository.meal.MealRepository
import io.krugosvet.dailydish.android.ui.mealList.view.MealVisual
import io.krugosvet.dailydish.android.ui.mealList.view.MealVisualFactory
import io.krugosvet.dailydish.core.service.DateService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MealListViewModel(
  savedStateHandle: SavedStateHandle,
  private val mealVisualFactory: MealVisualFactory,
  private val mealRepository: MealRepository,
  private val dateService: DateService,
  private val reminderNotificationService: ReminderNotificationService
) :
  ViewModel<MealListViewModel.Event>(savedStateHandle) {

  sealed class Event : NavigationEvent() {
    class ShowImagePicker(val meal: Meal) : Event()
  }

  val mealList: LiveData<List<MealVisual>> =
    mealRepository.meals
      .map { meals ->
        meals.sortedBy { it.lastCookingDate }
      }
      .map { meals ->
        meals.map {
          mealVisualFactory.from(it, onDelete(it), onImageClick(it), onCookTodayClick(it))
        }
      }
      .asLiveData()

  fun changeImage(meal: Meal, image: Uri) {
    viewModelScope.launch {
      mealRepository.update(meal.copy(image = image))
    }
  }

  private fun onDelete(meal: Meal): OnClick = {
    viewModelScope.launch {
      mealRepository.delete(meal)
    }
  }

  private fun onImageClick(meal: Meal): OnClick = {
    viewModelScope.launch {
      navigate(Event.ShowImagePicker(meal))
    }
  }

  private fun onCookTodayClick(meal: Meal): OnClick = {
    reminderNotificationService.closeReminder()

    viewModelScope.launch {
      mealRepository.update(meal.copy(lastCookingDate = dateService.currentDate.time))
    }
  }
}