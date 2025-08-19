import SwiftUI
import MapKit
import CoreLocation
import Shared

struct ReportDetailsView: View {
    let report: ReportModel
    var onEdit: () -> Void = {}
    var onDelete: () -> Void = {}

    @State private var current: ReportModel
    @State private var showEdit = false
    @State private var showDeleteConfirm = false

    // Address state
    @State private var addressText: String = ""
    @State private var isGeocoding = false
    @State private var savingError: String?

    init(report: ReportModel, onEdit: @escaping () -> Void = {}, onDelete: @escaping () -> Void = {}) {
        self.report = report
        self.onEdit  = onEdit
        self.onDelete = onDelete
        _current = State(initialValue: report)
    }

    var body: some View {
        ZStack {
            Color("BackgroundGray").ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {

                    // Wine Image
                    if !current.imageUrl.isEmpty, let url = URL(string: current.imageUrl) {
                        AsyncImage(url: url) { img in
                            img.resizable().scaledToFill()
                        } placeholder: {
                            Rectangle()
                                .fill(Color.gray.opacity(0.2))
                                .overlay(
                                    ProgressView()
                                        .scaleEffect(1.2)
                                )
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 250)
                        .clipped()
                        .cornerRadius(12)
                    } else {
                        Rectangle()
                        .fill(Color.gray.opacity(0.15))
                        .frame(maxWidth: .infinity)
                        .frame(height: 250)
                        .overlay(
                            VStack(spacing: 8) {
                                Image(systemName: "wineglass")
                                    .font(.system(size: 48))
                                    .foregroundColor(.gray)
                                Text("No image")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                        )
                        .cornerRadius(12)
                    }

                    // Winery Name (Main Title)
                    Text(current.wineryName.isEmpty ? "Unknown Winery" : current.wineryName)
                        .font(.custom("BalooBhaijaan2-Bold", size: 28))
                        .foregroundColor(Color("WineColor"))

                    // Rating Section
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Rating")
                            .font(.custom("BalooBhaijaan2-Bold", size: 20))
                            .foregroundColor(Color("WineColor"))

                        HStack(spacing: 4) {
                            ForEach(0..<5) { index in
                                Image(systemName: index < current.rating ? "star.fill" : "star")
                                    .font(.title3)
                                    .foregroundColor(index < current.rating ? Color("StarColor") : Color.gray.opacity(0.5))
                            }

                            Spacer()

                            Text("\(current.rating)/5 stars")
                                .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                .foregroundColor(.secondary)
                        }

                        Divider()
                    }

                    // Review Content
                    if !current.content.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Review")
                                .font(.custom("BalooBhaijaan2-Bold", size: 20))
                                .foregroundColor(Color("WineColor"))

                            Text(current.content)
                                .font(.custom("BalooBhaijaan2-Regular", size: 16))
                                .lineSpacing(4)
                                .frame(maxWidth: .infinity, alignment: .leading)

                            Divider()
                        }
                    }

                    // Reviewer Information
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Review Information")
                            .font(.custom("BalooBhaijaan2-Bold", size: 20))
                            .foregroundColor(Color("WineColor"))

                        if !current.userName.isEmpty {
                            LabeledInline(title: "Reviewed by:", value: current.userName)
                        }

                        LabeledInline(title: "Date:", value: formatDate(current.createdAt))

                        Divider()
                    }

                    // Location Section
                    if let location = current.location,
                       !location.lat.isNaN,
                       !location.lng.isNaN {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Winery Location")
                                .font(.custom("BalooBhaijaan2-Bold", size: 20))
                                .foregroundColor(Color("WineColor"))

                            HStack(alignment: .firstTextBaseline, spacing: 8) {
                                Image(systemName: "location")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(Color("WineColor"))

                                Text(location.name.isEmpty
                                         ? String(format: "Lat %.5f, Lng %.5f", location.lat, location.lng)
                                         : location.name)
                                    .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                    .foregroundColor(location.name.isEmpty ? .secondary : .primary)
                                    .lineLimit(nil)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }

                            // Map
                            Map(initialPosition: .region(region(for: location.lat, location.lng))) {
                                Annotation("", coordinate: CLLocationCoordinate2D(latitude: location.lat, longitude: location.lng)) {
                                    ZStack {
                                        Circle()
                                            .fill(Color("WineColor"))
                                            .frame(width: 32, height: 32)

                                        Image(systemName: "wineglass.fill")
                                            .font(.system(size: 16, weight: .bold))
                                            .foregroundColor(.white)
                                    }
                                    .shadow(color: Color.black.opacity(0.3), radius: 2, x: 0, y: 1)
                                }
                            }
                            .frame(height: 200)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color("WineColor").opacity(0.2), lineWidth: 1)
                            )

                            // Open in Maps button
                            Button {
                                openInMaps(location: location)
                            } label: {
                                HStack {
                                    Image(systemName: "map")
                                    Text("Open in Maps")
                                        .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                }
                                .foregroundColor(Color("WineColor"))
                                .padding(.vertical, 8)
                            }
                        }
                    } else {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Location")
                                .font(.custom("BalooBhaijaan2-Bold", size: 20))
                                .foregroundColor(Color("WineColor"))

                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.white)
                                .frame(height: 100)
                                .overlay(
                                    VStack(spacing: 8) {
                                        Image(systemName: "location.slash")
                                            .font(.title2)
                                            .foregroundColor(.secondary)
                                        Text("No location available")
                                            .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                            .foregroundColor(.secondary)
                                    }
                                )
                        }
                    }

                    Spacer().frame(height: 120)
                }
                .padding(.horizontal, 24)
                .padding(.top, 16)
            }

            // Bottom action buttons
            VStack(spacing: 12) {
                Spacer()

                Button {
                    showEdit = true
                    onEdit()
                } label: {
                    HStack {
                        Image(systemName: "pencil")
                        Text("Edit Review")
                            .font(.custom("BalooBhaijaan2-Bold", size: 16))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .background(Color("WineColor"))
                    .cornerRadius(12)
                }

                Button(role: .destructive) {
                    showDeleteConfirm = true
                } label: {
                    HStack {
                        Image(systemName: "trash")
                        Text("Delete Review")
                            .font(.custom("BalooBhaijaan2-Bold", size: 16))
                    }
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .foregroundColor(.red)
                    .background(Color.clear)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.red, lineWidth: 2)
                    )
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 16)
        }
        .navigationTitle("Wine Review")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Delete review?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) { onDelete() }
            Button("Cancel", role: .cancel) { }
        } message: {
            Text("This wine review will be permanently deleted.")
        }
        .alert("Save failed", isPresented: .constant(savingError != nil)) {
            Button("OK") { savingError = nil }
        } message: {
            Text(savingError ?? "")
        }
        .navigationDestination(isPresented: $showEdit) {
            EditReportView(report: current) { userName, wineryName, content, rating, location, imageUrl in
                current = ReportModel(
                    id: current.id,
                    userId: current.userId,
                    userName: userName,
                    wineryName: wineryName,
                    content: content,
                    imageUrl: imageUrl ?? current.imageUrl,
                    rating: rating,
                    createdAt: current.createdAt,
                    location: location
                )
                showEdit = false
            }
        }
    }

    private func region(for lat: Double, _ lng: Double) -> MKCoordinateRegion {
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: lat, longitude: lng),
            span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
        )
    }

    private func openInMaps(location: Location) {
        let coordinate = CLLocationCoordinate2D(latitude: location.lat, longitude: location.lng)
        let placemark = MKPlacemark(coordinate: coordinate)
        let mapItem = MKMapItem(placemark: placemark)
        mapItem.name = current.wineryName.isEmpty ? "Winery" : current.wineryName
        mapItem.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }

    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        formatter.dateStyle = .long
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

private struct LabeledInline: View {
    let title: String
    let value: String

    var body: some View {
        if !value.isEmpty {
            HStack(alignment: .top, spacing: 8) {
                Text(title)
                    .font(.custom("BalooBhaijaan2-Bold", size: 16))
                    .foregroundColor(Color("WineColor"))

                Text(value)
                    .font(.custom("BalooBhaijaan2-Medium", size: 16))
                    .foregroundColor(.primary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }
}

extension Notification.Name {
    static let reportsDidChange = Notification.Name("reportsDidChange")
}