package io.krugosvet.dailydish.android.ui.addMeal.view

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.lifecycle.Observer
import io.krugosvet.dailydish.android.BR
import io.krugosvet.dailydish.android.R
import io.krugosvet.dailydish.android.architecture.aspect.BindingComponent
import io.krugosvet.dailydish.android.architecture.extension.isEmpty
import io.krugosvet.dailydish.android.architecture.extension.subscribeOnIoThread
import io.krugosvet.dailydish.android.architecture.injection.activityInject
import io.krugosvet.dailydish.android.architecture.view.BaseFragment
import io.krugosvet.dailydish.android.databinding.DialogAddMealBinding
import io.krugosvet.dailydish.android.service.ImagePickerService
import io.krugosvet.dailydish.android.service.KeyboardService
import io.krugosvet.dailydish.android.ui.addMeal.viewmodel.AddMealViewModel
import io.krugosvet.dailydish.core.service.DateService
import io.krugosvet.dailydish.core.service.day
import io.krugosvet.dailydish.core.service.month
import io.krugosvet.dailydish.core.service.year
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class AddMealFragment :
  BaseFragment<DialogAddMealBinding, AddMealViewModel>(),
  DatePickerDialog.OnDateSetListener {

  override val viewModel: AddMealViewModel by stateViewModel()
  override val bindingComponent = BindingComponent(R.layout.dialog_add_meal, this, BR.viewModel)

  private val dateService: DateService by inject()

  private val imagePickerService: ImagePickerService by activityInject()
  private val keyboardService: KeyboardService by activityInject()

  override val parentContext: Context
    get() = requireContext()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    keyboardService.showKeyboard(binding.title.editText!!)

    viewModel.navigationEvent
      .observe(viewLifecycleOwner, Observer {
        when (it) {
          AddMealViewModel.Event.Close -> close()
          AddMealViewModel.Event.ShowImagePicker -> showImagePicker()
          AddMealViewModel.Event.ShowDatePicker -> showDatePicker()
        }
      })

    viewModel.isTitleValid
      .observe(viewLifecycleOwner, Observer { isValid ->
        binding.title.apply {
          if (!isValid) {
            error = getString(R.string.dialog_add_meal_empty_form_error, tag)
          } else {
            isErrorEnabled = false
          }
        }
      })

    viewModel.isDescriptionValid
      .observe(viewLifecycleOwner, Observer { isValid ->
        binding.description.apply {
          if (!isValid) {
            error = getString(R.string.dialog_add_meal_empty_form_error, tag)
          } else {
            isErrorEnabled = false
          }
        }
      })
  }

  override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
    viewModel.date.value = dateService.format(year, month + 1, dayOfMonth)
  }

  private fun showImagePicker() {
    imagePickerService.showImagePicker(isImageEmpty = viewModel.mainImage.value.isEmpty)
      .subscribeOnIoThread(onSuccess = { image -> viewModel.mainImage.postValue(image) })
      .store()
  }

  private fun showDatePicker() {
    val currentDate = dateService.currentDate

    DatePickerDialog(requireContext(), this, currentDate.year, currentDate.month, currentDate.day)
      .show()
  }

  private fun close() {
    navController.popBackStack()
  }
}