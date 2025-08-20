import SwiftUI
import MapKit
import Shared

// צבעי תאימות בדיוק כמו באנדרואיד
private let WineColor   = Color(hex: "#8B0000") // בורדו
private let StarColor   = Color(hex: "#FFD700") // צבע כוכבים
private let BgColor     = Color.white          // רקע המסך

extension Color {
  init(hex: String) {
    var hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
    var int: UInt64 = 0
    Scanner(string: hex).scanHexInt64(&int)
    let a, r, g, b: UInt64
    switch hex.count {
    case 3:
      (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
    case 6:
      (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
    case 8:
      (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
    default:
      (a, r, g, b) = (255, 0, 0, 0)
    }
    self.init(.sRGB, red: Double(r)/255, green: Double(g)/255, blue: Double(b)/255, opacity: Double(a)/255)
  }
}

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
  @State private var showNewReport = false

  var body: some View {
    ZStack(alignment: .bottomTrailing) {
      BgColor.ignoresSafeArea()

      Map(position: $cameraPosition) {
        UserAnnotation()
        ForEach(reports, id: \.id) { rpt in
          if let loc = rpt.location, !loc.lat.isNaN, !loc.lng.isNaN {
            let coord = CLLocationCoordinate2D(latitude: loc.lat, longitude: loc.lng)
            Annotation("", coordinate: coord) {
              WinePin(report: rpt)
            }
          }
        }
      }
      .padding(.top, 32)
      .padding(.bottom, 88)

      // כפתור review בורדו בסגנון אנדרואיד
      Button(action: { showNewReport = true }) {
        HStack(spacing: 8) {
          Image(systemName: "plus")
              .font(.system(size: 16, weight: .bold))
          Text("Review")
              .font(.custom("BalooBhaijaan2-Bold", size: 16))
        }
        .foregroundColor(.white)
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(WineColor)
        .cornerRadius(20)
        .shadow(color: .black.opacity(0.25), radius: 6, x: 0, y: 3)
      }
      .padding(.trailing, 24)
      .padding(.bottom, 100)
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

  struct WinePin: View {
    let report: ReportModel

    var body: some View {
      VStack(spacing: 6) {
        ZStack {
          Circle()
              .fill(WineColor)
              .frame(width: 36, height: 36)
          Image(systemName: "wineglass.fill")
              .foregroundColor(.white)
              .font(.system(size: 14, weight: .bold))
        }
        .shadow(color: .black.opacity(0.3), radius: 2, y: 1)

        VStack(spacing: 2) {
          Text(report.wineryName.isEmpty ? "Unknown Winery" : report.wineryName)
              .font(.custom("BalooBhaijaan2-Bold", size: 12))
              .foregroundColor(WineColor)

          HStack(spacing: 2) {
            ForEach(0..<5) { idx in
              Image(systemName: idx < report.rating ? "star.fill" : "star")
                  .font(.system(size: 9))
                  .foregroundColor(idx < report.rating ? StarColor : .gray.opacity(0.4))
            }
          }
        }
        .padding(5)
        .background(Color.white.opacity(0.9))
        .cornerRadius(6)
      }
    }
  }

  private func reloadReports() {
    guard !isLoadingReports else { return }
    isLoadingReports = true
    reportsError = nil

    Shared.ReportRepositoryImpl().getAllReports { list, error in
      DispatchQueue.main.async {
        isLoadingReports = false
        if let error = error {
          reportsError = error.localizedDescription
          reports = []
        } else {
          reports = list ?? []
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
        defer { isLocating = false }
        if let error = error {
          locationError = error.localizedDescription
          return
        }
        guard let loc = location else {
          locationError = "Unknown location error"
          return
        }
        let coord = CLLocationCoordinate2D(latitude: loc.latitude, longitude: loc.longitude)
        userCoordinate = coord
        cameraPosition = .region(
          MKCoordinateRegion(center: coord, span: MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02))
        )
      }
    }
  }
}
