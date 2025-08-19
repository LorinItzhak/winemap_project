import SwiftUI

struct HomeView: View {
    @State private var scale: CGFloat = 0.0
    var body: some View {
        NavigationStack {
            ZStack {
                Image("winemap_bg")
                    .resizable()
                    .scaledToFill()
                    .ignoresSafeArea()
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.95))
                        .frame(width: 350, height: 350)
                        .scaleEffect(scale)
                    VStack(spacing: 16) {
                        Image("logo")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 100, height: 100)
                            .offset(y: -30)
                        Text("The easy way to\ndiscover, rate and\nshare experiences\nfrom all wineries in\nIsrael")
                            .font(.custom("BalooBhaijaan2-Regular", size: 16))
                            .foregroundColor(.black)
                            .multilineTextAlignment(.center)
                            .lineSpacing(4)
                        NavigationLink(destination: RegisterView()) {
                            Text("LET'S GO !")
                                .font(.custom("BalooBhaijaan2-Medium", size: 14))
                                .foregroundColor(.white)
                                .frame(width: 120, height: 40)
                                .background(Color(red: 0.42, green: 0.36, blue: 0.45))
                                .cornerRadius(8)
                        }
                    }
                    .padding(40)
                    .scaleEffect(scale)
                }
            }
            .onAppear {
                withAnimation(.spring(response: 0.6, dampingFraction: 0.6, blendDuration: 0.2)) {
                    scale = 1.0
                }
            }
        }
    }
}
