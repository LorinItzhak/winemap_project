package org.example.project.`data`.report.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass
import org.example.project.`data`.report.AppDatabase
import org.example.project.`data`.report.ReportQueries

internal val KClass<AppDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = AppDatabaseImpl.Schema

internal fun KClass<AppDatabase>.newInstance(driver: SqlDriver): AppDatabase =
    AppDatabaseImpl(driver)

private class AppDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), AppDatabase {
  override val reportQueries: ReportQueries = ReportQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE reports (
          |  id         TEXT    NOT NULL PRIMARY KEY,
          |  userId     TEXT    NOT NULL,
          |  description TEXT   NOT NULL,
          |  name       TEXT    NOT NULL,
          |  phone      TEXT    NOT NULL,
          |  imageUrl   TEXT    NOT NULL,
          |  isLost     INTEGER NOT NULL,
          |  location   TEXT,             -- nullable
          |  lat        REAL    NOT NULL, -- Double; you can store Double.NaN if unknown
          |  lng        REAL    NOT NULL, -- Double; you can store Double.NaN if unknown
          |  createdAt  INTEGER NOT NULL  -- Long epoch millis
          |)
          """.trimMargin(), 0)
      driver.execute(null,
          "CREATE INDEX reports_user_created_idx ON reports(userId, createdAt DESC)", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
