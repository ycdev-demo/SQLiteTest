package me.ycdev.android.demo.sqlite.case

import me.ycdev.android.demo.sqlite.db.BooksTableDao

fun BooksTableDao.execute(case: SearchCase) {
    case.execute(this)
}
