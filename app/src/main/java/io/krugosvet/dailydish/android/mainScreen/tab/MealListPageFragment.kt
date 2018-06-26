package io.krugosvet.dailydish.android.mainScreen.tab

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.krugosvet.dailydish.android.DailyDishApplication
import io.krugosvet.dailydish.android.R
import io.krugosvet.dailydish.android.db.objects.meal.Meal
import io.krugosvet.dailydish.android.mainScreen.MealListAdapter
import io.krugosvet.dailydish.android.mainScreen.MealListAdapterPipe
import io.krugosvet.dailydish.android.network.BaseNetworkObserver
import io.krugosvet.dailydish.android.network.json.UpdateDateMeal
import io.krugosvet.dailydish.android.network.json.UpdateImageMeal
import io.krugosvet.dailydish.android.utils.ViewPagerFragment
import io.krugosvet.dailydish.android.utils.baseUi.BaseActivity
import io.krugosvet.dailydish.android.utils.baseUi.BaseFragment
import io.krugosvet.dailydish.android.utils.baseUi.showLongSnackbar
import io.krugosvet.dailydish.android.utils.getCurrentDate
import io.krugosvet.dailydish.android.utils.intent.ImageProviderActivity
import io.realm.RealmQuery
import kotlinx.android.synthetic.main.fragment_meal_list.*
import kotlinx.android.synthetic.main.layout_meal_empty.*

const val PAGE_TITLE = "pageTitle"

abstract class MealListPageFragment : BaseFragment(), ViewPagerFragment, MealListAdapterPipe {

    protected lateinit var adapter: MealListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_meal_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MealListAdapter(activity as ImageProviderActivity, getMealListQuery(), this)
        mealList.adapter = adapter
        emptyLayoutText.text = getString(getEmptyLayoutText())
    }

    override fun getFragmentTitle() = arguments?.getString(PAGE_TITLE) ?: ""

    override fun initInjection() {
        DailyDishApplication.appComponent.inject(this)
    }

    override fun deleteMeal(meal: Meal) {
        mealServicePipe.deleteMeal(meal).subscribe(
                object : BaseNetworkObserver<Void>(activity as BaseActivity) {
                    override val onErrorMessage = R.string.network_delete_meal_error
                    override val onSuccessMessage = R.string.network_delete_meal_success

                    override fun onComplete() {
                        super.onComplete()
                        meal.delete(realm)
                    }

                })
    }

    override fun onMealListChange(isEmpty: Boolean) {
        if (isAdded) {
            mealList.visibility = if (isEmpty) View.GONE else View.VISIBLE
            emptyLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun changeMealMainImage(meal: Meal, image: Uri) {
        mealServicePipe.updateImageMeal(
                UpdateImageMeal(meal.id, image.toString(), authTokenManager.userId())).subscribe(
                object : BaseNetworkObserver<Void>((activity as? Activity)) {
                    override val onErrorMessage = R.string.network_put_meal_error
                    override val onSuccessMessage = R.string.network_put_meal_success

                    override fun onComplete() {
                        super.onComplete()
                        meal.changeMainImage(realm, image)
                    }
                })
    }

    override fun removeMealMainImage(meal: Meal) {
        mealServicePipe.updateImageMeal(
                UpdateImageMeal(meal.id, "", authTokenManager.userId())).subscribe(
                object : BaseNetworkObserver<Void>(activity as BaseActivity) {
                    override val onErrorMessage = R.string.network_put_meal_error
                    override val onSuccessMessage = R.string.network_put_meal_success

                    override fun onComplete() {
                        super.onComplete()
                        meal.removeMainImage(realm)
                    }
                })
    }

    override fun showLongSnackbar(message: Int) {
        showLongSnackbar(activity as BaseActivity, message)
    }

    override fun changeMealCookedDate(meal: Meal) {
        mealServicePipe.updateDateMeal(
                UpdateDateMeal(meal.id, getCurrentDate(), authTokenManager.userId())).subscribe(
                object : BaseNetworkObserver<Void>(activity as BaseActivity) {
                    override val onErrorMessage = R.string.network_put_meal_error
                    override val onSuccessMessage = R.string.network_put_meal_success

                    override fun onComplete() {
                        super.onComplete()
                        meal.updateDateToCurrent(realm)
                    }
                })
    }

    protected abstract fun getMealListQuery(): () -> RealmQuery<Meal>

    @StringRes
    protected abstract fun getEmptyLayoutText(): Int
}
