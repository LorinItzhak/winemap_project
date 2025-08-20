import SwiftUI
import MapKit
import CoreLocation
import Shared
import PhotosUI

struct EditReportView: View {
  let report: ReportModel
  var onSave: (_ userName: String,
               _ wineryName: String,
               _ content: String,
               _ rating: Int32,
               _ location: Location?,
               _ imageUrl: String?) -> Void

  @State private var userName: String
  @State private var wineryName: String
  @State private var content: String
  @State private var rating: Int

  @State private var location: Location?
  @State private var addressText: String = ""
  @State private var isGeocoding = false
  @State private var showLocationPicker = false

  @State private var pickedItem: PhotosPickerItem?
  @State private var pickedImageData: Data?

  init(report: ReportModel, onSave: @escaping (_ userName: String, _ wineryName: String, _ content: String, _ rating: Int32, _ location: Location?, _ imageUrl: String?) -> Void) {
    self.report = report
    self.onSave = onSave
    _userName    = State(initialValue: report.userName)
    _wineryName  = State(initialValue: report.wineryName)
    _content     = State(initialValue: report.content)
    _rating      = State(initialValue: Int(report.rating))
    _location    = State(initialValue: report.location)
  }

  var body: some View {
    ZStack {
      Color("BackgroundGray").ignoresSafeArea()

      ScrollView {
        VStack(alignment: .leading, spacing: 16) {

          ZStack(alignment: .bottomTrailing) {
            Group {
              if let data = pickedImageData, let uiimg = UIImage(data: data) {
                Image(uiImage: uiimg).resizable().scaledToFill()
              } else if !report.imageUrl.isEmpty, let url = URL(string: report.imageUrl) {
                AsyncImage(url: url) { img in
                  img.resizable().scaledToFill()
                } placeholder: {
                  Color.gray.opacity(0.2)
                }
              } else {
                Color.gray.opacity(0.15)
              }
            }
            .frame(maxWidth: .infinity).frame(height: 220)
            .clipped().cornerRadius(12)

            .overlay(alignment: .bottomLeading) {
              if pickedImageData != nil {
                Button { pickedImageData = nil } label: {
                  Image(systemName:"xmark")
                      .foregroundColor(.white)
                      .frame(width:44,height:44)
                      .background(Color(red:0.545,green:0.0,blue:0.0))
                      .cornerRadius(8)
                }.padding(12)
              }
            }

            .overlay(alignment: .bottomTrailing) {
              PhotosPicker(selection:$pickedItem,matching:.images) {
                Image(systemName:"pencil")
                    .foregroundColor(.white)
                    .frame(width:44,height:44)
                    .background(Color(red:0.545,green:0.0,blue:0.0))
                    .cornerRadius(8)
              }
              .padding(12)
            }
          }
          .onChange(of:pickedItem){ newItem in
            Task { if let data = try? await newItem?.loadTransferable(type:Data.self){ pickedImageData = data } }
          }

          FloatingLabelTextField(text:$userName,label:"your name",placeholder:"Enter your name")
          FloatingLabelTextField(text:$wineryName,label:"winery name",placeholder:"Enter winery name")

          VStack(alignment:.leading, spacing:8){
            Text("Your Review")
                .font(.custom("BalooBhaijaan2-Medium",size:14)).foregroundColor(.secondary)
            TextEditor(text:$content)
                .frame(minHeight:100)
                .padding(12)
                .background(Color.white)
                .cornerRadius(8)
                .overlay(RoundedRectangle(cornerRadius:8).stroke(Color.gray.opacity(0.3),lineWidth:1))
          }

          VStack(alignment:.leading, spacing:12){
            Text("Rating")
                .font(.custom("BalooBhaijaan2-Bold", size:18))
                .foregroundColor(Color(red:0.545,green:0.0,blue:0.0))

            HStack(spacing:8){
              ForEach(1...5,id:\.self){ index in
                Button{ rating = index } label:{
                  Image(systemName:index <= rating ? "star.fill" : "star")
                      .foregroundColor(index <= rating ? Color(red:1,green:0.843,blue:0) : .gray)
                }.font(.title2)
              }
              Spacer()
              Text("\(rating)/5 stars")
                  .font(.custom("BalooBhaijaan2-Medium", size:16)).foregroundColor(.secondary)
            }
          }

          VStack(alignment:.leading, spacing:8){
            Text("Winery Location")
                .font(.custom("BalooBhaijaan2-Bold",size:18))
                .foregroundColor(Color(red:0.545,green:0.0,blue:0.0))

            HStack(alignment: .firstTextBaseline, spacing:10){
              if isGeocoding{
                Text("Resolving address…").foregroundColor(.secondary)
              } else if let loc = location, !loc.name.isEmpty{
                Text(loc.name)
              } else if let loc = location {
                Text(String(format:"Lat %.5f, Lng %.5f",loc.lat,loc.lng)).foregroundColor(.secondary)
              } else {
                Text("No location set").foregroundColor(.secondary)
              }

              Spacer(minLength:8)

              Button { showLocationPicker = true } label:{
                Text(location == nil ? "Add location":"Change location")
                    .font(.custom("BalooBhaijaan2-Bold",size:16))
                    .foregroundColor(Color(red:0.545,green:0.0,blue:0.0))
                    .padding(.horizontal,12).padding(.vertical,8)
                    .background(Color.white.opacity(0.7))
                    .overlay(RoundedRectangle(cornerRadius:8).stroke(Color(red:0.545,green:0.0,blue:0.0),lineWidth:1))
              }
            }
          }

          Spacer().frame(height:108)
        }
        .padding(.horizontal,24)
        .padding(.top,16)
      }

      VStack{
        Spacer()
        Button {
          Task{
            var finalUrl:String? = nil
            if let data = pickedImageData {
              finalUrl = try? await CloudinaryUploader.upload(imageData:data)
            }
            onSave(userName,wineryName,content,Int32(rating),location,finalUrl)
          }
        } label:{
          Text("Save Changes")
              .foregroundColor(.white)
              .font(.custom("BalooBhaijaan2-Bold", size:16))
              .frame(maxWidth:.infinity,minHeight:52)
              .background(Color(red:0.545,green:0.0,blue:0.0))  // בורדו
              .cornerRadius(14)
        }
        .padding(.horizontal,16)
        .padding(.bottom,16)
      }
    }
    .navigationTitle("Edit Wine Review")
    .navigationBarTitleDisplayMode(.inline)
    .sheet(isPresented:$showLocationPicker){
      LocationPickerView(
        initialCenter:(location?.lat ?? 0) != 0 ? CLLocationCoordinate2D(latitude:location!.lat,longitude:location!.lng) :
          CLLocationCoordinate2D(latitude:32.0853,longitude:34.7818)
      ){ lat,lng in
        Task{
          let addr = await reverseGeocode(lat:lat,lng:lng)
          location = Location(lat:lat,lng:lng,name:addr)
        }
      }
    }
  }

  @MainActor private func reverseGeocode(lat:Double,lng:Double)async->String{
    isGeocoding = true
    defer {isGeocoding=false}
    let g = CLGeocoder()
    do{
      let p = try await g.reverseGeocodeLocation(.init(latitude:lat,longitude:lng))
      if let pm = p.first {
        let arr = [pm.name,pm.thoroughfare,pm.subThoroughfare,pm.locality,pm.administrativeArea,pm.country]
        return arr.compactMap{ $0?.trimmingCharacters(in:.whitespacesAndNewlines)}.filter{!$0.isEmpty}.joined(separator:", ")
      }
    }catch{}
    return "Unknown location"
  }
}

private struct LocationPickerView: View {
  @Environment(\.dismiss) private var dismiss

  let initialCenter: CLLocationCoordinate2D
  let onPick: (_ lat: Double, _ lng: Double) -> Void

  @State private var cameraPosition: MapCameraPosition
  @State private var currentCenter: CLLocationCoordinate2D

  init(initialCenter:CLLocationCoordinate2D,onPick:@escaping(_ lat:Double,_ lng:Double)->Void){
    self.initialCenter = initialCenter
    self.onPick = onPick
    let region = MKCoordinateRegion(center:initialCenter, span: MKCoordinateSpan(latitudeDelta:0.01, longitudeDelta:0.01))
    _cameraPosition = State(initialValue:.region(region))
    _currentCenter = State(initialValue:initialCenter)
  }

  var body: some View {
    ZStack {
      Map(position:$cameraPosition)
      .mapControls{ MapUserLocationButton(); MapCompass() }
      .onMapCameraChange{ c in currentCenter = c.region.center }
      .ignoresSafeArea(edges:.bottom)

      Image(systemName:"mappin.circle.fill")
          .font(.system(size:28))
          .foregroundColor(Color(red:0.545,green:0.0,blue:0.0))
          .shadow(radius:2)

      VStack {
        Spacer()
        HStack{
          Button("Cancel"){ dismiss() }
          .frame(height:44).padding(.horizontal,16)
          .background(Color.gray.opacity(0.15)).cornerRadius(8)

          Spacer(minLength:12)

          Button {
            onPick(currentCenter.latitude,currentCenter.longitude)
            dismiss()
          } label:{
            Text("Use this location")
                .foregroundColor(.white)
                .frame(maxWidth:.infinity,minHeight:44)
                .background(Color(red:0.545,green:0.0,blue:0.0))  // בורדו
                .cornerRadius(8)
          }
        }
        .padding(.horizontal,16)
        .padding(.bottom,16)
      }
    }
    .presentationDetents([.medium,.large])
    .presentationDragIndicator(.visible)
  }
}

extension CloudinaryUploader {
  static func upload(imageData data:Data)async throws -> String{
    try await withCheckedThrowingContinuation{ cont in
      upload(data){ url in
        if let url { cont.resume(returning:url) }
        else { cont.resume(throwing:NSError(domain:"Cloudinary", code:-1)) }
      }
    }
  }
}
