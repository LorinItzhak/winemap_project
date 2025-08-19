import SwiftUI
import MapKit
import Shared

struct FeedView: View {
    @EnvironmentObject private var session: SessionStore

    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 32.0853, longitude: 34.7818),
            span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
        )
    )
    @State private var userCoordinate: CLLocationCoordinate2D?
    @State private var isLocating = false
    @State private var locationError: String?

    @State private var reports: [ReportModel] = []
    @State private var isLoadingReports = false
    @State private var reportsError: String?

    @State private var selectedReport: ReportModel? = nil
    @State private var showNewReport = false

    var body: some View {
        ZStack {
            Color("BackgroundGray").ignoresSafeArea()

            VStack(spacing: 16) {
                Map(position: $cameraPosition) {
                    UserAnnotation()

                    ForEach(reports, id: \.id) { rpt in
                        // Use location from Location object if available
                        if let location = rpt.location,
                           !location.lat.isNaN,
                           !location.lng.isNaN {
                            let coord = CLLocationCoordinate2D(latitude: location.lat, longitude: location.lng)
                            Annotation("", coordinate: coord) {
                                VStack(spacing: 2) {
                                    NavigationLink {
                                        ReportDetailsView(report: rpt)
                                            .navigationTitle("Wine Review")
                                            .navigationBarTitleDisplayMode(.inline)
                                    } label: {
                                        // Wine-themed map pin
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

                                    // Show winery name and rating
                                    VStack(spacing: 1) {
                                        Text(rpt.wineryName.isEmpty ? "Unknown Winery" : rpt.wineryName)
                                            .font(.caption2)
                                            .fontWeight(.semibold)
                                            .lineLimit(1)
                                            .foregroundColor(Color("WineColor"))

                                        // Rating stars
                                        HStack(spacing: 1) {
                                            ForEach(0..<5) { index in
                                                Image(systemName: index < rpt.rating ? "star.fill" : "star")
                                                    .font(.system(size: 8))
                                                    .foregroundColor(index < rpt.rating ? Color("StarColor") : Color.gray)
                                            }
                                        }
                                    }
                                    .padding(.horizontal, 4)
                                    .padding(.vertical, 2)
                                    .background(Color.white.opacity(0.9))
                                    .cornerRadius(6)
                                }
                            }
                        }
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(maxHeight: .infinity)
                .padding(.top, 32)
                .padding(.bottom, 16)
                .overlay(alignment: .topTrailing) {
                    HStack(spacing: 8) {
                        if isLoadingReports {
                            ProgressView()
                                .scaleEffect(0.8)
                                .padding(8)
                        }
                        Button(action: reloadReports) {
                            Image(systemName: "arrow.clockwise")
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color("WineColor").opacity(0.85))
                                .clipShape(Circle())
                        }
                        Button(action: locateMe) {
                            Image(systemName: "location.fill")
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color("WineColor"))
                                .clipShape(Circle())
                        }
                    }
                    .padding(16)
                }

                HStack {
                    Spacer()
                    Button {
                        showNewReport = true
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "plus")
                                .font(.system(size: 16, weight: .bold))
                            Text("Add Wine Review")
                                .font(.custom("BalooBhaijaan2-Bold", size: 14))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(Color("WineColor"))
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.2),
                                radius: 4, x: 0, y: 2)
                    }
                }

                // Status messages
                if isLocating {
                    HStack {
                        ProgressView()
                            .scaleEffect(0.8)
                        Text("Getting your location…")
                            .font(.custom("BalooBhaijaan2-Medium", size: 14))
                            .foregroundColor(.secondary)
                    }
                    .padding(.vertical, 4)
                }

                if let err = locationError {
                    Text("Location: \(err)")
                        .font(.footnote)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }

                if let rerr = reportsError {
                    Text("Reviews: \(rerr)")
                        .font(.footnote)
                        .foregroundColor(.red)
                        .padding(.horizontal)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 16)
        }
        .onAppear {
            session.currentTitle = "Wine Reviews"
        }
        .task {
            if reports.isEmpty { reloadReports() }
            if userCoordinate == nil { locateMe() }
        }
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showNewReport) {
            ReportsContainerView()
        }
    }

    private func reloadReports() {
        guard !isLoadingReports else { return }
        isLoadingReports = true
        reportsError = nil

        Shared.ReportRepositoryImpl().getAllReports { list, error in
            DispatchQueue.main.async {
                self.isLoadingReports = false
                if let error = error {
                    self.reportsError = error.localizedDescription
                    self.reports = []
                    return
                }
                if let arr = list {
                    self.reports = arr
                } else {
                    self.reports = []
                }
            }
        }
    }

    private func locateMe() {
        guard !isLocating else { return }
        isLocating = true
        locationError = nil

        Shared.LocationApi().get { location, error in
            DispatchQueue.main.async {
                defer { self.isLocating = false }
                if let error = error {
                    self.locationError = error.localizedDescription
                    return
                }
                guard let loc = location else {
                    self.locationError = "Unknown location error"
                    return
                }
                let coord = CLLocationCoordinate2D(latitude: loc.latitude, longitude: loc.longitude)
                self.userCoordinate = coord
                self.cameraPosition = .region(
                    MKCoordinateRegion(
                        center: coord,
                        span: MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02)
                    )
                )
            }
        }
    }
}