// ✅ BORDO COLOR: Color(red: 0.545, green: 0.0, blue: 0.0)
// ✅ GOLD COLOR : Color(red: 1.0, green: 0.843, blue: 0.0)

import SwiftUI
import Shared

struct MyReportsView: View {
  @State private var reports: [ReportModel] = []
  @State private var isLoading = false
  @State private var errorText: String?
  @State private var showNewReport = false

  private let repo = ReportRepositoryImpl()
  private let auth = RemoteFirebaseRepository()

  var body: some View {
    NavigationStack {
      ZStack {
        Color("BackgroundGray").ignoresSafeArea()

        if isLoading {
          VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.2)
            Text("Loading your wine reviews...")
                .font(.custom("BalooBhaijaan2-Medium", size: 16))
                .foregroundColor(.secondary)
          }
        } else if let err = errorText {
          VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundColor(.red)
            Text(err)
                .font(.custom("BalooBhaijaan2-Medium", size: 16))
                .foregroundColor(.red)
                .multilineTextAlignment(.center)
          }
          .padding(.horizontal, 32)
        } else if reports.isEmpty {
          VStack(spacing: 24) {
            Image(systemName: "wineglass")
                .font(.system(size: 64))
                .foregroundColor(Color(red: 0.545, green: 0.0, blue: 0.0).opacity(0.6))

            VStack(spacing: 8) {
              Text("No wine reviews yet")
                  .font(.custom("BalooBhaijaan2-Bold", size: 24))
                  .foregroundColor(Color(red: 0.545, green: 0.0, blue: 0.0))

              Text("Start by adding your first wine review!")
                  .font(.custom("BalooBhaijaan2-Medium", size: 16))
                  .foregroundColor(.secondary)
                  .multilineTextAlignment(.center)
            }

            Button {
              showNewReport = true
            } label: {
              HStack(spacing: 8) {
                Image(systemName: "plus")
                Text("Add First Review")
                    .font(.custom("BalooBhaijaan2-Bold", size: 16))
              }
              .foregroundColor(.white)
              .padding(.horizontal, 24)
              .padding(.vertical, 12)
              .background(Color(red: 0.545, green: 0.0, blue: 0.0))
              .cornerRadius(12)
            }
          }
        } else {
          List(reports.sorted(by: { $0.createdAt > $1.createdAt }), id: \.id) { rpt in
            NavigationLink {
              ReportDetailsView(report: rpt)
                  .navigationTitle("Wine Review")
                  .navigationBarTitleDisplayMode(.inline)
            } label: {
              WineReviewRow(report: rpt)
            }
            .listRowSeparator(.hidden)
            .listRowBackground(Color.clear)
          }
          .listStyle(.plain)
          .padding(.bottom, 12)
        }

        // Floating + button
        VStack {
          Spacer()
          HStack {
            Spacer()
            Button {
              showNewReport = true
            } label: {
              HStack(spacing: 8) {
                Image(systemName: "plus")
                    .font(.system(size: 16, weight: .bold))
                Text("Review")
                    .font(.custom("BalooBhaijaan2-Bold", size: 14))
              }
              .foregroundColor(.white)
              .padding(.horizontal, 16)
              .padding(.vertical, 12)
              .background(Color(red: 0.545, green: 0.0, blue: 0.0))
              .cornerRadius(12)
              .shadow(color: Color.black.opacity(0.2),
                      radius: 4, x: 0, y: 2)
            }
            .padding(.trailing, 16)
            .padding(.bottom, 24)
          }
        }
      }
      .navigationTitle("My Wine Reviews")
      .navigationBarTitleDisplayMode(.inline)
    }
    .onAppear { loadReports() }
    .sheet(isPresented: $showNewReport) {
      ReportsContainerView()
    }
  }

  private func loadReports() {
    guard let uid = auth.currentUserUid() else {
      self.errorText = "Please sign in first."
      return
    }
    isLoading = true
    errorText = nil

    repo.getReportsForUser(userId: uid) { list, err in
      DispatchQueue.main.async {
        self.isLoading = false
        if let err = err {
          self.errorText = err.localizedDescription
          return
        }
        self.reports = list ?? []
      }
    }
  }
}

struct WineReviewRow: View {
  let report: ReportModel

  var body: some View {
    VStack(spacing: 0) {
      if !report.imageUrl.isEmpty, let url = URL(string: report.imageUrl) {
        AsyncImage(url: url) { img in
          img.resizable().scaledToFill()
        } placeholder: {
          Rectangle()
              .fill(Color.gray.opacity(0.2))
              .overlay(
                Image(systemName: "wineglass")
                    .font(.title2)
                    .foregroundColor(.gray)
              )
        }
        .frame(height: 160)
        .clipped()
        .cornerRadius(14, corners: [.topLeft, .topRight])
      } else {
        Rectangle()
        .fill(Color.gray.opacity(0.15))
        .frame(height: 160)
        .overlay(
          VStack(spacing: 8) {
            Image(systemName: "wineglass")
                .font(.title2)
                .foregroundColor(.gray)
            Text("No image")
                .font(.caption)
                .foregroundColor(.gray)
          }
        )
        .cornerRadius(14, corners: [.topLeft, .topRight])
      }

      VStack(alignment: .leading, spacing: 12) {
        Text(report.wineryName.isEmpty ? "Unknown Winery" : report.wineryName)
            .font(.custom("BalooBhaijaan2-Bold", size: 18))
            .foregroundColor(Color(red: 0.545, green: 0.0, blue: 0.0))
            .lineLimit(1)

        HStack(spacing: 4) {
          ForEach(0..<5) { index in
            Image(systemName: index < report.rating ? "star.fill" : "star")
                .font(.system(size: 14))
                .foregroundColor(index < report.rating ? Color(red: 1.0, green: 0.843, blue: 0.0) : Color.gray.opacity(0.5))
          }

          Spacer()

          Text("\(report.rating)/5")
              .font(.custom("BalooBhaijaan2-Medium", size: 14))
              .foregroundColor(.secondary)
        }

        if !report.content.isEmpty {
          Text(report.content)
              .font(.custom("BalooBhaijaan2-Regular", size: 14))
              .foregroundColor(.secondary)
              .lineLimit(2)
              .truncationMode(.tail)
        }

        HStack {
          if let location = report.location, !location.name.isEmpty {
            HStack(spacing: 4) {
              Image(systemName: "location")
                  .font(.caption2)
                  .foregroundColor(.secondary)
              Text(location.name)
                  .font(.custom("BalooBhaijaan2-Regular", size: 12))
                  .foregroundColor(.secondary)
                  .lineLimit(1)
            }
          }

          Spacer()

          Text(formatDate(report.createdAt))
              .font(.custom("BalooBhaijaan2-Regular", size: 12))
              .foregroundColor(.secondary)
        }

        if !report.userName.isEmpty {
          Text("by \(report.userName)")
              .font(.custom("BalooBhaijaan2-Medium", size: 12))
              .foregroundColor(Color(red: 0.545, green: 0.0, blue: 0.0))
        }
      }
      .padding(16)
      .background(Color.white)
      .cornerRadius(14, corners: [.bottomLeft, .bottomRight])
    }
    .background(Color.white)
    .cornerRadius(14)
    .shadow(color: Color.black.opacity(0.08), radius: 6, x: 0, y: 3)
  }

  private func formatDate(_ timestamp: Int64) -> String {
    let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    return formatter.string(from: date)
  }
}

extension View {
  func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
    clipShape(RoundedCorner(radius: radius, corners: corners))
  }
}

struct RoundedCorner: Shape {
  var radius: CGFloat = .infinity
  var corners: UIRectCorner = .allCorners

  func path(in rect: CGRect) -> Path {
    let path = UIBezierPath(
      roundedRect: rect,
      byRoundingCorners: corners,
      cornerRadii: CGSize(width: radius, height: radius)
    )
    return Path(path.cgPath)
  }
}
