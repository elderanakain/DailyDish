package io.krugosvet.dailydish.android.network

import io.krugosvet.dailydish.android.db.objects.Meal
import io.krugosvet.dailydish.android.ibm.appId.AuthTokenManager
import io.krugosvet.dailydish.android.network.json.MealId
import io.krugosvet.dailydish.android.utils.applySchedulers
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.http.*

const val MEAL_ENDPOINT = "meal.php"
const val USER_ID_QUERY = "userId"
const val MEAL_ID_QUERY = "id"

interface MealService {

    @GET(MEAL_ENDPOINT)
    fun getMeals(@Query(USER_ID_QUERY) user: String): Maybe<List<Meal>>

    @GET(MEAL_ENDPOINT)
    fun getMeals(): Maybe<List<Meal>>

    @POST(MEAL_ENDPOINT)
    fun sendMeal(@Body meal: Meal): Single<MealId>

    @DELETE(MEAL_ENDPOINT)
    fun deleteMeal(@Query(MEAL_ID_QUERY) mealId: Int): Maybe<Void>
}

interface MealServicePipe {
    fun getMeals(): Maybe<List<Meal>>

    fun sendMeal(meal: Meal): Single<MealId>

    fun deleteMeal(meal: Meal): Maybe<Void>
}

class MealServicePipeImpl(private val mealService: MealService,
                          private val authTokenManager: AuthTokenManager) : MealServicePipe {

    override fun getMeals(): Maybe<List<Meal>> =
            getMealObserver(authTokenManager.userId()).applySchedulers()

    override fun sendMeal(meal: Meal): Single<MealId> = mealService.sendMeal(meal).applySchedulers()

    override fun deleteMeal(meal: Meal): Maybe<Void> =
            mealService.deleteMeal(meal.id).applySchedulers()

    private fun getMealObserver(userId: String) =
            if (userId.isEmpty()) mealService.getMeals() else mealService.getMeals(userId)
}
