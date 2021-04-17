package me.ycdev.android.demo.sqlite.model

import me.ycdev.android.demo.sqlite.db.FtsTableDao

fun FtsTableDao.execute(case: SearchCase) {
    case.execute(this)
}
