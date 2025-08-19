// commonMain
package org.example.project.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.*
import org.example.project.data.report.ReportModel
import org.example.project.data.report.Location
import kotlinx.datetime.Clock

class RemoteFirebaseRepository : FirebaseRepository {

    override suspend fun signUp(email: String, password: String) {
        // יוצר חשבון חדש ב‑Firebase Auth
        Firebase.auth.createUserWithEmailAndPassword(email, password)
    }

    override suspend fun signIn(email: String, password: String) {
        // מתחבר חשבון קיים
        Firebase.auth.signInWithEmailAndPassword(email, password)
    }

    override fun currentUserUid(): String? =
        Firebase.auth.currentUser?.uid

    override suspend fun saveUserProfile(uid: String, email: String) {
        // שומר מסמך משתמש ב‑Firestore תחת collection "users"
        Firebase.firestore
            .collection("users")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "email" to email
                )
            )
    }

    override suspend fun signOut() {
        Firebase.auth.signOut()
    }

    override fun currentUserEmail(): String? =
        Firebase.auth.currentUser?.email

    override suspend fun updatePassword(newPassword: String) {
        Firebase.auth.currentUser
            ?.updatePassword(newPassword)
            ?: throw IllegalStateException("No signed-in user")
    }

    override suspend fun saveReport(
        userId: String,
        userName: String,
        wineryName: String,
        content: String,
        imageUrl: String,
        rating: Int,
        location: Location?
    ) {
        // בניית המידע לשמירה
        val reportData = mutableMapOf<String, Any>(
            "userId" to userId,
            "userName" to userName,
            "wineryName" to wineryName,
            "content" to content,
            "imageUrl" to imageUrl,
            "rating" to rating,
            "createdAt" to Clock.System.now().toEpochMilliseconds()
        )

        // הוספת מיקום אם קיים
        location?.let {
            reportData["locationName"] = it.name
            reportData["locationLat"] = it.lat
            reportData["locationLng"] = it.lng
        }

        Firebase.firestore
            .collection("posts")
            .add(reportData)
    }

    override suspend fun getReportsForUser(userId: String): List<ReportModel> {
        val snapshot = Firebase.firestore
            .collection("posts")
            .where { "userId" equalTo userId }
            .get()

        val results = mutableListOf<ReportModel>()
        for (doc in snapshot.documents) {
            try {
                val m = doc.data(ReportModel.serializer()).copy(id = doc.id)
                results += m
            } catch (e: Exception) {
                val raw = try { doc.data() as? Map<String, Any?> ?: emptyMap() } catch (_: Throwable) { emptyMap() }
                results += ReportModel(
                    id = doc.id,
                    userId = raw["userId"]?.toString().orEmpty(),
                    userName = raw["userName"]?.toString().orEmpty(),
                    wineryName = raw["wineryName"]?.toString().orEmpty(),
                    content = raw["content"]?.toString().orEmpty(),
                    imageUrl = raw["imageUrl"]?.toString().orEmpty(),
                    rating = (raw["rating"] as? Number)?.toInt() ?: 0,
                    createdAt = (raw["createdAt"] as? Number)?.toLong() ?: 0L,
                    location = createLocationFromRaw(raw)
                )
            }
        }

        // החדשים ראשונים
        return results.sortedByDescending { it.createdAt }
    }

    override suspend fun getAllReports(): List<ReportModel> {
        val snapshot = Firebase.firestore
            .collection("posts")
            .get()

        val results = mutableListOf<ReportModel>()
        for (doc in snapshot.documents) {
            try {
                val m = doc.data(ReportModel.serializer()).copy(id = doc.id)
                results += m
            } catch (_: Exception) {
                val raw = try { doc.data() as? Map<String, Any?> ?: emptyMap() } catch (_: Throwable) { emptyMap() }
                results += ReportModel(
                    id = doc.id,
                    userId = raw["userId"]?.toString().orEmpty(),
                    userName = raw["userName"]?.toString().orEmpty(),
                    wineryName = raw["wineryName"]?.toString().orEmpty(),
                    content = raw["content"]?.toString().orEmpty(),
                    imageUrl = raw["imageUrl"]?.toString().orEmpty(),
                    rating = (raw["rating"] as? Number)?.toInt() ?: 0,
                    createdAt = (raw["createdAt"] as? Number)?.toLong() ?: 0L,
                    location = createLocationFromRaw(raw)
                )
            }
        }
        return results.sortedByDescending { it.createdAt }
    }

    override suspend fun updateReport(
        reportId: String,
        userName: String?,
        wineryName: String?,
        content: String?,
        imageUrl: String?,
        rating: Int?,
        location: Location?
    ) {
        // בניית מפת עדכון חלקית (רק שדות שלא null יעודכנו)
        val data = mutableMapOf<String, Any>()
        userName?.let { data["userName"] = it }
        wineryName?.let { data["wineryName"] = it }
        content?.let { data["content"] = it }
        imageUrl?.let { data["imageUrl"] = it }
        rating?.let { data["rating"] = it }

        location?.let {
            data["locationName"] = it.name
            data["locationLat"] = it.lat
            data["locationLng"] = it.lng
        }

        if (data.isEmpty()) return // אין מה לעדכן

        Firebase.firestore
            .collection("posts")
            .document(reportId)
            .update(data)
    }

    override suspend fun deleteReport(reportId: String) {
        Firebase.firestore
            .collection("posts")
            .document(reportId)
            .delete()
    }

    private fun createLocationFromRaw(raw: Map<String, Any?>): Location? {
        val name = raw["locationName"]?.toString()
        val lat = anyToDouble(raw["locationLat"])
        val lng = anyToDouble(raw["locationLng"])

        return if (!name.isNullOrEmpty() && !lat.isNaN() && !lng.isNaN()) {
            Location(lat = lat, lng = lng, name = name)
        } else null
    }

    private fun anyToDouble(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: Double.NaN
        else -> Double.NaN
    }
}