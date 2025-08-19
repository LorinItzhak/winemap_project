import SwiftUI
import Lottie

struct WineLoaderView: View {
    var body: some View {
        LottieContainerView(filename: "wine_glass_loader")
            .frame(width: 150, height: 150)
    }
}

private struct LottieContainerView: UIViewRepresentable {
    let filename: String

    func makeUIView(context: Context) -> UIView {
        let container = UIView(frame: .zero)
        let animationView = LottieAnimationView(name: filename)
        animationView.loopMode = .loop
        animationView.contentMode = .scaleAspectFit
        animationView.translatesAutoresizingMaskIntoConstraints = false
        container.addSubview(animationView)
        NSLayoutConstraint.activate([
                                        animationView.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                                        animationView.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                                        animationView.topAnchor.constraint(equalTo: container.topAnchor),
                                        animationView.bottomAnchor.constraint(equalTo: container.bottomAnchor)
                                    ])
        animationView.play()
        return container
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}



