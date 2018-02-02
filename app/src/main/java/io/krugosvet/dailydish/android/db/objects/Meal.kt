package io.krugosvet.dailydish.android.db.objects

import io.realm.Realm
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Meal: RealmModel {

    @PrimaryKey
    var id: String = ""
    var description: String = ""

    fun persist() {
        val realm = Realm.getDefaultInstance()
        realm.use {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(this)
            realm.commitTransaction()
        }
    }
}