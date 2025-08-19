import SwiftUI
import MapKit
import Shared

struct NewReportView: View {
    @Environment(\.dismiss) private var dismiss

    @State private var userName: String = ""
    @State private var wineryName: String = ""
    @State private var content: String = ""
    @State private var rating: Int = 0
    @State private var selectedImage: UIImage? = nil
    @State private var showPhotoOptions: Bool = false
    @State private var showImagePicker: Bool = false
    @State private var isUploading = false
    @State private var uploadedUrl: String? = nil
    @State private var imagePickerSource: UIImagePickerController.SourceType = .photoLibrary

    // Location
    @State private var pickedLocation: Location? = nil
    @State private var locationError: String? = nil
    @State private var showLocationPicker: Bool = false

    var onAddPhoto: () -> Void = {}
    var onAddLocation: () -> Void = {}

    var onPublish: (
        _ userName: String,
        _ wineryName: String,
        _ content: String,
        _ rating: Int32,
        _ imageUrl: String,
        _ location: Location?
    ) -> Void

    var body: some View {
        NavigationView {
            ZStack {
                Color("BackgroundGray").ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        // Title
                        Text("New Wine Review")
                            .font(.custom("BalooBhaijaan2-Bold", size: 28))
                            .foregroundColor(Color("WineColor"))
                            .padding(.top, 16)

                        // Wine Photo Section
                        ZStack(alignment: .center) {
                            if let uiImage = selectedImage {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(maxWidth: .infinity, maxHeight: 200)
                                    .clipped()
                                    .cornerRadius(12)
                            } else {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.white)
                                    .frame(maxWidth: .infinity, maxHeight: 200)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12)
                                            .stroke(Color("WineColor").opacity(0.3), lineWidth: 2)
                                    )
                                    .overlay(
                                        VStack(spacing: 12) {
                                            Image(systemName: "camera")
                                                .font(.system(size: 48))
                                                .foregroundColor(.gray)
                                            Text("Add wine photo")
                                                .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                                .foregroundColor(.gray)
                                        }
                                    )
                            }

                            // Add photo button
                            if selectedImage == nil {
                                Button {
                                    showPhotoOptions = true
                                } label: {
                                    Image(systemName: "plus.circle.fill")
                                        .font(.system(size: 32))
                                        .foregroundColor(Color("WineColor"))
                                        .background(Color.white)
                                        .clipShape(Circle())
                                }
                            } else {
                                VStack {
                                    HStack {
                                        Spacer()
                                        Button {
                                            showPhotoOptions = true
                                        } label: {
                                            Image(systemName: "pencil.circle.fill")
                                                .font(.system(size: 32))
                                                .foregroundColor(Color("WineColor"))
                                                .background(Color.white)
                                                .clipShape(Circle())
                                        }
                                        .padding(.trailing, 12)
                                        .padding(.top, 12)
                                    }
                                    Spacer()
                                }
                            }
                        }
                        .confirmationDialog("Select image source",
                                            isPresented: $showPhotoOptions,
                                            titleVisibility: .visible) {
                            Button("Photos") {
                                imagePickerSource = .photoLibrary
                                showImagePicker = true
                            }
                            if UIImagePickerController.isSourceTypeAvailable(.camera) {
                                Button("Camera") {
                                    imagePickerSource = .camera
                                    showImagePicker = true
                                }
                            }
                            Button("Cancel", role: .cancel) { }
                        }
                        .sheet(isPresented: $showImagePicker) {
                            ImagePicker(sourceType: imagePickerSource, selectedImage: $selectedImage)
                        }

                        VStack(spacing: 16) {
                            // Your name field
                            FloatingLabelTextField(
                                text: $userName,
                                label: "your name",
                                placeholder: "Enter your name"
                            )

                            // Winery name field
                            FloatingLabelTextField(
                                text: $wineryName,
                                label: "winery name",
                                placeholder: "Name of the winery"
                            )

                            // Review content field
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Your Review")
                                    .font(.custom("BalooBhaijaan2-Medium", size: 14))
                                    .foregroundColor(.secondary)

                                ZStack(alignment: .topLeading) {
                                    TextEditor(text: $content)
                                        .padding(12)
                                        .scrollContentBackground(.hidden)
                                        .background(Color.white)
                                        .cornerRadius(8)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 8)
                                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                        )
                                        .frame(height: 100)

                                    if content.isEmpty {
                                        Text("Share your wine tasting experience...")
                                            .foregroundColor(.gray)
                                            .padding(.top, 12)
                                            .padding(.leading, 16)
                                            .allowsHitTesting(false)
                                    }
                                }
                            }

                            // Rating Section
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Rate this wine")
                                    .font(.custom("BalooBhaijaan2-Bold", size: 18))
                                    .foregroundColor(Color("WineColor"))

                                HStack(spacing: 8) {
                                    ForEach(1...5, id: \.self) { index in
                                        Button {
                                            rating = index
                                        } label: {
                                            Image(systemName: index <= rating ? "star.fill" : "star")
                                                .font(.title2)
                                                .foregroundColor(index <= rating ? Color("StarColor") : Color.gray)
                                        }
                                    }

                                    Spacer()

                                    if rating > 0 {
                                        Text("\(rating)/5 stars")
                                            .font(.custom("BalooBhaijaan2-Medium", size: 16))
                                            .foregroundColor(.secondary)
                                    }
                                }
                            }

                            // Location Section
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Winery Location")
                                    .font(.custom("BalooBhaijaan2-Bold", size: 18))
                                    .foregroundColor(Color("WineColor"))

                                Button {
                                    onAddLocation()
                                    showLocationPicker = true
                                } label: {
                                    HStack {
                                        Image(systemName: "location")
                                            .foregroundColor(Color("WineColor"))
                                        Text(pickedLocation == nil ? "Add winery location" : "Change location")
                                            .font(.custom("BalooBhaijaan2-Bold", size: 16))
                                            .foregroundColor(Color("WineColor"))
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .font(.caption)
                                            .foregroundColor(Color("WineColor"))
                                    }
                                    .padding()
                                    .background(Color.white.opacity(0.7))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(Color("WineColor"), lineWidth: 1)
                                    )
                                    .cornerRadius(8)
                                }
                                .sheet(isPresented: $showLocationPicker) {
                                    LocationPickerView(
                                        initialCenter: pickedLocation?.lat != nil && pickedLocation?.lng != nil
                                            ? CLLocationCoordinate2D(latitude: pickedLocation!.lat, longitude: pickedLocation!.lng)
                                            : CLLocationCoordinate2D(latitude: 32.0853, longitude: 34.7818)
                                    ) { lat, lng in
                                        Task {
                                            let address = await reverseGeocode(lat: lat, lng: lng)
                                            pickedLocation = Location(lat: lat, lng: lng, name: address)
                                            locationError = nil
                                        }
                                    }
                                }

                                // Show selected location
                                if let location = pickedLocation {
                                    HStack {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(.green)
                                        Text(location.name.isEmpty ?
                                                 String(format: "%.5f, %.5f", location.lat, location.lng) :
                                                 location.name)
                                            .font(.custom("BalooBhaijaan2-Medium", size: 14))
                                            .foregroundColor(.secondary)
                                    }
                                    .padding(.leading, 8)
                                }

                                if let locationError {
                                    Text(locationError)
                                        .foregroundColor(.red)
                                        .font(.footnote)
                                }
                            }
                        }

                        // Publish Button
                        Button {
                            guard let uiImage = selectedImage,
                                  let jpegData = uiImage.jpegData(compressionQuality: 0.8) else {
                                return
                            }

                            isUploading = true
                            CloudinaryUploader.upload(jpegData) { url in
                                DispatchQueue.main.async {
                                    isUploading = false
                                    if let imageUrl = url {
                                        onPublish(userName, wineryName, content, Int32(rating), imageUrl, pickedLocation)
                                        dismiss()
                                    }
                                }
                            }
                        } label: {
                            HStack {
                                if isUploading {
                                    ProgressView()
                                        .progressViewStyle(.circular)
                                        .scaleEffect(0.8)
                                } else {
                                    Image(systemName: "paperplane.fill")
                                }
                                Text(isUploading ? "Publishing..." : "Publish Wine Review")
                                    .font(.custom("BalooBhaijaan2-Bold", size: 16))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity, minHeight: 52)
                            .background(
                                isFormValid() && !isUploading ?
                                    Color("WineColor") :
                                    Color.gray.opacity(0.5)
                            )
                            .cornerRadius(12)
                        }
                        .disabled(!isFormValid() || isUploading)

                        Spacer(minLength: 20)
                    }
                    .padding(.horizontal, 24)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .foregroundColor(Color("WineColor"))
                }
            }
        }
    }

    private func isFormValid() -> Bool {
        return !userName.isEmpty &&
            !wineryName.isEmpty &&
            !content.isEmpty &&
            rating > 0 &&
            selectedImage != nil
    }

    @MainActor
    private func reverseGeocode(lat: Double, lng: Double) async -> String {
        let geocoder = CLGeocoder()
        do {
            let placemarks = try await geocoder.reverseGeocodeLocation(.init(latitude: lat, longitude: lng))
            if let pm = placemarks.first {
                let parts = [pm.name, pm.thoroughfare, pm.subThoroughfare, pm.locality, pm.administrativeArea, pm.country]
                return parts.compactMap { $0?.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }
                .joined(separator: ", ")
            }
        } catch {
            print("Geocoding error: \(error)")
        }
        return "Unknown location"
    }
}

private struct LocationPickerView: View {
    @Environment(\.dismiss) private var dismiss

    let initialCenter: CLLocationCoordinate2D
    let onPick: (_ lat: Double, _ lng: Double) -> Void

    @State private var cameraPosition: MapCameraPosition
    @State private var currentCenter: CLLocationCoordinate2D

    init(initialCenter: CLLocationCoordinate2D,
         onPick: @escaping (_ lat: Double, _ lng: Double) -> Void) {
        self.initialCenter = initialCenter
        self.onPick = onPick
        let region = MKCoordinateRegion(
            center: initialCenter,
            span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
        )
        _cameraPosition = State(initialValue: .region(region))
        _currentCenter  = State(initialValue: initialCenter)
    }

    var body: some View {
        NavigationView {
            ZStack {
                Map(position: $cameraPosition)
                .mapControls {
                    MapUserLocationButton()
                    MapCompass()
                }
                .onMapCameraChange { ctx in
                    currentCenter = ctx.region.center
                }
                .ignoresSafeArea(edges: .bottom)

                // Center pin
                Image(systemName: "mappin.circle.fill")
                    .font(.system(size: 28))
                    .foregroundColor(Color("WineColor"))
                    .shadow(radius: 2)

                VStack {
                    Spacer()

                    VStack(spacing: 16) {
                        Text("Select Winery Location")
                            .font(.custom("BalooBhaijaan2-Bold", size: 18))
                            .foregroundColor(Color("WineColor"))

                        Text(String(format: "Lat: %.5f   Lng: %.5f", currentCenter.latitude, currentCenter.longitude))
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
                                    .background(Color("WineColor"))
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
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text("Pick Location")
                        .font(.custom("BalooBhaijaan2-Bold", size: 18))
                        .foregroundColor(Color("WineColor"))
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }
}

struct NewReportView_Previews: PreviewProvider {
    static var previews: some View {
        NewReportView(
            onAddPhoto: { },
            onAddLocation: { },
            onPublish: { userName, wineryName, content, rating, imageUrl, location in
                print("Preview publish:", userName, wineryName, content, rating, imageUrl, location)
            }
        )
    }
}