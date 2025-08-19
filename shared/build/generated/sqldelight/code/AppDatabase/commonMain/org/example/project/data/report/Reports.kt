package org.example.project.`data`.report

import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Reports(
  public val id: String,
  public val userId: String,
  public val description: String,
  public val name: String,
  public val phone: String,
  public val imageUrl: String,
  public val isLost: Boolean,
  public val location: String?,
  public val lat: Double,
  public val lng: Double,
  public val createdAt: Long,
)
