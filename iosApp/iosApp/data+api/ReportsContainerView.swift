import SwiftUI
import Shared

struct ReportsContainerView: View {
    // Instantiate with the new zero-arg init
    private let reportVm = ReportViewModel()
    private let auth = RemoteFirebaseRepository()

    var body: some View {
        NewReportView(
            onAddPhoto: { },
            onAddLocation: { },
            onPublish: { userName, wineryName, content, rating, imageUrl, location in
                // Get current user ID
                guard let currentUserId = auth.currentUserUid() else {
                    print("Error: No current user")
                    return
                }

                reportVm.saveReport(
                    userId: currentUserId,
                    userName: userName,
                    wineryName: wineryName,
                    content: content,
                    imageUrl: imageUrl,
                    rating: rating,
                    location: location
                )
            }
        )
    }
}