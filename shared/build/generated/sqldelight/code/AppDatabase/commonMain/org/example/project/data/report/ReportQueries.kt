package org.example.project.`data`.report

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String

public class ReportQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    id: String,
    userId: String,
    description: String,
    name: String,
    phone: String,
    imageUrl: String,
    isLost: Boolean,
    location: String?,
    lat: Double,
    lng: Double,
    createdAt: Long,
  ) -> T): Query<T> = Query(925_200_990, arrayOf("reports"), driver, "Report.sq", "selectAll", """
  |SELECT reports.id, reports.userId, reports.description, reports.name, reports.phone, reports.imageUrl, reports.isLost, reports.location, reports.lat, reports.lng, reports.createdAt
  |FROM reports
  |ORDER BY createdAt DESC
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getBoolean(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getLong(10)!!
    )
  }

  public fun selectAll(): Query<Reports> = selectAll { id, userId, description, name, phone,
      imageUrl, isLost, location, lat, lng, createdAt ->
    Reports(
      id,
      userId,
      description,
      name,
      phone,
      imageUrl,
      isLost,
      location,
      lat,
      lng,
      createdAt
    )
  }

  public fun <T : Any> selectByUser(userId: String, mapper: (
    id: String,
    userId: String,
    description: String,
    name: String,
    phone: String,
    imageUrl: String,
    isLost: Boolean,
    location: String?,
    lat: Double,
    lng: Double,
    createdAt: Long,
  ) -> T): Query<T> = SelectByUserQuery(userId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getBoolean(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getLong(10)!!
    )
  }

  public fun selectByUser(userId: String): Query<Reports> = selectByUser(userId) { id, userId_,
      description, name, phone, imageUrl, isLost, location, lat, lng, createdAt ->
    Reports(
      id,
      userId_,
      description,
      name,
      phone,
      imageUrl,
      isLost,
      location,
      lat,
      lng,
      createdAt
    )
  }

  public fun <T : Any> selectById(id: String, mapper: (
    id: String,
    userId: String,
    description: String,
    name: String,
    phone: String,
    imageUrl: String,
    isLost: Boolean,
    location: String?,
    lat: Double,
    lng: Double,
    createdAt: Long,
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getBoolean(6)!!,
      cursor.getString(7),
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getLong(10)!!
    )
  }

  public fun selectById(id: String): Query<Reports> = selectById(id) { id_, userId, description,
      name, phone, imageUrl, isLost, location, lat, lng, createdAt ->
    Reports(
      id_,
      userId,
      description,
      name,
      phone,
      imageUrl,
      isLost,
      location,
      lat,
      lng,
      createdAt
    )
  }

  public fun insertReport(
    id: String,
    userId: String,
    description: String,
    name: String,
    phone: String,
    imageUrl: String,
    isLost: Boolean,
    location: String?,
    lat: Double,
    lng: Double,
    createdAt: Long,
  ) {
    driver.execute(1_051_251_540, """
        |INSERT INTO reports(
        |  id, userId, description, name, phone, imageUrl, isLost, location, lat, lng, createdAt
        |)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          bindString(0, id)
          bindString(1, userId)
          bindString(2, description)
          bindString(3, name)
          bindString(4, phone)
          bindString(5, imageUrl)
          bindBoolean(6, isLost)
          bindString(7, location)
          bindDouble(8, lat)
          bindDouble(9, lng)
          bindLong(10, createdAt)
        }
    notifyQueries(1_051_251_540) { emit ->
      emit("reports")
    }
  }

  public fun upsertReport(
    id: String,
    userId: String,
    description: String,
    name: String,
    phone: String,
    imageUrl: String,
    isLost: Boolean,
    location: String?,
    lat: Double,
    lng: Double,
    createdAt: Long,
  ) {
    driver.execute(-993_658_550, """
        |INSERT OR REPLACE INTO reports(
        |  id, userId, description, name, phone, imageUrl, isLost, location, lat, lng, createdAt
        |)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          bindString(0, id)
          bindString(1, userId)
          bindString(2, description)
          bindString(3, name)
          bindString(4, phone)
          bindString(5, imageUrl)
          bindBoolean(6, isLost)
          bindString(7, location)
          bindDouble(8, lat)
          bindDouble(9, lng)
          bindLong(10, createdAt)
        }
    notifyQueries(-993_658_550) { emit ->
      emit("reports")
    }
  }

  public fun deleteReport(id: String) {
    driver.execute(-462_874_426, """DELETE FROM reports WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(-462_874_426) { emit ->
      emit("reports")
    }
  }

  private inner class SelectByUserQuery<out T : Any>(
    public val userId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("reports", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("reports", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_897_618_149, """
    |SELECT reports.id, reports.userId, reports.description, reports.name, reports.phone, reports.imageUrl, reports.isLost, reports.location, reports.lat, reports.lng, reports.createdAt
    |FROM reports
    |WHERE userId = ?
    |ORDER BY createdAt DESC
    """.trimMargin(), mapper, 1) {
      bindString(0, userId)
    }

    override fun toString(): String = "Report.sq:selectByUser"
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("reports", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("reports", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_383_499_083, """
    |SELECT reports.id, reports.userId, reports.description, reports.name, reports.phone, reports.imageUrl, reports.isLost, reports.location, reports.lat, reports.lng, reports.createdAt
    |FROM reports
    |WHERE id = ?
    """.trimMargin(), mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "Report.sq:selectById"
  }
}
