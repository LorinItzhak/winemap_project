import SwiftUI
import MapKit
import CoreLocation
import Shared

struct ReportDetailsView: View {
  let report: ReportModel
  var onDelete: () -> Void = {}

  @State private var current: ReportModel
  @State private var showEditSheet = false
  @State private var showDeleteConfirm = false
  @State private var showLocationPicker = false

  private let mainColor = Color(red: 0.545, green: 0.0, blue: 0.0)
  private let starColor = Color(red: 1.0, green: 0.843, blue: 0.0)

  init(report: ReportModel, onDelete: @escaping () -> Void = {}) {
    self.report = report
    self.onDelete = onDelete
    _current = State(initialValue: report)
  }

  var body: some View {
    ZStack {
      Color("BackgroundGray").ignoresSafeArea()

      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          // Image
          if !current.imageUrl.isEmpty, let url = URL(string: current.imageUrl) {
            AsyncImage(url: url) { img in
              img.resizable().scaledToFill()
            } placeholder: {
              Rectangle().fill(Color.gray.opacity(0.2)).overlay(ProgressView().scaleEffect(1.2))
            }
            .frame(height: 250)
            .clipped()
            .cornerRadius(12)
          } else {
            Rectangle()
            .fill(Color.gray.opacity(0.15))
            .frame(height: 250)
            .overlay(VStack(spacing: 8) {
              Image(systemName: "wineglass")
                  .font(.system(size: 48))
                  .foregroundColor(.gray)
              Text("No image")
                  .font(.caption)
                  .foregroundColor(.gray)
            })
            .cornerRadius(12)
          }

          // Winery
          Text(current.wineryName.isEmpty ? "Unknown Winery" : current.wineryName)
              .font(.custom("BalooBhaijaan2-Bold", size: 28))
              .foregroundColor(mainColor)

          // Rating
          VStack(alignment: .leading, spacing: 12) {
            Text("Rating")
                .font(.custom("BalooBhaijaan2-Bold", size: 20))
                .foregroundColor(mainColor)
            HStack(spacing: 4) {
              ForEach(0..<5) { i in
                Image(systemName: i < current.rating ? "star.fill" : "star")
                    .foregroundColor(i < current.rating ? starColor : Color.gray.opacity(0.5))
                    .font(.title3)
              }
              Spacer()
              Text("\(current.rating)/5 stars")
                  .font(.custom("BalooBhaijaan2-Medium", size: 16))
                  .foregroundColor(.secondary)
            }
            Divider()
          }

          // Review text
          if !current.content.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
              Text("Review")
                  .font(.custom("BalooBhaijaan2-Bold", size: 20))
                  .foregroundColor(mainColor)
              Text(current.content)
                  .font(.custom("BalooBhaijaan2-Regular", size: 16))
              Divider()
            }
          }

          // Info
          VStack(alignment: .leading, spacing: 12) {
            Text("Review Information")
                .font(.custom("BalooBhaijaan2-Bold", size: 20))
                .foregroundColor(mainColor)

            if !current.userName.isEmpty {
              HStack {
                Text("Reviewed by:")
                    .font(.custom("BalooBhaijaan2-Bold", size: 16))
                    .foregroundColor(mainColor)
                Text(current.userName)
                    .font(.custom("BalooBhaijaan2-Medium", size: 16))
              }
            }
            HStack {
              Text("Date:")
                  .font(.custom("BalooBhaijaan2-Bold", size: 16))
                  .foregroundColor(mainColor)
              Text(formatDate(current.createdAt))
                  .font(.custom("BalooBhaijaan2-Medium", size: 16))
            }
            Divider()
          }

          // Location
          if let loc = current.location, !loc.lat.isNaN, !loc.lng.isNaN {
            VStack(alignment: .leading, spacing: 12) {
              Text("Winery Location")
                  .font(.custom("BalooBhaijaan2-Bold", size: 20))
                  .foregroundColor(mainColor)

              HStack {
                Image(systemName: "location").foregroundColor(mainColor)
                Text(loc.name.isEmpty ? String(format: "Lat %.5f, Lng %.5f", loc.lat, loc.lng) : loc.name)
                    .font(.custom("BalooBhaijaan2-Medium", size: 16))
              }

              Map(initialPosition: .region(region(for: loc.lat, loc.lng))) {
                Annotation("", coordinate: CLLocationCoordinate2D(latitude: loc.lat, longitude: loc.lng)) {
                  ZStack {
                    Circle().fill(mainColor).frame(width: 32, height: 32)
                    Image(systemName: "wineglass.fill")
                        .foregroundColor(.white).font(.system(size: 16, weight: .bold))
                  }
                }
              }
              .frame(height:200)
              .cornerRadius(12)
              .overlay(RoundedRectangle(cornerRadius:12).stroke(mainColor.opacity(0.2), lineWidth:1))
            }
          } else {
            VStack(alignment: .leading, spacing: 12) {
              Text("Location")
                  .font(.custom("BalooBhaijaan2-Bold", size: 20))
                  .foregroundColor(mainColor)

              HStack {
                Text("No location selected")
                    .font(.custom("BalooBhaijaan2-Medium", size: 16))
                Spacer()
                Button {
                  showLocationPicker = true
                } label: {
                  Text("Pick location")
                      .font(.custom("BalooBhaijaan2-Bold", size: 16))
                      .foregroundColor(mainColor)
                      .padding(.horizontal,16)
                      .padding(.vertical,8)
                      .overlay(RoundedRectangle(cornerRadius:12).stroke(mainColor, lineWidth:2))
                }
              }
              .padding()
              .background(Color.white)
              .cornerRadius(12)
            }
          }

          Spacer().frame(height:120)
        }
        .padding(.horizontal,24)
        .padding(.top,16)
      }

      // Bottom buttons
      VStack(spacing:12){
        Spacer()
        Button {
          showEditSheet = true
        } label: {
          HStack { Image(systemName:"pencil"); Text("Edit Review").font(.custom("BalooBhaijaan2-Bold", size: 16)) }
          .foregroundColor(.white)
          .frame(maxWidth:.infinity,minHeight:52)
          .background(mainColor)
          .cornerRadius(12)
        }
        Button {
          showDeleteConfirm = true
        } label: {
          HStack { Image(systemName:"trash"); Text("Delete Review").font(.custom("BalooBhaijaan2-Bold", size: 16)) }
          .foregroundColor(.white)
          .frame(maxWidth:.infinity,minHeight:52)
          .background(mainColor)
          .cornerRadius(12)
        }
      }
      .padding(.horizontal,24)
      .padding(.bottom,16)
    }
    .navigationTitle("Wine Review")
    .navigationBarTitleDisplayMode(.inline)

    // --------------- Edit sheet ---------------
    .sheet(isPresented: $showEditSheet) {
      EditReportView(report: current) { u,n,c,r,l,img in
        current = ReportModel(id: current.id, userId: current.userId, userName: u, wineryName: n, content: c, imageUrl: img ?? current.imageUrl, rating: r, createdAt: current.createdAt, location: l )
        showEditSheet = false
      }
    }

    // --------------- Location Picker ---------------
    .sheet(isPresented: $showLocationPicker) {
      LocationPickerView(
        initialCenter: current.location != nil ?
          CLLocationCoordinate2D(latitude: current.location!.lat, longitude: current.location!.lng) :
          CLLocationCoordinate2D(latitude: 32.0853, longitude: 34.7818)
      ) { lat, lng in
        current = ReportModel(
          id: current.id, userId: current.userId, userName: current.userName,
          wineryName: current.wineryName, content: current.content, imageUrl: current.imageUrl,
          rating: current.rating, createdAt: current.createdAt,
          location: Location(lat: lat, lng: lng, name: "")
        )
      }
    }
    .alert("Delete review?", isPresented: $showDeleteConfirm) {
      Button("Delete", role: .destructive) { onDelete() }
      Button("Cancel", role: .cancel) { }
    }
  }

  private func region(for lat:Double,_ lng:Double)->MKCoordinateRegion{
    MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: lat, longitude: lng), span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
  }

  private func formatDate(_ timestamp:Int64)->String{
    let date = Date(timeIntervalSince1970: TimeInterval(timestamp/1000))
    let formatter=DateFormatter()
    formatter.dateStyle = .long
    formatter.timeStyle = .short
    return formatter.string(from: date)
  }
}

// --------------------------------------------------
// LocationPickerView נקודתי (כולל בקובץ אחד לעבודה נוחה)
// --------------------------------------------------
private struct LocationPickerView: View {
  @Environment(\.dismiss) private var dismiss

  let initialCenter: CLLocationCoordinate2D
  let onPick: (_ lat: Double, _ lng: Double) -> Void

  @State private var cameraPosition: MapCameraPosition
  @State private var currentCenter: CLLocationCoordinate2D

  init(initialCenter: CLLocationCoordinate2D, onPick: @escaping (_ lat: Double, _ lng: Double) -> Void) {
    self.initialCenter = initialCenter
    self.onPick = onPick
    let region = MKCoordinateRegion(center: initialCenter, span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01))
    _cameraPosition = State(initialValue: .region(region))
    _currentCenter  = State(initialValue: initialCenter)
  }

  var body: some View {
    NavigationView {
      ZStack {
        Map(position: $cameraPosition)
        .mapControls { MapUserLocationButton(); MapCompass() }
        .onMapCameraChange { ctx in currentCenter = ctx.region.center }
        .ignoresSafeArea(edges: .bottom)

        Image(systemName: "mappin.circle.fill")
            .font(.system(size: 28))
            .foregroundColor(.red)
            .shadow(radius: 2)

        VStack {
          Spacer()
          VStack(spacing: 16) {
            Text("Select Winery Location")
                .font(.custom("BalooBhaijaan2-Bold", size: 18))
            Text(String(format: "Lat %.5f   Lng %.5f", currentCenter.latitude, currentCenter.longitude))
                .font(.footnote)
                .foregroundColor(.secondary)
            HStack {
              Button("Cancel") { dismiss() }
              .frame(height: 44)
              .padding(.horizontal, 16)
              .background(Color.gray.opacity(0.15))
              .cornerRadius(8)
              Spacer(minLength: 12)
              Button {
                onPick(currentCenter.latitude, currentCenter.longitude)
                dismiss()
              } label: {
                Text("Use this location")
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .background(Color(red: 0.545, green: 0.0, blue: 0.0))
                    .cornerRadius(8)
              }
            }
          }
          .padding(20)
          .background(Color.white)
          .cornerRadius(16)
          .shadow(radius: 10)
          .padding(.horizontal, 16)
          .padding(.bottom, 16)
        }
      }
      .navigationBarTitleDisplayMode(.inline)
    }
    .presentationDetents([.medium,.large])
    .presentationDragIndicator(.visible)
  }
}
