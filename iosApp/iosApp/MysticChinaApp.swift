import SwiftUI

@main
struct MysticChinaApp: App {
    var body: some Scene {
        WindowGroup {
            KuiklyViewController()
        }
    }
}

struct KuiklyViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // 通过 Kuikly 启动页面
        let vc = KuiklyPagerManager.shared.startPager(pageName: "MainPage")
        return vc ?? UIViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
