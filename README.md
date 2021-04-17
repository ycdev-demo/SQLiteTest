# SQLiteTest

This project aims to validate the features of SQLite variants.

Currently, this project coverred the following SQLite variants:

* Android Framework SQLite
* SQLCipher (https://github.com/sqlcipher/android-database-sqlcipher)
* sqlite-android (https://github.com/requery/sqlite-android)
* WCDB (https://github.com/Tencent/wcdb)

We use instrumentation test cases to validate the features. Currently, the following features were converred:

* FTS4/FTS5


## Dataset for performance test

### amazon_reviews_phone.dat

We created the performance dataset "amazon_reviews_phone.dat" from "Amazon Review Data" at
https://nijianmo.github.io/amazon/index.html

We chose the "Cell Phones and Accessories" dataset to create "amazon_reviews_phone.dat".
There are 1,128,437 reviews in the dataset.
Download link: http://deepyeti.ucsd.edu/jianmo/amazon/categoryFilesSmall/Cell_Phones_and_Accessories_5.json.gz


## Performance

We use the following test rules:
* Run same test cases with the SQLite variants
* For each test, query 10 times to get the average time

### Pixel 4, Android 11

* Dataset: app/src/main/assets/amazon_reviews_phone.dat.gz
* Records: 1127672 records

#### Case 1

Key words: ```broken```

FTS4 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts4 on meta._id=index_fts4.rowid WHERE index_fts4 MATCH 'broken* ' ORDER BY _id ASC LIMIT 1000

FTS5 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts5 on meta._id=index_fts5.rowid WHERE index_fts5 MATCH 'broken* ' ORDER BY _id ASC LIMIT 1000

| SQLite variant     | FTS version | DB records | Query results | Time (ms) |
| :--                | :--         | :--        | :--           | :--       |
| Framework SQLite   | FTS4        | 1127672    | 1000          | 39        |
| Framework SQLite   | FTS5        | 1127672    | 1000          | N/A       |
| sqlite-android     | FTS4        | 1127672    | 1000          | 32        |
| sqlite-android     | FTS5        | 1127672    | 1000          | 31        |
| SQLCipher          | FTS4        | 1127672    | 1000          | 199       |
| SQLCipher          | FTS5        | 1127672    | 1000          | 202       |
| WCDB               | FTS4        | 1127672    | 1000          | 175       |
| WCDB               | FTS5        | 1127672    | 1000          | 174       |


#### Case 2

Key words: ```broken iphone```

FTS4 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts4 on meta._id=index_fts4.rowid WHERE index_fts4 MATCH 'broken* iphone* ' ORDER BY _id ASC LIMIT 1000

FTS5 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts5 on meta._id=index_fts5.rowid WHERE index_fts5 MATCH 'broken* iphone* ' ORDER BY _id ASC LIMIT 1000

| SQLite variant     | FTS version | DB records | Query results | Time (ms) |
| :--                | :--         | :--        | :--           | :--       |
| Framework SQLite   | FTS4        | 1127672    | 1000          | 53        |
| Framework SQLite   | FTS5        | 1127672    | 1000          | N/A       |
| sqlite-android     | FTS4        | 1127672    | 1000          | 42        |
| sqlite-android     | FTS5        | 1127672    | 1000          | 35        |
| SQLCipher          | FTS4        | 1127672    | 1000          | 138       |
| SQLCipher          | FTS5        | 1127672    | 1000          | 136       |
| WCDB               | FTS4        | 1127672    | 1000          | 74        |
| WCDB               | FTS5        | 1127672    | 1000          | 67        |


#### Case 3

Key words: ```broken sell```

FTS4 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts4 on meta._id=index_fts4.rowid WHERE index_fts4 MATCH 'broken* sell* ' ORDER BY _id ASC LIMIT 1000


FTS5 Query SQL:
> SELECT dataId,title,desc,author from meta INNER JOIN index_fts5 on meta._id=index_fts5.rowid WHERE index_fts5 MATCH 'broken* sell* ' ORDER BY _id ASC LIMIT 1000

| SQLite variant     | FTS version | DB records | Query results | Time (ms) |
| :--                | :--         | :--        | :--           | :--       |
| Framework SQLite   | FTS4        | 1127672    | 491           | 34        |
| Framework SQLite   | FTS5        | 1127672    | 491           | N/A       |
| sqlite-android     | FTS4        | 1127672    | 491           | 30        |
| sqlite-android     | FTS5        | 1127672    | 491           | 25        |
| SQLCipher          | FTS4        | 1127672    | 491           | 78       |
| SQLCipher          | FTS5        | 1127672    | 491           | 80       |
| WCDB               | FTS4        | 1127672    | 491           | 38        |
| WCDB               | FTS5        | 1127672    | 491           | 36        |
