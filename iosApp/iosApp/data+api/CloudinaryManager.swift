import Cloudinary

final class CloudinaryManager {
  static let shared: CLDCloudinary = {
    // replace with your real values:
    let config = CLDConfiguration(
      cloudName: "detpngf0i",
      apiKey:    "859679673437186",
      apiSecret: "mupstOb71Ci2Yg3C3_kI8tRD-CA"
    )
    return CLDCloudinary(configuration: config)
  }()
}
